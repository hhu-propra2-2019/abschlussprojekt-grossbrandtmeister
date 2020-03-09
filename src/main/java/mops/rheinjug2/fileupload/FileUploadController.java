package mops.rheinjug2.fileupload;

import java.util.HashMap;
import java.util.Map;
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

  @Autowired
  public FileUploadController(FileService fileService) {
    this.fileService = fileService;
  }

  @RequestMapping("/file")
  public String showPage(Model model) {
    return "fileUpload";
  }

  @PostMapping("/file")
  public String uploadFile(@RequestParam(value = "file") MultipartFile file, Model model) {
    try {
      fileService.uploadFile(file.getOriginalFilename(), file.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Map<String, String> result = new HashMap<>();
    result.put("key", file.getOriginalFilename());
    return "fileUpload";
  }
}