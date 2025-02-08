package com.auth.springbackend.email;


import javax.mail.MessagingException;

public interface EmailService {

    // Method
    // To send a simple email
    String sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);

    void sendHtmlMessage(EmailDetails email) throws MessagingException;
}
