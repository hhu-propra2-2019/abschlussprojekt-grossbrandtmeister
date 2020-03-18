package mops.rheinjug2.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Service;

@Service
@Log4j2
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
  public void createCertificatePdf(final ByteArrayOutputStream outputStream,
                                   final String forename, final String surname, final String email) {
    final File pdf = new File("./DummyCertificate.pdf");
    try {
      pdfForm = PDDocument.load(pdf);

      final PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
      final PDAcroForm acroForm = docCatalog.getAcroForm();

      setCertificateDate(acroForm);
      acroForm.getField("name2[first]").setValue(forename);
      acroForm.getField("name2[last]").setValue(surname);
      acroForm.getField("email3").setValue(email);
      acroForm.getField("scheinart6").setValue("EntwickelBar");

      // pdfForm.save("DummyCertificate" + forename + ".pdf");
      pdfForm.save(outputStream);
    } catch (final IOException e) {
      log.catching(e);
    } finally {
      try {
        pdfForm.close();
      } catch (final IOException e) {
        log.catching(e);
      }
    }
  }

  /**
   * Setzt beim Schein das aktuelle Datum.
   *
   * @param acroForm Schein-PDF
   */
  private static void setCertificateDate(final PDAcroForm acroForm) throws IOException {
    final LocalDate currentDate = LocalDate.now();
    final int day = currentDate.getDayOfMonth();
    final int month = currentDate.getMonthValue();
    final int year = currentDate.getYear();

    acroForm.getField("date7[day]").setValue(Integer.toString(day));
    acroForm.getField("date7[month]").setValue(Integer.toString(month));
    acroForm.getField("date7[year]").setValue(Integer.toString(year));
  }
}
