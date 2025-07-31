package com.example.dto;

import lombok.Data;

@Data
public class LoanRequestDto {
    private String applicantName;
    private double amount;
    private int creditScore;
}
