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

  @Autowired
  private FileService fileService;

  @RequestMapping("/file")
  public String showPage(Model model) {
    return "fileUpload";
  }

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
    try {
      FileService.store(file);
    } catch (Exception e) {
      e.printStackTrace();
      // create log message if something did not go well
    }

    model.addAttribute("msg", "Succesfully uploadad files");
    return "fileUpload";
  }
}