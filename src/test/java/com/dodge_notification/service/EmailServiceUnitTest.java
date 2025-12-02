package com.dodge_notification.service;


import com.dodge_notification.dto.NotificationRequest;
import com.dodge_notification.model.Notification;
import com.dodge_notification.model.NotificationPreference;
import com.dodge_notification.model.NotificationStatus;
import com.dodge_notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUnitTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private EmailService emailService;


    @Test
    void sendNotificationEmail_shouldThrowException_whenUserPreferenceDisabled() {
        UUID userId = UUID.randomUUID();
        NotificationRequest req = new NotificationRequest(userId,"subj", "body");

        EmailService spyService = Mockito.spy(emailService);
        Mockito.doReturn(new NotificationPreference(userId, false, "x@mail.bg"))
                .when(spyService).getPreferenceByUserId(userId);

        assertThrows(IllegalArgumentException.class,
                () -> spyService.sendNotificationEmail(req));
    }

    @Test
    void sendNotificationEmail_shouldSendEmailSuccessfully() {
        UUID userId = UUID.randomUUID();
        NotificationRequest req = new NotificationRequest(userId,"subj", "body");

        EmailService spyService = Mockito.spy(emailService);
        Mockito.doReturn(new NotificationPreference(userId, true, "stoyan.stoyanov.kz@mail.bg"))
                .when(spyService).getPreferenceByUserId(userId);

        Notification saved = Notification.builder().status(NotificationStatus.SUCCEEDED).build();
        when(notificationRepository.save(any())).thenReturn(saved);

        Notification result = spyService.sendNotificationEmail(req);

        assertEquals(NotificationStatus.SUCCEEDED, result.getStatus());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void sendNotificationEmail_shouldSetStatusFailed_whenMailSenderThrowsException() {
        UUID userId = UUID.randomUUID();
        NotificationRequest notificationRequest = new NotificationRequest(userId, "subj", "body");

        EmailService spyService = Mockito.spy(emailService);
        Mockito.doReturn(new NotificationPreference(userId, true, "stoyan.stoyanov.kz@mail.bg"))
                .when(spyService).getPreferenceByUserId(userId);

        doThrow(new RuntimeException("Mail error"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        Notification saved = Notification.builder()
                .status(NotificationStatus.FAILED)
                .build();

        when(notificationRepository.save(any())).thenReturn(saved);

        Notification result = spyService.sendNotificationEmail(notificationRequest);

        assertEquals(NotificationStatus.FAILED, result.getStatus());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }



    @Test
    void getAllStatuses_shouldReturnListFromRepository() {
        List<Notification> expected = List.of(new Notification());
        when(notificationRepository.findAllByDeletedFalse()).thenReturn(expected);

        List<Notification> result = emailService.getAllStatuses();

        assertEquals(expected, result);
        verify(notificationRepository, times(1)).findAllByDeletedFalse();
    }



    @Test
    void getNotificationUser_shouldReturnUserNotifications() {
        UUID userId = UUID.randomUUID();
        List<Notification> expected = List.of(new Notification());
        when(notificationRepository.findAllByUserIdAndDeletedIsFalse(userId))
                .thenReturn(expected);

        List<Notification> result = emailService.getNotificationUser(userId);

        assertEquals(expected, result);
        verify(notificationRepository, times(1))
                .findAllByUserIdAndDeletedIsFalse(userId);
    }



    @Test
    void clearNotifications_shouldMarkNotificationsAsDeleted() {
        UUID userId = UUID.randomUUID();
        Notification notificationFirst = Notification.builder().deleted(false).build();
        Notification notificationSecond = Notification.builder().deleted(false).build();
        List<Notification> list = List.of(notificationFirst, notificationSecond);

        EmailService spyService = Mockito.spy(emailService);
        doReturn(list).when(spyService).getNotificationUser(userId);

        spyService.clearNotifications(userId);

        assertTrue(notificationFirst.isDeleted());
        assertTrue(notificationSecond.isDeleted());
        verify(spyService, times(1)).getNotificationUser(userId);
        verify(notificationRepository, times(1)).saveAll(list);
    }



    @Test
    void getPreferenceByUserId_shouldReturnPreferenceWithSameUserId() {
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = emailService.getPreferenceByUserId(userId);
        assertEquals(userId, notificationPreference.getUserId());
    }

    @Test
    void getPreferenceByUserId_shouldReturnPreferenceEnabledTrue() {
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = emailService.getPreferenceByUserId(userId);
        assertTrue(notificationPreference.isEnabled());
    }

    @Test
    void getPreferenceByUserId_shouldReturnPreferenceWithExpectedContactInfo() {
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = emailService.getPreferenceByUserId(userId);
        assertEquals("stoyan.stoyanov.kz@mail.bg", notificationPreference.getContactInfo());
    }

    @Test
    void getPreferenceByUserId_shouldNotInteractWithMailSenderOrRepository() {
        UUID userId = UUID.randomUUID();
        emailService.getPreferenceByUserId(userId);
        verifyNoInteractions(mailSender, notificationRepository);
    }
}
