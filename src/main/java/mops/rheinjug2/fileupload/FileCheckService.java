package mops.rheinjug2.fileupload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileCheckService {

  static boolean checkIfIsAdoc(final MultipartFile file) {
    if (!file.isEmpty()) {
      if (file.getOriginalFilename().endsWith(".adoc")) {
        return true;
      }
    }
    return false;
  }
}
