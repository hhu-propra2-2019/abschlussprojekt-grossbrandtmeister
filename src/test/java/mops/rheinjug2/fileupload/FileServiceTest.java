package mops.rheinjug2.fileupload;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.xmlpull.v1.XmlPullParserException;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {FileConfiguration.class})
@Testcontainers
class FileServiceTest {

  private static final String ACCESS_KEY = "minio";
  private static final String SECRET_KEY = "minio123";
  private static String minioServerUrl;

  private transient MinioClient minioClient;

  private static final String BUCKETMANE = "grossbrandtmeiser";

  @Container
  static GenericContainer minioServer = new GenericContainer<>("minio/minio")
      .withEnv("MINIO_ACCESS_KEY", ACCESS_KEY)
      .withEnv("MINIO_SECRET_KEY", SECRET_KEY)
      .withCommand("server /data")
      .withExposedPorts(9000);

  private transient FileService fileService;

  /**
   * Startet den MinioServer und initialisiert den Fileservice  und den
   * MinioClient mit Defaultbucketname grossbrandtmeister".
   */
  @BeforeAll
  void setUp() throws InvalidPortException, InvalidEndpointException, IOException,
      InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
      InvalidResponseException, ErrorResponseException, NoResponseException,
      InvalidBucketNameException, XmlPullParserException, InternalException,
      RegionConflictException {
    minioServer.start();
    final Integer mappedPort = minioServer.getMappedPort(9000);
    exposeHostPorts(mappedPort);
    minioServerUrl = String.format("http://%s:%s", minioServer.getContainerIpAddress(), mappedPort);


    minioClient = new MinioClient(minioServerUrl, ACCESS_KEY, SECRET_KEY);
    minioClient.makeBucket(BUCKETMANE);
    fileService = new FileService(minioClient, BUCKETMANE, "/");
  }

  @Test
  void test() {
    assertTrue(minioServer.isRunning());
  }

  @AfterAll
  static void shutDown() {
    if (minioServer.isRunning()) {
      minioServer.stop();
    }
  }

  @Test
  public void canCreateBucketWithUser() throws Exception {
    final String bucketName = "foo";
    minioClient.makeBucket(bucketName);
    assertTrue(minioClient.bucketExists(bucketName));
  }

  @Test
  void testIfUploadedFileIsStored() throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata"
        .getBytes(StandardCharsets.UTF_8));
    final String filename = "filenametestIfUploadedFileIsStored";
    fileService.uploadFile(testFile, filename);
    minioClient.statObject(BUCKETMANE, filename);
  }

  @Test
  void testIfUploadedFileContentIsSameAsDownloadedFileContent() throws
      IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException,
      InvalidArgumentException, InvalidResponseException, ErrorResponseException,
      NoResponseException, InvalidBucketNameException, InsufficientDataException,
      InternalException {
    final String filename = "filenameUploadedFileIsSameAsDownloadedFile";
    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain",
        "testdata".getBytes(StandardCharsets.UTF_8));
    final File compareFile = new File(filename);
    testFile.transferTo(compareFile);
    fileService.uploadFile(testFile, filename);
    final File minioFile = fileService.getFile(filename);
    fileService.getFile(filename);
    assertEquals(compareFile, minioFile);
  }

  @Test
  void testIfUploadStringContentIsStored() throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final String content = "Ich denke mir einen schönen Satz aus";
    final String filename = "filenameUploadStringContentIsStored";
    fileService.uploadContentConvertToMd(content, filename);
    minioClient.statObject(BUCKETMANE, filename);
  }

  @Test
  @SuppressWarnings("PMD.CloseResource")
  void testIfUploadStringContentIsRightSentence() throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final String testContent = "Ich denke mir einen schönen Satz aus";
    final String filename = "filenameUploadStringContentIsStored";
    fileService.uploadContentConvertToMd(testContent, filename);
    final InputStream inputStream = minioClient.getObject(BUCKETMANE, filename);
    final String storedContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    assertEquals(testContent, storedContent);
    inputStream.close();
  }

  @Test
  @SuppressWarnings("PMD.CloseResource")
  void testIfInputstreamGivesTheSameContentBack() throws IOException, XmlPullParserException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException,
      InvalidResponseException, ErrorResponseException, NoResponseException,
      InvalidBucketNameException, InsufficientDataException, InternalException {
    final String content = "ICh denke mir einen schönen Satz aus";
    final InputStream testInputStream = new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8));
    final String filename = "filenameInputstreamIsSame";
    minioClient.putObject(BUCKETMANE, filename,
        testInputStream, null, null, null, null);
    final InputStream minioInputstream = fileService.getFileInputStream(filename);
    final String storedContent = IOUtils.toString(minioInputstream, StandardCharsets.UTF_8);
    assertEquals(content, storedContent);
  }
}


