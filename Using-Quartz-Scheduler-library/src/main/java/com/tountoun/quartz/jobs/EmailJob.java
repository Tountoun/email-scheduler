package com.tountoun.quartz.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

// Job class which could help instanciate email job details
@Component
public class EmailJob extends QuartzJobBean {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MailProperties mailProperties;

    @Override
    // This method is called when the email job is triggered
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // Get  the job data set when creating the job detail
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String recipientEmail = jobDataMap.getString("email");
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");

        // Then the informations can be sent to the reciever
        sendEmail(mailProperties.getUsername(), recipientEmail, subject, body);
    }
    private void sendEmail(String fromEmail, String toEmail, String subject, String body){
        try{
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            mimeMessageHelper.setFrom(fromEmail);
            mimeMessageHelper.setTo(toEmail);

            mailSender.send(message);
        }catch (MessagingException e){
            System.out.println(e.getMessage());
        }
    }
}
