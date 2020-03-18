package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.fileupload.FileCheckService;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import org.apache.commons.io.IOUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xmlpull.v1.XmlPullParserException;

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

  @Autowired
  public FileUploadController(final FileService fileService, final ModelService modelService, final MeterRegistry registry) {
    this.modelService = modelService;
    authenticatedAccess = registry.counter("access.authenticated");
    this.fileService = fileService;
  }

  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/student/reportsubmit")
  public String uploadFile(final KeycloakAuthenticationToken token,
                           final RedirectAttributes attributes,
                           @RequestParam(value = "file") final MultipartFile file,
                           final Long eventId) {
    if (fileCheckService.checkIfIsMarkdown(file)) {
      try {
        final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        final String username = principal.getName();
        if (!username.isEmpty()) {
          final String filename = username + "_" + eventId;
          fileService.uploadFile(file, filename);
          attributes.addFlashAttribute("message",
              "You successfully uploaded " + filename + '!');
        }
      } catch (final Exception e) {
        log.catching(e);
        attributes.addFlashAttribute("message",
            "Your file was not able to be uploaded ");
      }
    } else {
      attributes.addFlashAttribute("message",
          "Your file has the wrong format. It needs to be a markdown file!");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit";
  }

  /**
   * Nimmt das Summary mit Inhalt und gibt den Inhalt an den FileService weiter
   * um das File zu speichern.
   */
  @PostMapping(path = "/student/summarysubmit")
  public String useForm(final KeycloakAuthenticationToken token,
                        final RedirectAttributes attributes, final Summary summary,
                        final Long eventId) {
    try {
      final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
      final String username = principal.getName();
      if (!username.isEmpty()) {
        System.out.println(summary.getEventId());
        System.out.println(eventId + "die ohne das objekt");
        final String filename = username + "_" + eventId;
        fileService.uploadContentConvertToMd(summary.getContent(), filename);
        attributes.addFlashAttribute("message",
            "You successfully uploaded the form !");
      }

    } catch (final Exception e) {
      log.catching(e);
      attributes.addFlashAttribute("message",
          "Your file was not able to be uploaded ");
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit";
  }

  /**
   * füge das File auf einer eigenen Website hinzu. Evtl in späteren versionen zu ändern.
   *
   * @param model thymeleaf.
   * @return String
   */
  @RequestMapping("/download")
  public String downloadFile(final KeycloakAuthenticationToken token, final Model model)
      throws IOException, XmlPullParserException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException,
      InvalidResponseException, ErrorResponseException, NoResponseException,
      InvalidBucketNameException, InsufficientDataException, InternalException {
    final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    final String username = principal.getName();
    if (!username.isEmpty()) {
      final String filename = username + "_" + Veranstaltung;
      final File file = fileService.getFile(filename);
      model.addAttribute("file", file);
    } else {
      model.addAttribute("file", null);
    }
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "download";
  }

  /**
   * Die Methode lädt die passende Datei des Studenten aus dem Fileserver herunter.
   */
  @RequestMapping("/download/file")
  @ResponseBody
  public void downloadFilebyToken(final KeycloakAuthenticationToken token,
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
        log.catching(e);
      }
    } else {
      response.sendError(403);
    }

    authenticatedAccess.increment();
  }

  /**
   * Die Methode lädt die Markdown-Vorlage aus dem Fileserver herunter.
   */
  @RequestMapping("/download/presentation")
  @ResponseBody
  public void downloadPResentationforSummary(final KeycloakAuthenticationToken token,
                                             final HttpServletResponse response)
      throws IOException {

    final String filename = "VorlageZusammenfassung.md";
    try (final InputStream inputStream = fileService.getFileInputStream(filename)) {
      response.addHeader("Content-disposition", "attachment;filename=" + filename);
      response.setContentType(URLConnection.guessContentTypeFromName(filename));
      IOUtils.copy(inputStream, response.getOutputStream());
      response.flushBuffer();
    } catch (final Exception e) {
      response.sendError(404, "File not found");
    }
    authenticatedAccess.increment();
  }
}