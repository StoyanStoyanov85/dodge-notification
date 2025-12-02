package com.dodge_notification.web;

import com.dodge_notification.dto.ErrorResponse;
import com.dodge_notification.exception.NoResourceFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ExceptionAdvice.class)
public class ExceptionAdviceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @GetMapping("/error")
        public String throwError() {
            throw new NoResourceFoundException();
        }
    }

    @Test
    void whenNoResourceFound_thenReturnCustomErrorResponse() {

        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity("/test/error", ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Invalid request. Please check.", body.getMessage());
    }
}

