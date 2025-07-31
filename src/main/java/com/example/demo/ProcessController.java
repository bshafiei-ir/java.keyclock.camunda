package com.example.demo;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final ProcessEngine processEngine;
    private final HistoryService historyService;
    private final TaskService taskService;

    public ProcessController(RuntimeService runtimeService,
            RepositoryService repositoryService,
            ProcessEngine processEngine,
            HistoryService historyService,
            TaskService taskService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.processEngine = processEngine;
        this.historyService = historyService;
        this.taskService = taskService;
    }

    /**
     * Start a process instance by processKey.
     */
    @PostMapping("/start/{processKey}")
    public ResponseEntity<?> startProcess(@PathVariable String processKey) {
        try {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processKey)
                    .latestVersion()
                    .singleResult();

            if (definition == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Process definition not found",
                        "processKey", processKey));
            }

            ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey);

            return ResponseEntity.ok(Map.of(
                    "instanceId", instance.getId(),
                    "definitionId", instance.getProcessDefinitionId(),
                    "isEnded", instance.isEnded()));
        } catch (Exception e) {
            return handleException(e, "Failed to start process");
        }
    }

    /**
     * List all deployed process definitions.
     */
    @GetMapping("/definitions")
    public ResponseEntity<?> getProcessDefinitions() {
        try {
            List<ProcessDefinition> definitions = repositoryService
                    .createProcessDefinitionQuery()
                    .orderByProcessDefinitionVersion()
                    .asc()
                    .list();

            List<Map<String, Object>> result = definitions.stream()
                    .map(def -> {
                        Map<String, Object> defMap = new LinkedHashMap<>();
                        defMap.put("id", def.getId());
                        defMap.put("key", def.getKey());
                        defMap.put("name", def.getName());
                        defMap.put("version", def.getVersion());
                        defMap.put("deploymentId", def.getDeploymentId());
                        return defMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error retrieving process definitions",
                            "details", e.getMessage()));
        }

    }

    /**
     * Get active process instances by processKey.
     */
    @GetMapping("/instances/{processKey}")
    public ResponseEntity<?> getActiveInstances(@PathVariable String processKey) {
        try {
            List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(processKey)
                    .list();

            List<Map<String, Object>> result = instances.stream()
                    .map(pi -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", pi.getId());
                        m.put("definitionId", pi.getProcessDefinitionId());
                        m.put("isEnded", pi.isEnded());
                        m.put("businessKey", Optional.ofNullable(pi.getBusinessKey()).orElse("N/A"));
                        m.put("activityIds", runtimeService.getActiveActivityIds(pi.getId()));
                        return m;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return handleException(e, "Failed to get process instances");
        }
    }

    /**
     * Get details about a specific process instance.
     */
    @GetMapping("/instance/{instanceId}")
    public ResponseEntity<?> getInstanceDetails(@PathVariable String instanceId) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();

            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();

            result.put("runtimeExists", instance != null);
            if (instance != null) {
                result.put("definitionId", instance.getProcessDefinitionId());
                result.put("isEnded", instance.isEnded());
                result.put("activityIds", runtimeService.getActiveActivityIds(instanceId));

                List<Task> tasks = taskService.createTaskQuery()
                        .processInstanceId(instanceId)
                        .list();

                result.put("activeTasks", tasks.stream().map(Task::getName).collect(Collectors.toList()));
            }

            HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            result.put("historyExists", historic != null);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return handleException(e, "Failed to get instance details");
        }
    }

    /**
     * Get all tasks for a given process key.
     */
    @GetMapping("/tasks/{processKey}")
    public ResponseEntity<?> getActiveTasks(@PathVariable String processKey) {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(processKey)
                .list();

        List<Map<String, Object>> result = tasks.stream()
                .map(task -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("taskId", task.getId());
                    map.put("taskName", task.getName());
                    map.put("instanceId", task.getProcessInstanceId());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);

    }

    /**
     * Get basic engine & DB info.
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        try {
            String engineName = processEngine.getName();
            String dbType = processEngine.getProcessEngineConfiguration().getDatabaseType();
            return ResponseEntity.ok(Map.of(
                    "engineName", engineName,
                    "databaseType", dbType,
                    "camundaVersion", ProcessEngine.VERSION,
                    "springBootVersion", SpringBootVersion.getVersion()));
        } catch (Exception e) {
            return handleException(e, "Failed to get engine info");
        }
    }

    /**
     * Get DB connection info.
     */
    @GetMapping("/db-info")
    public ResponseEntity<?> getDatabaseInfo() {
        try (Connection conn = processEngine.getProcessEngineConfiguration().getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            return ResponseEntity.ok(Map.of(
                    "database", metaData.getDatabaseProductName(),
                    "url", metaData.getURL()));
        } catch (Exception e) {
            return handleException(e, "Failed to retrieve DB info");
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeTask(@RequestParam String taskId) {
        taskService.complete(taskId);
        return ResponseEntity.ok("Task completed");
    }

    private ResponseEntity<?> handleException(Exception e, String message) {
        return ResponseEntity.status(500).body(Map.of(
                "error", message,
                "message", e.getMessage(),
                "cause", Optional.ofNullable(e.getCause()).map(Throwable::getMessage).orElse("N/A")));
    }
}
