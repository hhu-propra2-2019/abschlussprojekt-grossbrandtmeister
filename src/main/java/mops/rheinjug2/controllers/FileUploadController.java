package mops.rheinjug2.controllers;

import java.util.HashMap;
import java.util.Map;
import mops.rheinjug2.fileupload.FileService;
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

  @PostMapping(path = "/upload", consumes = "text/plain")
  public Map<String, String> uploadFile(@RequestParam(value = "file") MultipartFile file, Model model) {
    try {
      FileService.store(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Map<String, String> result = new HashMap<>();
    result.put("key", file.getOriginalFilename());
    return result;

    //return "fileUpload";
  }
}