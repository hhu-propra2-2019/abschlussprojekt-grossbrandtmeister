package mops.rheinjug2.fileupload;

import static org.junit.jupiter.api.Assertions.assertTrue;


import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@org.testcontainers.junit.jupiter.Testcontainers
class FileServiceTest {

  private static final String ACCESS_KEY = "minio";
  private static final String SECRET_KEY = "minio123";
  private static String minioServerUrl;

  @Container
  private static final GenericContainer minioServer = new GenericContainer(
      "minio/minio")
      .withEnv("MINIO_ACCESS_KEY", ACCESS_KEY)
      .withEnv("MINIO_SECRET_KEY", SECRET_KEY)
      .withExposedPorts(9001);


  @BeforeAll
  static void setUp() throws Exception {
    minioServer.start();
    final Integer mappedPort = minioServer.getFirstMappedPort();
    Testcontainers.exposeHostPorts(mappedPort);
    minioServerUrl = String.format("http://%s:%s", minioServer.getContainerIpAddress(), mappedPort);

  }

  @Test
  public void canCreateBucketWithUser() throws Exception {
    final MinioClient client = new MinioClient(ACCESS_KEY, SECRET_KEY, minioServerUrl);
    client.ignoreCertCheck();

    final String bucketName = "foo";
    client.makeBucket(bucketName);
    assertTrue(client.bucketExists(bucketName));
  }


  @AfterEach
  void tearDown() {
  }

  @Test
  void uploadFile() {
  }

  @Test
  void getFile() {
  }

  @Test
  void getFileInputStream() {
  }

  @Test
  void uploadContentConvertToMd() {
  }

  @Test
  void getContentOfFileAsString() {
  }

  @Test
  void getFileAsHtmlString() {
  }

  @AfterAll
  static void shutDown() {
    if (minioServer.isRunning()) {
      minioServer.stop();
    }
  }
}
