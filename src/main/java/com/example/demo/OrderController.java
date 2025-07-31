package com.example.demo;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public OrderController(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOrder(@RequestParam String customer) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "order_process",
                Map.of("customer", customer)
        );
        return ResponseEntity.ok("Started process ID: " + instance.getId());
    }

    @GetMapping("/tasks")
    public List<String> getTasks() {
        return taskService.createTaskQuery()
                .taskAssignee("operator")
                .list()
                .stream()
                .map(task -> task.getId() + ": " + task.getName())
                .toList();
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeTask(@RequestParam String taskId) {
        taskService.complete(taskId);
        return ResponseEntity.ok("Task " + taskId + " completed.");
    }
}
