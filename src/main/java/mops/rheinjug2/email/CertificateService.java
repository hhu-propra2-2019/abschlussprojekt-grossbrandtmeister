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
  
  private final transient int numberOfRequiredEntwickelbarEvents = 1;
  private final transient int numberOfRequiredEveningEvents = 3;
  
  private transient PDDocument pdfForm;
  private transient ByteArrayOutputStream outputStream;
  
  /**
   * Füllt den Schein mit den Informationen des/der Studenten/Studentin.
   *
   * @param name       des Studenten/der Studentin
   * @param gender     Geschlecht des Studenten/der Studentin
   * @param matnr      Matrikelnummer des Studenten/der Studentin
   * @param usedEvents Liste der Events die für das PDF verbraucht werden.
   */
  public byte[] createCertificatePdf(final String name, final String gender,
                                     final String matnr, final List<Event> usedEvents) {
    final File pdf = new File("./rheinjug_schein.pdf");
    try {
      pdfForm = PDDocument.load(pdf);
      
      final PDDocumentCatalog docCatalog = pdfForm.getDocumentCatalog();
      final PDAcroForm acroForm = docCatalog.getAcroForm();
      
      acroForm.getField("Vorname, Name").setValue(name);
      
      acroForm.getField("Text").setValue(setGenderFormOfAddress(gender) + " "
          + name + " - Matrikelnummer " + matnr + " wird hiermit bescheinigt, dass "
          + setGenderPronoun(gender)
          + " an folgenden rheinjug-Veranstaltungen (Java in der Praxis) "
          + "teilgenommen und dazu jeweils eine Zusammenfassung geschrieben hat.");
      
      
      if (usedEvents.size() == numberOfRequiredEveningEvents) {
        acroForm.getField("Veranstaltung 1").setValue(usedEvents.get(0).getTitle());
        acroForm.getField("Veranstaltung 2").setValue(usedEvents.get(1).getTitle());
        acroForm.getField("Veranstaltung 3").setValue(usedEvents.get(2).getTitle());
      } else if (usedEvents.size() == numberOfRequiredEntwickelbarEvents) {
        acroForm.getField("Veranstaltung 1").setValue(usedEvents.get(0).getTitle());
        acroForm.getField("Veranstaltung 2").setValue("");
        acroForm.getField("Veranstaltung 3").setValue("");
      } else {
        return null;
      }
      
      acroForm.getField("Datum 1").setValue(setCertificateDate());
      acroForm.getField("Datum 2").setValue(setCertificateDate());
      
      outputStream = new ByteArrayOutputStream();
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
    return outputStream.toByteArray();
  }
  
  /**
   * Setzt beim Schein das aktuelle Datum.
   */
  private static String setCertificateDate() {
    final LocalDate currentDate = LocalDate.now();
    final int day = currentDate.getDayOfMonth();
    final int month = currentDate.getMonthValue();
    final int year = currentDate.getYear();
    return day + "." + month + "." + year;
  }
  
  private static String setGenderFormOfAddress(final String gender) {
    switch (gender) {
      case "male":
        return "Herr";
      case "female":
        return "Frau";
      default:
        return "";
    }
  }
  
  private static String setGenderPronoun(final String gender) {
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
