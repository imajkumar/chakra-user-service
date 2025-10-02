package com.bellpatra.userservice.job;

import com.bellpatra.userservice.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCleanupJob implements Job {

    private final EmailQueueService emailQueueService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Starting email cleanup job at: {}", java.time.LocalDateTime.now());
            
            emailQueueService.cleanupOldEmails();
            
            log.info("Email cleanup job completed successfully");
            
        } catch (Exception e) {
            log.error("Error in email cleanup job", e);
            throw new JobExecutionException("Email cleanup job failed", e);
        }
    }
}
