package com.example.digitalapproval.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            logger.warn("Email delivery failed: {}", ex.getMessage());
        }
    }

    public void sendApprovalNotification(String email, String requestTitle, String status) {
        String subject = "Request " + status;
        String text = "Your request '" + requestTitle + "' has been " + status.toLowerCase() + ".";
        sendEmail(email, subject, text);
    }
}