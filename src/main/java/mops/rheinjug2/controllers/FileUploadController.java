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
  public FileUploadController(FileService fileService,
                              MeterRegistry registry, ModelService modelService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.fileService = fileService;
    this.modelService = modelService;
  }

  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/student/reportsubmit")
  public String uploadFile(KeycloakAuthenticationToken token,
                           RedirectAttributes attributes,
                           @RequestParam(value = "file") MultipartFile file,
                           Long eventId) {
    if (eventId == null) {
      attributes.addFlashAttribute("message", "You did not choose an event."
          + "Go to your personal event side and choose for which event "
          + "you want to handle your summary in");
      return "redirect:/rheinjug2/student/reportsubmit";
    }
    if (fileCheckService.checkIfIsMarkdown(file)) {
      try {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        String username = principal.getName();
        if (!username.isEmpty()) {
          modelService.submitSummary(username, eventId);
          String filename = username + "_" + eventId;
          fileService.uploadFile(file, filename);
          attributes.addFlashAttribute("message",
              "You successfully uploaded " + filename + '!');
        }
      } catch (Exception e) {
        log.catching(e);
        attributes.addFlashAttribute("message",
            "Your file was not able to be uploaded ");
      }
    } else {
      attributes.addFlashAttribute("message",
          "Your file has the wrong format. It needs to be a markdown file!");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit?eventId=" + eventId;
  }

  /**
   * Nimmt das Summary mit Inhalt und gibt den Inhalt an den FileService weiter
   * um das File zu speichern.
   */
  @PostMapping(path = "/student/summarysubmit")
  public String useForm(KeycloakAuthenticationToken token,
                        RedirectAttributes attributes, Summary summary,
                        Long eventId) {
    if (eventId == null) {
      attributes.addFlashAttribute("message", "You did not choose an event."
          + "Go to your personal event side and choose for which event "
          + "you want to handle your summary in");
      return "redirect:/rheinjug2/student/reportsubmit";
    }
    try {
      KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
      String username = principal.getName();
      if (!username.isEmpty()) {
        modelService.submitSummary(username, eventId);
        String filename = username + "_" + eventId;
        fileService.uploadContentConvertToMd(summary.getContent(), filename);
        attributes.addFlashAttribute("message",
            "You successfully uploaded the form !");
      }

    } catch (Exception e) {
      log.catching(e);
      attributes.addFlashAttribute("message",
          "Your file was not able to be uploaded ");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit?eventId=" + eventId;
  }


  /**
   * Die Methode lädt die passende Datei des Studenten aus dem Fileserver herunter.
   */
  @RequestMapping("/download/file")
  @ResponseBody
  public void downloadFilebyToken(KeycloakAuthenticationToken token,
                                  HttpServletResponse response, Long eventId)
      throws IOException {


    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String username = principal.getName();
    if (!username.isEmpty()) {
      String filename = username + "_" + eventId;
      try (InputStream inputStream = fileService.getFileInputStream(filename)) {
        response.addHeader("Content-disposition", "attachment;filename=" + filename + ".md");
        response.setContentType(URLConnection.guessContentTypeFromName(filename));
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
      } catch (Exception e) {
        log.catching(e);
      }
    } else {
      response.sendError(404);
    }

    authenticatedAccess.increment();
  }

  /**
   * Die Methode lädt die Markdown-Vorlage aus dem Fileserver herunter.
   */
  @RequestMapping("/download/presentation")
  @ResponseBody
  public void downloadPResentationforSummary(KeycloakAuthenticationToken token,
                                             HttpServletResponse response)
      throws IOException {

    final String filename = "VorlageZusammenfassung.md";
    try (InputStream inputStream = fileService.getFileInputStream(filename)) {
      response.addHeader("Content-disposition", "attachment;filename=" + filename);
      response.setContentType(URLConnection.guessContentTypeFromName(filename));
      IOUtils.copy(inputStream, response.getOutputStream());
      response.flushBuffer();
    } catch (Exception e) {
      response.sendError(404, "File not found");
    }
    authenticatedAccess.increment();
  }
}