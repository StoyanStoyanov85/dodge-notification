package com.dodge_notification.dto;

import com.dodge_notification.model.Notification;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationResponse fromNotification(Notification entity) {

        return NotificationResponse.builder()
                .userId(entity.getUserId())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .type(entity.getType())
                .build();
    }
}
