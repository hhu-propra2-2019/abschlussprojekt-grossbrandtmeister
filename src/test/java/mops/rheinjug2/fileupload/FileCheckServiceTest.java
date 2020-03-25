package mops.rheinjug2.fileupload;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileCheckServiceTest {

  @Test
  void checkIfIsMarkdown() {
    final MultipartFile testFile = new MockMultipartFile("file.md",
        "file.md", "text/plain",
        "testdata".getBytes(StandardCharsets.UTF_8));

    assertTrue(FileCheckService.isMarkdown(testFile));
  }
}