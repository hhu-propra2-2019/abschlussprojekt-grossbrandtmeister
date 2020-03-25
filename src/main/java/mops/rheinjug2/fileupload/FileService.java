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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
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
  FileService(final MinioClient minioClient,
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
   * @param filename Name der Datei.
   * @return Inputstream.
   */
  public InputStream getFileInputStream(final String filename)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException,
      InsufficientDataException, InvalidArgumentException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final InputStream inputStream = minioClient.getObject(defaultBucketName, filename);
    return inputStream;
  }

  /**
   * Speichert String in MinIO Server.
   *
   * @param content  Inhalt des Files.
   * @param filename Name des gesuchten File.
   */
  public void uploadContentConvertToMd(final String content, final String filename)
      throws IOException {
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8));
    try {
      if (!minioClient.bucketExists(defaultBucketName)) {
        minioClient.makeBucket(defaultBucketName);
      }
      minioClient.putObject(defaultBucketName, filename, inputStream,
          null, null, null, "text/plain");
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      inputStream.close();
    }
  }

  /**
   * Gebt den Inhalt des gesucheten Files als String zurück.
   *
   * @param filename Name des gesuchten Files.
   * @return String filecontent.
   */
  public String getContentOfFileAsString(final String filename) throws IOException,
      XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException,
      InvalidArgumentException, InvalidResponseException, ErrorResponseException,
      NoResponseException, InvalidBucketNameException, InsufficientDataException,
      InternalException {
    final InputStream inputStream = getFileInputStream(filename);
    try {
      final String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
      return content;
    } catch (final Exception e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      inputStream.close();
    }
  }

  /**
   * Gives the content of a markdown file back as a HTML-file in format of a String.
   *
   * @param summaryStudent filename of the searched summary.
   * @return String as HTML file gerenders.
   */
  public String getFileAsHtmlString(final String summaryStudent) {
    try {
      final String content = getContentOfFileAsString(summaryStudent);
      final Parser parser = Parser.builder().build();
      final Node document = parser.parse(content);
      final HtmlRenderer renderer = HtmlRenderer.builder().build();
      return renderer.render(document);
    } catch (final Exception e) {
      log.catching(e);
    }
    return "File Not Found";
  }
}
