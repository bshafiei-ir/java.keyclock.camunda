package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class SendSmsDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String customer = (String) execution.getVariable("customer");
        System.out.println("📱 پیامک ارسال شد برای مشتری: " + customer);
    }
}
