package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loan/history")
public class LoanHistoryController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    // دریافت فرآیندهای در حال اجرا
    @GetMapping("/running")
    public List<Map<String, Object>> getRunningInstances() {
        return runtimeService.createProcessInstanceQuery()
                .list()
                .stream()
                .map(pi -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pi.getId());
                    map.put("processDefinitionId", pi.getProcessDefinitionId());
                    map.put("businessKey", pi.getBusinessKey());
                    map.put("isEnded", pi.isEnded());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{processInstanceId}/tasks")
    public List<Map<String, String>> getActiveTasks(@PathVariable String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .map(task -> Map.of(
                        "id", task.getId(),
                        "name", task.getName(),
                        "assignee", task.getAssignee()))
                .toList();
    }

    @GetMapping("/{processInstanceId}/history")
    public List<Map<String, Object>> getHistory(@PathVariable String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list()
                .stream()
                .map(act -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("activityId", act.getActivityId());
                    map.put("activityName", act.getActivityName());
                    map.put("activityType", act.getActivityType());
                    map.put("startTime", act.getStartTime());
                    map.put("endTime", act.getEndTime());
                    return map;
                })
                .collect(Collectors.toList());
    }
}