package mops.rheinjug2.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Service;

@Service
public class CertificateService {
  
  transient PDDocument pdfForm;
  
  /**
   * Füllt den Schein mit den Informationen des/der Studenten/Studentin.
   *
   * @param outputStream zum Speichern der befüllten PdfForm
   * @param forename     des Studenten/der Studentin
   * @param surname      des Studenten/der Studentin
   * @param email        des Studenten/der Studentin
   */
  public void createCertificatePdf(ByteArrayOutputStream outputStream,
                                   String forename, String surname, String email) {
    File pdf = new File("./DummyCertificate.pdf");
    try {
      pdfForm = PDDocument.load(pdf);
      
      PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
      PDAcroForm acroForm = docCatalog.getAcroForm();
      
      setCertificateDate(acroForm);
      acroForm.getField("name2[first]").setValue(forename);
      acroForm.getField("name2[last]").setValue(surname);
      acroForm.getField("email3").setValue(email);
      acroForm.getField("scheinart6").setValue("EntwickelBar");
      
      // pdfForm.save("DummyCertificate" + forename + ".pdf");
      pdfForm.save(outputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        pdfForm.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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
