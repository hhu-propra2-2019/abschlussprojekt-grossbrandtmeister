package mops.rheinjug2.fileupload;

import static org.junit.jupiter.api.Assertions.assertTrue;


import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class FileServiceTest {

  private static final String ACCESS_KEY = "minio";
  private static final String SECRET_KEY = "minio123";
  private static String minioServerUrl;

  @Autowired
  private MinioClient minioClient;

  @Container
  private static final GenericContainer minioServer = new GenericContainer<>(
      "minio/minio")
      .withCommand("server /data")
      .withExposedPorts(9001)
      .withEnv("MINIO_ACCESS_KEY", ACCESS_KEY)
      .withEnv("MINIO_SECRET_KEY", SECRET_KEY);

  @BeforeAll
  static void setUp() {
    //minioServer.start();
    minioServer.waitingFor(new HostPortWaitStrategy());
    final Integer mappedPort = minioServer.getMappedPort(9001);
    final String ipAddress = minioServer.getContainerIpAddress();
    org.testcontainers.Testcontainers.exposeHostPorts(mappedPort);
    final String rootUrl = String.format("http://host.testcontainers.internal:%d/", mappedPort);
    minioServerUrl = String.format("http://%s:%s", ipAddress, mappedPort);
  }

  @Test
  void test() {
    assertTrue(minioServer.isRunning());
  }

  /*
  @Test
  public void canCreateBucketWithUser() throws Exception {
    final MinioClient client = new MinioClient(minioServerUrl, ACCESS_KEY, SECRET_KEY);
    client.ignoreCertCheck();

    final String bucketName = "foo";
    client.makeBucket(bucketName);
    assertTrue(client.bucketExists(bucketName));
  }*/


  @AfterAll
  static void shutDown() {
    if (minioServer.isRunning()) {
      minioServer.stop();
    }
  }

}
