package com.dodge_notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPreference {

    private UUID userId;
    private boolean enabled;
    private String contactInfo;
}
