package com.dodge_notification.service;

import com.dodge_notification.dto.NotificationRequest;
import com.dodge_notification.model.Notification;
import com.dodge_notification.model.NotificationPreference;
import com.dodge_notification.model.NotificationStatus;
import com.dodge_notification.model.NotificationType;
import com.dodge_notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final MailSender mailSender;
    private final NotificationRepository notificationRepository;


    @Autowired
    public EmailService(MailSender mailSender, NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    public Notification sendNotificationEmail(NotificationRequest notificationRequest) {

        UUID userId = notificationRequest.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isEnabled()) {
            throw new IllegalArgumentException(
                    "User with id %s does not allow to receive notifications.".formatted(userId)
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getBody());

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .userId(userId)
                .deleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending an email to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllStatuses() {
        return notificationRepository.findAllByDeletedFalse();
    }


    public List<Notification> getNotificationUser(UUID userId) {
        return notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);
    }


    public void clearNotifications(UUID userId) {
        List<Notification> notifications = getNotificationUser(userId);

        for (Notification notification : notifications) {
            notification.setDeleted(true);
        }
        notificationRepository.saveAll(notifications);
    }


    public NotificationPreference getPreferenceByUserId(UUID userId) {
        return new NotificationPreference(userId, true, "stoyan.stoyanov.kz@mail.bg");
    }
}
