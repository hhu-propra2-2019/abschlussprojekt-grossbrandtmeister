package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  @Autowired
  MinioClient minioClient;

  @Value("${minio.buckek.name}")
  String defaultBucketName;

  @Value("${minio.default.folder}")
  String defaultBaseFolder;

  public static void store(MultipartFile file) throws Exception {
    //file.getOriginalFilename(), file.getBytes()
  }

}
