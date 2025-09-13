package com.example.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("disburseLoanDelegate")
public class DisburseLoanDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {        
        System.out.println("disburse load delegate is executed...");        
    }
}
