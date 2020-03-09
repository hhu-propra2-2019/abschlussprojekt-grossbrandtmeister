package mops.rheinjug2.controllers;

import com.sun.istack.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rheinjug2")
public class SimpleMailController {
  
  public final JavaMailSender emailSender;
  
  public SimpleMailController(JavaMailSender emailSender) {
    this.emailSender = emailSender;
  }
  
  /**
   * Dummy Methode die beim aufrufen von /sendSimpleEmail eine
   * Test Email an eine angegebene Email sendet.
   *
   * @return Dummy String
   */
  @ResponseBody
  @RequestMapping("/sendSimpleEmail")
  public String sendSimpleEmail() throws Exception {
    
    // Create a Simple MailMessage.
    SimpleMailMessage message = new SimpleMailMessage();
    
    message.setTo("pamei104@uni-duesseldorf.de");
    message.setSubject("Test Simple Email");
    message.setText("Hello, Im testing Simple Email");
    
    
    MimeBodyPart textBodyPart = new MimeBodyPart();
    textBodyPart.setText(message.getText());
    
    //now write the PDF content to the output stream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    writePdf(outputStream, "foo", "bar", "foo.bar@foobar.de");
    byte[] bytes = outputStream.toByteArray();
    
    //construct the pdf body part
    ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
    MimeBodyPart pdfBodyPart = new MimeBodyPart();
    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
    pdfBodyPart.setFileName("DummyCertificate.pdf");
    
    //construct the mime multi part
    MimeMultipart mimeMultipart = new MimeMultipart();
    mimeMultipart.addBodyPart(textBodyPart);
    mimeMultipart.addBodyPart(pdfBodyPart);
    
    
    //construct the mime message
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setSubject(message.getSubject());
    mimeMessageHelper.setTo(message.getTo());
    mimeMessage.setContent(mimeMultipart);
    
    // Send Message!
    emailSender.send(mimeMessage);
    
    return "Email Sent!";
  }
  
  /**
   * Füllt den Schein mit den Informationen des/der Studenten/Studentin.
   *
   * @param outputStream zum Speichern der befüllten PdfForm
   * @param vorname      des Studenten
   * @param nachname     des Studenten
   * @param email        Zieladresse der E-Mail
   */
  public void writePdf(ByteArrayOutputStream outputStream, String vorname, String nachname, String email) throws Exception {
    File pdf = new File("./DummyCertificate.pdf");
    PDDocument pdfForm = PDDocument.load(pdf);
    
    PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();
    
    setScheinDate(acroForm);
    
    acroForm.getField("name2[first]").setValue(vorname);
    acroForm.getField("name2[last]").setValue(nachname);
    acroForm.getField("email3").setValue(email);
    acroForm.getField("scheinart6").setValue("EntwickelBar");
    
    // pdfForm.save("ScheinDummy" + vorname + ".pdf");
    pdfForm.save(outputStream);
    
    pdfForm.close();
  }
  
  /**
   * Setzt beim Schein das aktuelle Datum.
   *
   * @param acroForm Schein-PDF
   */
  private static void setScheinDate(PDAcroForm acroForm) throws IOException {
    LocalDate currentDate = LocalDate.now();
    int day = currentDate.getDayOfMonth();
    int month = currentDate.getMonthValue();
    int year = currentDate.getYear();
    
    acroForm.getField("date7[day]").setValue(Integer.toString(day));
    acroForm.getField("date7[month]").setValue(Integer.toString(month));
    acroForm.getField("date7[year]").setValue(Integer.toString(year));
  }
}
