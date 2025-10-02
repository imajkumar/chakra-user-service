package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quartz")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class QuartzController {

    private final Scheduler scheduler;

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllJobs() {
        try {
            List<Map<String, Object>> jobs = new ArrayList<>();
            
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    
                    Map<String, Object> jobInfo = Map.of(
                            "jobName", jobKey.getName(),
                            "jobGroup", jobKey.getGroup(),
                            "jobClass", jobDetail.getJobClass().getSimpleName(),
                            "description", jobDetail.getDescription(),
                            "triggerCount", triggers.size(),
                            "isDurable", jobDetail.isDurable()
                    );
                    jobs.add(jobInfo);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(jobs, "Jobs retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve jobs", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve jobs: " + e.getMessage()));
        }
    }

    @GetMapping("/triggers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTriggers() {
        try {
            List<Map<String, Object>> triggers = new ArrayList<>();
            
            for (String groupName : scheduler.getTriggerGroupNames()) {
                for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName))) {
                    Trigger trigger = scheduler.getTrigger(triggerKey);
                    Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
                    
                    Map<String, Object> triggerInfo = Map.of(
                            "triggerName", triggerKey.getName(),
                            "triggerGroup", triggerKey.getGroup(),
                            "jobName", trigger.getJobKey().getName(),
                            "jobGroup", trigger.getJobKey().getGroup(),
                            "state", state.name(),
                            "nextFireTime", trigger.getNextFireTime() != null ? trigger.getNextFireTime().toString() : "N/A",
                            "previousFireTime", trigger.getPreviousFireTime() != null ? trigger.getPreviousFireTime().toString() : "N/A"
                    );
                    triggers.add(triggerInfo);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(triggers, "Triggers retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve triggers", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve triggers: " + e.getMessage()));
        }
    }

    @PostMapping("/jobs/{jobName}/pause")
    public ResponseEntity<ApiResponse<String>> pauseJob(@PathVariable String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName);
            scheduler.pauseJob(jobKey);
            return ResponseEntity.ok(ApiResponse.success("Job paused successfully", "Job " + jobName + " has been paused"));
        } catch (Exception e) {
            log.error("Failed to pause job: {}", jobName, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to pause job: " + e.getMessage()));
        }
    }

    @PostMapping("/jobs/{jobName}/resume")
    public ResponseEntity<ApiResponse<String>> resumeJob(@PathVariable String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName);
            scheduler.resumeJob(jobKey);
            return ResponseEntity.ok(ApiResponse.success("Job resumed successfully", "Job " + jobName + " has been resumed"));
        } catch (Exception e) {
            log.error("Failed to resume job: {}", jobName, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to resume job: " + e.getMessage()));
        }
    }

    @PostMapping("/jobs/{jobName}/trigger")
    public ResponseEntity<ApiResponse<String>> triggerJob(@PathVariable String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName);
            scheduler.triggerJob(jobKey);
            return ResponseEntity.ok(ApiResponse.success("Job triggered successfully", "Job " + jobName + " has been triggered"));
        } catch (Exception e) {
            log.error("Failed to trigger job: {}", jobName, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to trigger job: " + e.getMessage()));
        }
    }

    @GetMapping("/scheduler/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedulerInfo() {
        try {
            Map<String, Object> schedulerInfo = Map.of(
                    "schedulerName", scheduler.getSchedulerName(),
                    "schedulerInstanceId", scheduler.getSchedulerInstanceId(),
                    "isStarted", scheduler.isStarted(),
                    "isInStandbyMode", scheduler.isInStandbyMode(),
                    "isShutdown", scheduler.isShutdown(),
                    "jobGroupNames", scheduler.getJobGroupNames(),
                    "triggerGroupNames", scheduler.getTriggerGroupNames()
            );
            
            return ResponseEntity.ok(ApiResponse.success(schedulerInfo, "Scheduler info retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve scheduler info", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve scheduler info: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "Quartz Scheduler",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "Quartz scheduler is running"));
    }
}
