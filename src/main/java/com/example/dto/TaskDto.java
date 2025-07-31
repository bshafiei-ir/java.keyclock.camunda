package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TaskDto {
    private String id;
    private String name;
    private String processInstanceId;
}
