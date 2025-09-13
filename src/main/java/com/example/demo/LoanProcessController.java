package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.LoanRequestDto;
import com.example.dto.TaskDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanProcessController {
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @PostMapping("/start")
    public ResponseEntity<String> startLoanProcess(@RequestBody LoanRequestDto dto) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantName", dto.getApplicantName());
        variables.put("amount", dto.getAmount());
        variables.put("creditScore", dto.getCreditScore());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("loanRequestProcess", variables);
        return ResponseEntity.ok("Started process instance with ID: " + instance.getId());
    }

    @GetMapping("/tasks")
    public List<TaskDto> getUserTasks(@RequestParam String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();

        return tasks.stream()
                .map(task -> new com.example.dto.TaskDto(task.getId(), task.getName(), task.getProcessInstanceId()))
                .collect(Collectors.toList());
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }

        taskService.complete(taskId, variables);
        return ResponseEntity.ok("Task " + taskId + " completed successfully.");
    }
}
