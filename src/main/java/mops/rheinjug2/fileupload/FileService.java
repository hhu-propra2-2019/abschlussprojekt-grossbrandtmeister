package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  MinioClient minioClient;

  @Value("${minio.bucket.name}")
  String defaultBucketName;

  @Value("${minio.default.folder}")
  String defaultBaseFolder;

  public FileService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  /**
   * Nimmt ein File entgegen und speichert dieses auf dem Minio-Server.
   *
   * @param file - File aus dem Controller.
   */
  public void uploadFile(MultipartFile file) throws IOException {
    InputStream inputStream = new BufferedInputStream(file.getInputStream());
    try {
      if (!minioClient.bucketExists(defaultBucketName)) {
        minioClient.makeBucket(defaultBucketName);
      }
      minioClient.putObject(defaultBucketName, file.getName(), inputStream,
          file.getSize(), null, null);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
