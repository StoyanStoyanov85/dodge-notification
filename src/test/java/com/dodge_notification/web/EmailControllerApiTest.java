package com.dodge_notification.web;

import com.dodge_notification.controller.EmailController;
import com.dodge_notification.model.Notification;
import com.dodge_notification.model.NotificationStatus;
import com.dodge_notification.model.NotificationType;
import com.dodge_notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
public class EmailControllerApiTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void sendNotificationEmail_ShouldReturn200_WhenServiceReturnsNotification() throws Exception {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .subject("Test Subject")
                .body("Test Body")
                .createdOn(LocalDateTime.now())
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SUCCEEDED)
                .deleted(false)
                .build();

        when(emailService.sendNotificationEmail(any()))
                .thenReturn(notification);

        String requestJson = """
            {
                "email": "test@test.com",
                "message": "msg"
            }
            """;

        mockMvc.perform(post("/api/emails/notifyAdvanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification status: SUCCEEDED"));
    }


    @Test
    void sendNotificationEmail_ShouldReturn400_WhenServiceThrowsIllegalArgument() throws Exception {
        when(emailService.sendNotificationEmail(any()))
                .thenThrow(new IllegalArgumentException("Invalid email"));

        String requestJson = """
                {
                    "email": "bad",
                    "message": "msg"
                }
                """;

        mockMvc.perform(post("/api/emails/notifyAdvanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email"));
    }



    @Test
    void checkStatus_ShouldReturn200WithList_WhenStatusesExist() throws Exception {

        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .subject("Subject")
                .body("Hello")
                .createdOn(LocalDateTime.now())
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SUCCEEDED)
                .deleted(false)
                .build();

        when(emailService.getAllStatuses()).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/emails/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].userId").isNotEmpty())
                .andExpect(jsonPath("$[0].subject").isNotEmpty())
                .andExpect(jsonPath("$[0].createdOn").isNotEmpty())
                .andExpect(jsonPath("$[0].status").isNotEmpty())
                .andExpect(jsonPath("$[0].type").isNotEmpty());
    }



    @Test
    void checkStatus_ShouldReturn200WithEmptyList_WhenNoStatusesExist() throws Exception {
        when(emailService.getAllStatuses()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/emails/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }



    @Test
    void clearNotificationUser_ShouldReturn200_WhenUserIdValid() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/emails")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());

        verify(emailService, times(1)).clearNotifications(userId);
    }

    @Test
    void clearNotificationUser_ShouldReturn400_WhenUserIdMissing() throws Exception {
        mockMvc.perform(delete("/api/emails"))
                .andExpect(status().isBadRequest());
    }
}
