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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

@Service
@Log4j2
public class FileService {

  transient MinioClient minioClient;

  final transient String defaultBucketName;

  final transient String defaultBaseFolder;


  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public FileService(final MinioClient minioClient,
                     @Value("${minio.bucket.name}") final String defaultBucketName,
                     @Value("${minio.default.folder}") final String defaultBaseFolder) {
    this.minioClient = minioClient;
    this.defaultBucketName = defaultBucketName;
    this.defaultBaseFolder = defaultBaseFolder;
  }

  /**
   * Nimmt ein File entgegen und speichert dieses auf dem Minio-Server.
   *
   * @param file - File aus dem Controller.
   */
  public void uploadFile(final MultipartFile file, final String objektname) throws IOException {
    final InputStream inputStream = new BufferedInputStream(file.getInputStream());
    try {
      if (!minioClient.bucketExists(defaultBucketName)) {
        minioClient.makeBucket(defaultBucketName);
      }
      minioClient.putObject(defaultBucketName, objektname, inputStream,
          file.getSize(), null, null, file.getContentType());
    } catch (final Exception e) {
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
  public File getFile(final String filename) throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException,
      InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    minioClient.statObject(defaultBucketName, filename);
    final InputStream inputStream = minioClient.getObject(defaultBucketName, filename);
    try {
      log.info("Reading file " + filename);
      final File file = new File(filename);
      FileUtils.copyInputStreamToFile(inputStream, file);
      log.info("File with length " + file.length() + " read.");
      return file;
    } catch (final Exception e) {
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
  public InputStream getFileInputStream(final String filename)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException,
      InsufficientDataException, InvalidArgumentException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final InputStream inputStream = minioClient.getObject(defaultBucketName, filename);
    return inputStream;
  }
}
