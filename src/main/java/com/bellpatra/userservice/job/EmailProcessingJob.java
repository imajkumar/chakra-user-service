package com.bellpatra.userservice.job;

import com.bellpatra.userservice.entity.EmailQueue;
import com.bellpatra.userservice.repository.EmailQueueRepository;
import com.bellpatra.userservice.service.EmailProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingJob implements Job {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailProcessor emailProcessor;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.debug("Starting email processing job at: {}", LocalDateTime.now());
            
            // Process pending emails
            processPendingEmails();
            
            // Process failed emails for retry
            processFailedEmails();
            
            log.debug("Email processing job completed at: {}", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error in email processing job", e);
            throw new JobExecutionException("Email processing job failed", e);
        }
    }

    private void processPendingEmails() {
        try {
            List<EmailQueue> pendingEmails = emailQueueRepository.findByStatusAndScheduledAtLessThanEqualOrderByCreatedAtAsc(
                    EmailQueue.EmailStatus.PENDING, 
                    LocalDateTime.now()
            );

            if (pendingEmails.isEmpty()) {
                log.debug("No pending emails to process");
                return;
            }

            log.info("Processing {} pending emails", pendingEmails.size());

            for (EmailQueue emailQueue : pendingEmails) {
                try {
                    emailProcessor.processEmail(emailQueue);
                } catch (Exception e) {
                    log.error("Failed to process email ID: {} for recipient: {}", 
                            emailQueue.getId(), emailQueue.getRecipientEmail(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error processing pending emails", e);
        }
    }

    private void processFailedEmails() {
        try {
            List<EmailQueue> failedEmails = emailQueueRepository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                    EmailQueue.EmailStatus.FAILED, 
                    3 // max retries
            );

            if (failedEmails.isEmpty()) {
                log.debug("No failed emails to retry");
                return;
            }

            log.info("Retrying {} failed emails", failedEmails.size());

            for (EmailQueue emailQueue : failedEmails) {
                try {
                    emailProcessor.processEmail(emailQueue);
                } catch (Exception e) {
                    log.error("Failed to retry email ID: {} for recipient: {}", 
                            emailQueue.getId(), emailQueue.getRecipientEmail(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error processing failed emails", e);
        }
    }
}
