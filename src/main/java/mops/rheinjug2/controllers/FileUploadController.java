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
public class FileUploadController {

  transient FileService fileService;

  transient FileCheckService fileCheckService;

  private final transient Counter authenticatedAccess;

  static final String Veranstaltung = "Veranstaltung";


  @Autowired
  public FileUploadController(FileService fileService, MeterRegistry registry) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.fileService = fileService;
  }

  @RequestMapping("/uploadfile")
  public String showPage(Model model) {
    return "fileUpload";
  }


  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/student/reportsubmit")
  public String uploadFile(KeycloakAuthenticationToken token, RedirectAttributes attributes,
                           @RequestParam(value = "file") MultipartFile file) {
    if (fileCheckService.checkIfIsMarkdown(file)) {
      try {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        String username = principal.getName();
        if (!username.isEmpty()) {
          String filename = username + "_" + Veranstaltung;
          fileService.uploadFile(file, filename);
          attributes.addFlashAttribute("message", "You successfully uploaded " + filename + '!');
        }

      } catch (Exception e) {
        e.printStackTrace();
        attributes.addFlashAttribute("message", "Your file was not able to be uploaded ");
      }
    }
    authenticatedAccess.increment();
    return "redirect:/rheinjug2/student/reportsubmit";
    //return "report_submit";
  }

  /**
   * füge das File auf einer eigenen Website hinzu. Evtl in späteren versionen zu ändern.
   *
   * @param model thymeleaf.
   * @return String
   */
  @RequestMapping("/download")
  public String downloadFile(KeycloakAuthenticationToken token, Model model)
      throws IOException, XmlPullParserException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException,
      InvalidResponseException, ErrorResponseException, NoResponseException,
      InvalidBucketNameException, InsufficientDataException, InternalException {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String username = principal.getName();
    if (!username.isEmpty()) {
      String filename = username + "_" + Veranstaltung;
      File file = fileService.getFile(filename);
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
  public void downloadFilebyToken(KeycloakAuthenticationToken token,
                                  HttpServletResponse response)
      throws IOException, XmlPullParserException, NoSuchAlgorithmException,
      InvalidKeyException, InvalidArgumentException, InvalidResponseException,
      ErrorResponseException, NoResponseException, InvalidBucketNameException,
      InsufficientDataException, InternalException {

    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String username = principal.getName();
    if (!username.isEmpty()) {
      String filename = username + "_" + Veranstaltung;
      try (InputStream inputStream = fileService.getFileInputStream(filename)) {
        response.addHeader("Content-disposition", "attachment;filename=" + filename + ".md");
        response.setContentType(URLConnection.guessContentTypeFromName(filename));
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
      }

    } else {
      response.sendError(404, "File not found");
    }

    authenticatedAccess.increment();
  }

}

