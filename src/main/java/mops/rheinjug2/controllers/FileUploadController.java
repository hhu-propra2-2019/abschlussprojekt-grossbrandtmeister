package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.fileupload.FileCheckService;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import mops.rheinjug2.services.ModelService;
import org.apache.commons.io.IOUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2")
@Log4j2
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class FileUploadController {

  transient FileService fileService;
  transient FileCheckService fileCheckService;
  transient ModelService modelService;

  private final transient Counter authenticatedAccess;

  static final String Veranstaltung = "Veranstaltung";

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @Autowired
  public FileUploadController(final FileService fileService,
                              final MeterRegistry registry, final ModelService modelService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.fileService = fileService;
    this.modelService = modelService;
  }

  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/student/reportsubmit")
  public String uploadFile(final KeycloakAuthenticationToken token,
                           final RedirectAttributes attributes,
                           @RequestParam(value = "file") final MultipartFile file,
                           final Long eventId,
                           @RequestParam(value = "publish2",
                               required = false) final String publish) {
    if (eventId == null) {
      attributes.addFlashAttribute("message", "Du hast kein Event "
          + "ausgewählt, für das du eine Zusammenfassung abgeben kannst. "
          + "Gehe zu meinen Veranstaltungen und wähle eine aus.");
      return "redirect:/rheinjug2/student/reportsubmit";
    }
    if (fileCheckService.isMarkdown(file)) {
      try {
        final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        final String username = principal.getName();
        if (!username.isEmpty()) {
          modelService.submitSummary(username, eventId);
          final String filename = username + "_" + eventId;
          fileService.uploadFile(file, filename);
          attributes.addFlashAttribute("message",
              "Deine Datei wurde unter" + filename + "erfolgreich hochgeladen!");
        }
        if (publish != null) {
          modelService.addPublishingIsPossible(username, eventId);
        }
      } catch (final Exception e) {
        log.catching(e);
        attributes.addFlashAttribute("message",
            ""
                + "Deine Datei konnte nicht hochgeladen werden. Versuche es später noch mal.");
      }
    } else {
      attributes.addFlashAttribute("message",
          "Deine Datei konnte nicht hochgeladen werden, "
              + "da es sich nicht um ein Markdownfile (.md) handelt! "
              + "Schreibe deine Zusammenfassung bitte in Markdown");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit?eventId=" + eventId;
  }

  /**
   * Nimmt das Summary mit Inhalt und gibt den Inhalt an den FileService weiter
   * um das File zu speichern.
   */
  @PostMapping(path = "/student/summarysubmit")

  public String useForm(final KeycloakAuthenticationToken token,
                        final RedirectAttributes attributes, final Summary summary,
                        final Long eventId,
                        @RequestParam(value = "publish1", required = false) final String publish) {
    if (eventId == null) {
      attributes.addFlashAttribute("message", "Du hast kein Event "
          + "ausgewählt, für das du eine Zusammenfassung abgeben kannst."
          + " Gehe zu meinen Veranstaltungen und wähle eine aus.");
      return "redirect:/rheinjug2/student/reportsubmit";
    }
    try {
      final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
      final String username = principal.getName();
      if (!username.isEmpty()) {
        modelService.submitSummary(username, eventId);
        final String filename = username + "_" + eventId;
        fileService.uploadContentConvertToMd(summary.getContent(), filename);
        attributes.addFlashAttribute("message",
            "Du hast die Datei erfolgreich hochgeladen!");
      }
      if (publish != null) {
        modelService.addPublishingIsPossible(username, eventId);
      }
    } catch (final Exception e) {
      log.catching(e);
      attributes.addFlashAttribute("message",
          "Deine Datei konnte nicht hochgeladen werden. "
              + "Versuche es später noch mal.");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit?eventId=" + eventId;
  }


  /**
   * Die Methode lädt die passende Datei des Studenten aus dem Fileserver herunter.
   */
  @RequestMapping("/download/file")
  @ResponseBody
  public void downloadFileByToken(final KeycloakAuthenticationToken token,
                                  final HttpServletResponse response, final Long eventId)
      throws IOException {

    final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    final String username = principal.getName();

    if (!username.isEmpty()) {
      final String filename = username + "_" + eventId;
      try (final InputStream inputStream = fileService.getFileInputStream(filename)) {
        response.addHeader("Content-disposition", "attachment;filename=" + filename + ".md");
        response.setContentType(URLConnection.guessContentTypeFromName(filename));
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
      } catch (final Exception e) {
        response.sendError(404, "Deine Datei wurde nicht gefunden!"
            + " Du hast noch keine Zusammenfassung abgegeben für diese Veranstaltung");
      }
    }
    authenticatedAccess.increment();
  }

  /**
   * Die Methode lädt die Markdown-Vorlage aus dem Fileserver herunter.
   */
  @RequestMapping("/download/presentation")
  @ResponseBody
  public void downloadPresentationForSummary(final KeycloakAuthenticationToken token,
                                             final HttpServletResponse response)
      throws IOException {

    final String filename = "VorlageZusammenfassung.md";
    try (final InputStream inputStream = fileService.getFileInputStream(filename)) {
      response.addHeader("Content-disposition", "attachment;filename=" + filename);
      response.setContentType("text/plain");
      IOUtils.copy(inputStream, response.getOutputStream());
      response.flushBuffer();
    } catch (final Exception e) {
      response.sendError(404, "Momentan ist leider keine Vorlage vorhanden. "
          + "Schau dir als Beispiele auf markdown.de an.");
    }
    authenticatedAccess.increment();
  }
}