package mops.rheinjug2.creditpoints;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
  
  private final transient String[] qualifiedNames
      = {"Veranstaltung 1", "Veranstaltung 2", "Veranstaltung 3"};
  
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
      
      setTextFields(acroForm, gender, matnr, name);
      setListOfUsedEvents(acroForm, usedEvents);
      setCertificateDate(acroForm);
      
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
  
  private static void setTextFields(final PDAcroForm acroForm,
                                    final String gender,
                                    final String matnr,
                                    final String name) throws IOException {
    acroForm.getField("Vorname, Name").setValue(name);
    acroForm.getField("Text").setValue(setGenderFormOfAddress(gender) + " "
        + name + " - Matrikelnummer " + matnr + " wird hiermit bescheinigt, dass "
        + setGenderPronoun(gender)
        + " an folgenden rheinjug-Veranstaltungen (Java in der Praxis) "
        + "teilgenommen und dazu jeweils eine Zusammenfassung geschrieben hat.");
  }
  
  private void setListOfUsedEvents(final PDAcroForm acroForm,
                                   final List<Event> usedEvents) throws IOException {
    for (int i = 0; i < usedEvents.size(); i++) {
      acroForm.getField(qualifiedNames[i]).setValue("- " + usedEvents.get(i).getTitle());
    }
    final int numberOfPossibleEvents = 3;
    for (int i = usedEvents.size(); i < numberOfPossibleEvents; i++) {
      acroForm.getField(qualifiedNames[i]).setValue("");
    }
  }
  
  private static void setCertificateDate(final PDAcroForm acroForm) throws IOException {
    final LocalDate currentDate = LocalDate.now();
    final String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    acroForm.getField("Datum 1").setValue(formattedDate);
    acroForm.getField("Datum 2").setValue(formattedDate);
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
