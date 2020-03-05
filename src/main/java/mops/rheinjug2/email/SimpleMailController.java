package mops.rheinjug2.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class SimpleMailController {

    @Autowired
    public JavaMailSender emailSender;

    @ResponseBody
    @RequestMapping("/sendSimpleEmail")
    public String sendSimpleEmail() {

        // Create a Simple MailMessage.
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo("Luca10399@yahoo.de");
        message.setSubject("Test Simple Email");
        message.setText("Hello, Im testing Simple Email");

        // Send Message!
        emailSender.send(message);

        return "Email Sent!";
    }
}
