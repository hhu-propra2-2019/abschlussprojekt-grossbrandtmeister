package mops.rheinjug2.fileupload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileCheckService {

  static boolean checkIfIsAdoc(MultipartFile file) {
    if (!file.isEmpty()) {
      if (file.getOriginalFilename().endsWith(".md")) {
        return true;
      }
    }
    return false;
  }
}
