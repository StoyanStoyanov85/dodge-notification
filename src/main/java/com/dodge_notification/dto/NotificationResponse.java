package com.dodge_notification.dto;

import com.dodge_notification.model.NotificationStatus;
import com.dodge_notification.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID userId;

    private String subject;

    private LocalDateTime createdOn;

    private NotificationStatus status;

    private NotificationType type;
}