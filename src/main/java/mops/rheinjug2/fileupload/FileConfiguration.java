package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileConfiguration {
  /**
   * Erstellen des MinioClients.
   */
  @Bean
  public MinioClient generateMinioClient(
      @Value("${minio.access.name}") String accessKey,
      @Value("${minio.access.secret}") String accessSecret,
      @Value("${minio.url}") String minioUrl) {
    try {
      return new MinioClient(minioUrl, accessKey, accessSecret);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}