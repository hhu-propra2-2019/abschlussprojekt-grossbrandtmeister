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
import mops.rheinjug2.fileupload.FileCheckService;
import mops.rheinjug2.fileupload.FileService;
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
public class FileUploadController {

  transient FileService fileService;

  transient FileCheckService fileCheckService;

  private final transient Counter authenticatedAccess;

  static final String Veranstaltung = "Veranstaltung";


  @Autowired
  public FileUploadController(final FileService fileService, final MeterRegistry registry) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.fileService = fileService;
  }

  @RequestMapping("/uploadfile")
  public String showPage(final Model model) {
    return "fileUpload";
  }


  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/student/reportsubmit")
  public String uploadFile(final KeycloakAuthenticationToken token, final RedirectAttributes attributes,
                           @RequestParam(value = "file") final MultipartFile file) {
    if (fileCheckService.checkIfIsMarkdown(file)) {
      try {
        final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        final String username = principal.getName();
        if (!username.isEmpty()) {
          final String filename = username + "_" + Veranstaltung;
          fileService.uploadFile(file, filename);
          attributes.addFlashAttribute("message", "You successfully uploaded " + filename + '!');
        }
      } catch (final Exception e) {
        log.catching(e);
        attributes.addFlashAttribute("message", "Your file was not able to be uploaded ");
      }
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit";
    //return "report_submit";
  }

  /**
   * Nimmt den Inhalt aus der Textarea-Box und gibt ihn an den FileService weiter
   * um das File zu speichern.
   */
  @PostMapping(path = "/student/summarysubmit")
  public String useForm(final KeycloakAuthenticationToken token, final RedirectAttributes attributes,
                        @RequestParam("Inhalt") final String content) {
    try {
      final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
      final String username = principal.getName();
      if (!username.isEmpty()) {
        final String filename = username + "_" + Veranstaltung;
        fileService.uploadeContentConvertToMd(content, filename);
        attributes.addFlashAttribute("message", "You successfully uploaded the form !");
      }

    } catch (final Exception e) {
      log.catching(e);
      attributes.addFlashAttribute("message", "Your file was not able to be uploaded ");
    }
    authenticatedAccess.increment();
    System.out.println(content);
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
   * Die methode lädt die passende datei aus dem Fileserver herunter.
   */
  @RequestMapping("/download/file")
  @ResponseBody
  public void downloadFilebyToken(final KeycloakAuthenticationToken token,
                                  final HttpServletResponse response)
      throws IOException {

    final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    final String username = principal.getName();
    if (!username.isEmpty()) {
      final String filename = username + "_" + Veranstaltung;
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
   * Die methode lädt die Maarkdown-Vorlage aus dem Fileserver herunter.
   */
  @RequestMapping("/download/presentation")
  @ResponseBody
  public void downloadPResentationforSummary(final KeycloakAuthenticationToken token,
                                             final HttpServletResponse response) throws IOException {

    final String filename = "Vorlage.md";
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
