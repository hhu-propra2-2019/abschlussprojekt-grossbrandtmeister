package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class MinioConfig {

  @Bean
  public MinioClient generateMinioClient(
      @Value("${minio.access.name}") final String accessKey,
      @Value("${minio.access.secret}") final String accessSecret,
      @Value("${minio.url}") final String minioUrl) {
    try {
      return new MinioClient(minioUrl, accessKey, accessSecret);
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

}
