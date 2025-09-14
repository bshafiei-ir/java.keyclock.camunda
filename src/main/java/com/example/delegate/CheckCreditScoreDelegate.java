package com.example.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("test2")
public class CheckCreditScoreDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {        
        int creditScore = (int) (Math.random() * 1000);
        System.out.println("Calculated Credit Score: " + creditScore);
        execution.setVariable("creditScore", creditScore);
    }
}
