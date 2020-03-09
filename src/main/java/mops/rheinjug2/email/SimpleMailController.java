package mops.rheinjug2.email;

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
   * Dummy Methode die beim aufrufen von /sendEmail eine
   * Test Email an eine angegebene Email sendet.
   *
   * @return Dummy String
   */
  @ResponseBody
  @RequestMapping("/sendEmail")
  public String sendEmailWithCertificate() throws Exception {
    final String recipient = "pamei104@uni-duesseldorf.de";
    final String subject = "Test Email";
    final String text = "Test";
    
    // Write certificate to outputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // Dummy Values for testing purposes
    createCertificatePdf(outputStream, "foo", "bar", "foo.bar@foobar.de");
    byte[] bytes = outputStream.toByteArray();
    
    ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
    MimeBodyPart pdfBodyPart = new MimeBodyPart();
    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
    pdfBodyPart.setFileName("DummyCertificate.pdf");
    
    MimeBodyPart textBodyPart = new MimeBodyPart();
    textBodyPart.setText(text);
    
    MimeMultipart mimeMultipart = new MimeMultipart();
    mimeMultipart.addBodyPart(textBodyPart);
    mimeMultipart.addBodyPart(pdfBodyPart);
    
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setTo(recipient);
    mimeMessage.setContent(mimeMultipart);
    
    emailSender.send(mimeMessage);
    
    return "Email Sent!";
  }
  
  /**
   * Füllt den Schein mit den Informationen des/der Studenten/Studentin.
   *
   * @param outputStream zum Speichern der befüllten PdfForm
   * @param forename     des Studenten/der Studentin
   * @param surname      des Studenten/der Studentin
   * @param email        des Studenten/der Studentin
   */
  public void createCertificatePdf(ByteArrayOutputStream outputStream,
                                   String forename, String surname, String email) throws Exception {
    File pdf = new File("./DummyCertificate.pdf");
    PDDocument pdfForm = PDDocument.load(pdf);
    PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();
    
    setCertificateDate(acroForm);
    acroForm.getField("name2[first]").setValue(forename);
    acroForm.getField("name2[last]").setValue(surname);
    acroForm.getField("email3").setValue(email);
    acroForm.getField("scheinart6").setValue("EntwickelBar");
    
    // pdfForm.save("DummyCertificate" + forename + ".pdf");
    pdfForm.save(outputStream);
    
    pdfForm.close();
  }
  
  /**
   * Setzt beim Schein das aktuelle Datum.
   *
   * @param acroForm Schein-PDF
   */
  private static void setCertificateDate(PDAcroForm acroForm) throws IOException {
    LocalDate currentDate = LocalDate.now();
    int day = currentDate.getDayOfMonth();
    int month = currentDate.getMonthValue();
    int year = currentDate.getYear();
    
    acroForm.getField("date7[day]").setValue(Integer.toString(day));
    acroForm.getField("date7[month]").setValue(Integer.toString(month));
    acroForm.getField("date7[year]").setValue(Integer.toString(year));
  }
}
