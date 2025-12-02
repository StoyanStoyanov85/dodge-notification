package com.dodge_notification.controller;

import com.dodge_notification.dto.DtoMapper;
import com.dodge_notification.dto.NotificationRequest;
import com.dodge_notification.dto.NotificationResponse;
import com.dodge_notification.model.Notification;
import com.dodge_notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/notifyAdvanced")
    public ResponseEntity<String> sendNotificationEmail(@RequestBody NotificationRequest request) {
        try {
            Notification notification = emailService.sendNotificationEmail(request);
            return ResponseEntity.ok("Notification status: " + notification.getStatus());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<NotificationResponse>> checkStatus() {
        List<NotificationResponse> status = emailService.getAllStatuses().stream().map(DtoMapper::fromNotification).toList();
        return ResponseEntity.ok(status);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearNotificationUser(@RequestParam(name = "userId") UUID userId) {
        emailService.clearNotifications(userId);
        return ResponseEntity.ok().body(null);
    }
}
