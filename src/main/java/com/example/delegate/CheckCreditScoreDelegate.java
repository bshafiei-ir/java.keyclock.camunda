package com.example.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("checkCreditScoreDelegate")
public class CheckCreditScoreDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {        
        System.out.println("Checking credit score...");
        execution.setVariable("creditScore", 700);
    }
}
