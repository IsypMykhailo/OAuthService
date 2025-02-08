package com.auth.springbackend.events.listener;

import com.auth.springbackend.email.EmailDetails;
import com.auth.springbackend.email.EmailServiceImpl;
import com.auth.springbackend.events.OnRegistrationCompleteEvent;
import com.auth.springbackend.service.EmailVerificationTokenService;
import com.auth.springbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private EmailVerificationTokenService service;

    @Autowired
    private MessageSource messages;

    @Autowired
    private EmailServiceImpl emailService;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        try {
            this.confirmRegistration(event);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) throws MessagingException {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        EmailDetails email = constructEmailMessage(event, user, token);

        emailService.sendHtmlMessage(email);
    }

    private EmailDetails constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {
        String link = "http://localhost:8080/confirm-account?token=" + token;
        EmailDetails email = new EmailDetails();
        email.setRecipient(user.getEmail());
        email.setFrom("Gribble");
        email.setMsgBody("Please Confirm your account to access our resource");
        email.setSubject("Confirm your Email");
        email.setTemplate("confirm-email-template.html");

        Map<String, Object> properties = new HashMap<>();
        properties.put("link", link);
        properties.put("msgBody", email.getMsgBody());
        email.setProperties(properties);

        return email;
    }
}
