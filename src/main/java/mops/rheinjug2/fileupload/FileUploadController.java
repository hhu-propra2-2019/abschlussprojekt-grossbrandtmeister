package mops.rheinjug2.fileupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/rheinjug2")
public class FileUploadController {

  FileService fileService;

  FileCheckService fileCheckService;

  @Autowired
  public FileUploadController(final FileService fileService) {
    this.fileService = fileService;
  }

  @RequestMapping("/file")
  public String showPage(final Model model) {
    return "fileUpload";
  }


  /**
   * Gibt das File an den FileService weiter um das File zu speichern.
   */
  @PostMapping(path = "/file")
  public String uploadFile(@RequestParam(value = "file") final MultipartFile file,
                           final Model model) {
    if (fileCheckService.checkIfIsAdoc(file)) {
      try {
        fileService.uploadFile(file);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return "fileUpload";
  }

}