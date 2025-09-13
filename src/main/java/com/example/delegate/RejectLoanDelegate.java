package com.example.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("rejectLoanDelegate")
public class RejectLoanDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {        
        System.out.println("reject loadn delegate is executed...");        
    }
}
