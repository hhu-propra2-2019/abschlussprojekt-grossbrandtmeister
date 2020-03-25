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
  public static boolean isMarkdown(final MultipartFile file) {
    if (!file.isEmpty()) {
      return file.getOriginalFilename().endsWith(".md");
    }
    return false;
  }
}
