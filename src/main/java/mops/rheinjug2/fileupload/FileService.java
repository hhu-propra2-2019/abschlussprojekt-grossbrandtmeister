package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  final MinioClient minioClient;

  public FileService() throws InvalidPortException, InvalidEndpointException {
    final String accessKey = "EY9QX8JV680F69KF1RZJ";
    final String secretKey = "eobizOIujzVW5+y4Z6oYP2OsTAgmpf4imzWfeTby";
    minioClient = new MinioClient("http://127.0.0.1:9000", accessKey,
        secretKey);
  }


  public void store(MultipartFile file) throws Exception {

  }

}
