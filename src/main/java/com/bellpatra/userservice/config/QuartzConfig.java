package com.bellpatra.userservice.config;

import com.bellpatra.userservice.job.EmailProcessingJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    @Bean
    public JobDetail emailProcessingJobDetail() {
        return JobBuilder.newJob(EmailProcessingJob.class)
                .withIdentity("emailProcessingJob")
                .withDescription("Process pending and failed emails")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger emailProcessingTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(emailProcessingJobDetail())
                .withIdentity("emailProcessingTrigger")
                .withDescription("Trigger for email processing job")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(30) // Run every 30 seconds
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail emailRetryJobDetail() {
        return JobBuilder.newJob(EmailProcessingJob.class)
                .withIdentity("emailRetryJob")
                .withDescription("Retry failed emails")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger emailRetryTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(emailRetryJobDetail())
                .withIdentity("emailRetryTrigger")
                .withDescription("Trigger for email retry job")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5) // Run every 5 minutes
                        .repeatForever())
                .build();
    }

}
