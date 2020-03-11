package mops.rheinjug2.fileupload;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

@Controller
@RequestMapping("/rheinjug2")
public class FileUploadController {

  FileService fileService;

  FileCheckService fileCheckService;

  @Autowired
  public FileUploadController(FileService fileService) {
    this.fileService = fileService;
  }

  @RequestMapping("/file")
  public String showPage(Model model) {
    return "fileUpload";
  }


  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/file")
  public String uploadFile(@RequestParam(value = "file") MultipartFile file,
                           Model model) {
    if (fileCheckService.checkIfIsAdoc(file)) {
      try {
        final String filename = "documentation";
        fileService.uploadFile(file, filename);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return "fileUpload";
  }

  @RequestMapping("/download")
  public String downloadFile(Model model) throws IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException, InvalidResponseException, ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException, InternalException, RegionConflictException {
    final String filename = "documentation";
    File file = fileService.getFile(filename);
    model.addAttribute("file", file);
    return "download";
  }

  @RequestMapping("/download/file/{filename}")
  @ResponseBody
  public void downloadFile(@PathVariable("filename") String object, HttpServletResponse response) throws IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException, InvalidResponseException, ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException, InternalException, RegionConflictException {
    InputStream inputStream = fileService.getFileInputStream(object);

    response.addHeader("Content-disposition", "attachment;filename=" + object + ".adoc");
    response.setContentType(URLConnection.guessContentTypeFromName(object));

    IOUtils.copy(inputStream, response.getOutputStream());
    response.flushBuffer();

    inputStream.close();
  }

}

