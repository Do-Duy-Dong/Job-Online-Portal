package com.example.demo.service;

import com.example.demo.exception.ResourceNotFound;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;

    @Async
    public void sendMail(
            String to,
            String subject,
            Map<String, Object> map
    ){
        Context context = new Context();
        context.setVariables(map);
        String htmlBody= templateEngine.process("email",context);

        try{
//            Tạo đối tượng MimeMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper= new MimeMessageHelper(mimeMessage,true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("TopCv@cv.com");
            helper.setText(htmlBody,true);

            mailSender.send(mimeMessage);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFound("Failed to send email to " + to);
        }
    }
}
