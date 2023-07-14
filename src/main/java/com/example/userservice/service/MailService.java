package com.example.userservice.service;

import com.example.userservice.domain.dto.request.MailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class MailService {

    private final RestTemplate restTemplate;

    @Value("${services.notification-url}")
    private String notificationServiceUrl;

    public String sendVerificationCode(String email, String verificationCode,String link) {
        String message = "This is your verification code: " + verificationCode + "\nThis is your link "+ link;
        return sendMail(message, email, "/send-single");
    }

    private String sendMail(String message, String email, String uri) {
        MailDto mailDto = new MailDto(message, email);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MailDto> entity = new HttpEntity<>(mailDto, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(notificationServiceUrl + uri),
                HttpMethod.POST,
                entity,
                String.class);
        return response.getBody();
    }

}
