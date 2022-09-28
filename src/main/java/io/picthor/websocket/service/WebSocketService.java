package io.picthor.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import io.picthor.data.entity.BatchJob;
import io.picthor.rest.repr.BatchJobRepr;
import io.picthor.rest.repr.JobCounterRepr;
import io.picthor.rest.repr.NotificationRepr;
import io.picthor.services.JobCounter;
import io.picthor.services.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate template;

    private final ObjectMapper objectMapper;

    private final RateLimiter rateLimiter;

    public WebSocketService(SimpMessagingTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.rateLimiter = RateLimiter.create(10);
    }

    public void publishNotification(Notification notification) {
        log.debug("Publishing new notification: {}", notification.getId());
        try {
            this.template.convertAndSend("/topic/notifications", objectMapper.writeValueAsString(new NotificationRepr(notification)));
        } catch (JsonProcessingException e) {
            log.error("Failed to publish notification: {}", notification.getId());
        }
    }

    public void publishJobCounterUpdated(JobCounter counter) {
        if (rateLimiter.tryAcquire(1)) {
            log.debug("Publishing job counter update: {}", counter.getJobId());
            try {
                this.template.convertAndSend("/topic/jobs/counter-update", objectMapper.writeValueAsString(new JobCounterRepr(counter)));
            } catch (JsonProcessingException e) {
                log.error("Failed to publish job updated: {}", counter.getJobId());
            }
        }
    }

    public void publishJobAdded(BatchJob job) {
        log.debug("Publishing job added: {}", job.getId());
        try {
            this.template.convertAndSend("/topic/jobs/add", objectMapper.writeValueAsString(new BatchJobRepr(job)));
        } catch (JsonProcessingException e) {
            log.error("Failed to publish job added: {}", job.getId());
        }
    }

    public void publishJobRemoved(BatchJob job) {
        log.debug("Publishing job removed: {}", job.getId());
        try {
            this.template.convertAndSend("/topic/jobs/remove", objectMapper.writeValueAsString(new BatchJobRepr(job)));
        } catch (JsonProcessingException e) {
            log.error("Failed to publish job removed: {}", job.getId());
        }

    }
}
