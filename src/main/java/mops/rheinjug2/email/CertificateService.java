package mops.rheinjug2.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.entities.Event;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CertificateService {
  
  private final transient int entwickelbarEventsAnzahl = 1;
  private final transient int normalEventsAnzahl = 3;
  
  transient PDDocument pdfForm;
  
  /**
   * Füllt den Schein mit den Informationen des/der Studenten/Studentin.
   *
   * @param outputStream zum Speichern der befüllten PdfForm
   * @param name         des Studenten/der Studentin
   * @param gender       Geschlecht des Studenten/der Studentin
   * @param matnr        Matrikelnummer des Studenten/der Studentin
   * @param usedEvents   Liste der Events die für das PDF verbraucht werden.
   */
  public void createCertificatePdf(ByteArrayOutputStream outputStream,
                                   String name, String gender,
                                   String matnr, List<Event> usedEvents) {
    File pdf = new File("./rheinjug_schein.pdf");
    try {
      pdfForm = PDDocument.load(pdf);
      
      PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
      PDAcroForm acroForm = docCatalog.getAcroForm();
      
      acroForm.getField("Vorname, Name").setValue(name);
      acroForm.getField("Anrede, Vorname, Name")
          .setValue(setGenderFormOfAddress(gender) + " " + name);
      acroForm.getField("Matrikelnummer").setValue(matnr);
      acroForm.getField("er,sie").setValue(setGenderPronoun(gender));
      
      if (usedEvents.size() == normalEventsAnzahl) {
        acroForm.getField("Veranstaltung 1").setValue(usedEvents.get(0).getTitle());
        acroForm.getField("Veranstaltung 2").setValue(usedEvents.get(1).getTitle());
        acroForm.getField("Veranstaltung 3").setValue(usedEvents.get(2).getTitle());
      } else if (usedEvents.size() == entwickelbarEventsAnzahl) {
        acroForm.getField("Veranstaltung 1").setValue(usedEvents.get(0).getTitle());
        acroForm.getField("Veranstaltung 2").setValue("");
        acroForm.getField("Veranstaltung 3").setValue("");
      } else {
        return;
      }
      
      acroForm.getField("Datum 1").setValue(setCertificateDate());
      acroForm.getField("Datum 2").setValue(setCertificateDate());
      
      // pdfForm.save("DummyCertificate" + forename + ".pdf");
      pdfForm.save(outputStream);
    } catch (IOException e) {
      log.catching(e);
    } finally {
      try {
        pdfForm.close();
      } catch (IOException e) {
        log.catching(e);
      }
    }
  }
  
  /**
   * Setzt beim Schein das aktuelle Datum.
   */
  private static String setCertificateDate() {
    LocalDate currentDate = LocalDate.now();
    int day = currentDate.getDayOfMonth();
    int month = currentDate.getMonthValue();
    int year = currentDate.getYear();
    return day + "." + month + "." + year;
  }
  
  private static String setGenderFormOfAddress(String gender) {
    switch (gender) {
      case "male":
        return "Herr";
      case "female":
        return "Frau";
      default:
        return "";
    }
  }
  
  private static String setGenderPronoun(String gender) {
    switch (gender) {
      case "male":
        return "er";
      case "female":
        return "sie";
      default:
        return "";
    }
  }
}
