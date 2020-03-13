package mops.rheinjug2.fileupload;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

@Service
public class FileService {

  transient MinioClient minioClient;

  @Value("${minio.bucket.name}")
  transient String defaultBucketName;

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
  public void uploadFile(MultipartFile file, String objektname) throws IOException {
    InputStream inputStream = new BufferedInputStream(file.getInputStream());
    try {
      if (!minioClient.bucketExists(defaultBucketName)) {
        minioClient.makeBucket(defaultBucketName);
      }
      minioClient.putObject(defaultBucketName, objektname, inputStream,
          file.getSize(), null, null, file.getContentType());
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      inputStream.close();
    }
  }

  /**
   * Sucht über den Filenamen das Objekt vom MinIO-Server und gibt es als File-Objekt zurück.
   *
   * @param filename Name der Datei
   * @return File
   */
  public File getFile(String filename) throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException,
      InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    minioClient.statObject(defaultBucketName, filename);
    InputStream inputStream = minioClient.getObject(defaultBucketName, filename);
    try {
      File file = new File(filename);
      FileUtils.copyInputStreamToFile(inputStream, file);
      System.out.println(file);
      return file;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      inputStream.close();
    }
  }

  /**
   * Sucht über den Filenamen das Objekt vom MinIO-Server und gibt es als Inputstream zurück.
   *
   * @param filename Name der Datei
   * @return Inputstream
   */
  public InputStream getFileInputStream(String filename)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException,
      InsufficientDataException, InvalidArgumentException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    InputStream inputStream = minioClient.getObject(defaultBucketName, filename);
    return inputStream;
  }

  /**
   * Speichert String in MinIO Server.
   *
   * @param content
   * @param objektname
   * @throws IOException
   */
  public void uploadeContentConvertToMd(String content, String objektname) throws IOException {
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try {
      if (!minioClient.bucketExists(defaultBucketName)) {
        minioClient.makeBucket(defaultBucketName);
      }
      minioClient.putObject(defaultBucketName, objektname, inputStream,
          (long) content.length(), null, null, "text/markdown");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      inputStream.close();
    }
  }
}
