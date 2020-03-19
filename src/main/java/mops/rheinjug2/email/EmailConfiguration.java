package mops.rheinjug2.email;


// email + password - will be changed after development
//rheinjug2@gmail.com
//pwd: +JNh&qP6;bALzk"n

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


@Configuration
public class EmailConfiguration {

  /**
   * Konfiguration für den Mail Server.
   * -> Google Server
   *
   * <p>Passwort und Email einstellen
   */
  @Bean
  public JavaMailSender getJavaMailSender() {
    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(587);

    mailSender.setUsername("rheinjug2@gmail.com");
    mailSender.setPassword("+JNh&qP6;bALzk\"n");

    final Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");

    return mailSender;
  }

}
