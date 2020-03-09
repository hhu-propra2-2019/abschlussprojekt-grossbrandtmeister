package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import java.io.File;
import java.io.FileOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

  MinioClient minioClient;

  @Value("${minio.buckek.name}")
  String defaultBucketName;

  @Value("${minio.default.folder}")
  String defaultBaseFolder;

  @Autowired
  public FileService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public void uploadFile(String name, byte[] content) {
    File file = new File("/tmp/" + name);
    file.canWrite();
    file.canRead();
    try {
      FileOutputStream iofs = new FileOutputStream(file);
      iofs.write(content);
      minioClient.putObject(defaultBucketName, defaultBaseFolder + name, file.getAbsolutePath());
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
