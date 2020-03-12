package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileConfiguration {
  @Value("${minio.access.name}")
  transient String accessKey;
  @Value("${minio.access.secret}")
  transient String accessSecret;
  @Value("${minio.url}")
  transient String minioUrl;

  /**
   * Erstellen des MinioClients.
   */
  @Bean
  public MinioClient generateMinioClient() {
    try {
      final MinioClient client = new MinioClient(minioUrl, accessKey, accessSecret);
      return client;
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}