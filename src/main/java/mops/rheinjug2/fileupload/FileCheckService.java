package mops.rheinjug2.fileupload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileCheckService {

  /**
   * überprüft ob ein File das Format Markdown enthält.
   *
   * @param file zu überprüfende Datei
   * @return boolean
   */
  public static boolean checkIfIsMarkdown(final MultipartFile file) {
    if (!file.isEmpty()) {
      if (file.getOriginalFilename().endsWith(".md")) {
        return true;
      }
    }
    return false;
  }
}
