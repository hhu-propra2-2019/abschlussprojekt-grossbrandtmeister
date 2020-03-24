package mops.rheinjug2.fileupload;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.Testcontainers.exposeHostPorts;


import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.Base58;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {FileConfiguration.class})
@Testcontainers
class FileServiceTest {

  private static final String ACCESS_KEY = "minio";
  private static final String SECRET_KEY = "minio123";
  private static String minioServerUrl;

  private transient MinioClient minioClient;

  @Container
  static GenericContainer minioServer = new GenericContainer<>("minio/minio")
      .withEnv("MINIO_ACCESS_KEY", ACCESS_KEY)
      .withEnv("MINIO_SECRET_KEY", SECRET_KEY)
      .withCommand("server /data")
      .withExposedPorts(9000);

  private transient FileService fileService;


  @BeforeAll
  void setUp() throws InvalidPortException, InvalidEndpointException {
    minioServer.start();
    final Integer mappedPort = minioServer.getMappedPort(9000);
    exposeHostPorts(mappedPort);
    minioServerUrl = String.format("http://%s:%s", minioServer.getContainerIpAddress(), mappedPort);

    minioClient = new MinioClient(minioServerUrl, ACCESS_KEY, SECRET_KEY);
    fileService = new FileService(minioClient, "grossbrandtmeiser", "/");
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

  /*
  @Test
  void testIfUploadedFileIsStored() throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException,
      ErrorResponseException, InvalidArgumentException {
    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());
    final String filename = "filenametestIfUploadedFileIsStored";
    fileService.uploadFile(testFile, filename);
    minioClient.statObject("grossbrandtmeister", filename);
  }


  @Test
  void testIfUploadedFileContentIsSameAsDownloadedFileContent() throws
      IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException, InvalidResponseException, ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException, InternalException {
    final String filename = "filenameUploadedFileIsSameAsDownloadedFile";
    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());
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
    minioClient.statObject("grossbrandmeister", filename);
  }

  @Test
  void testIfUploadStringContentIsRightSentence() throws IOException, InvalidKeyException,
      NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException,
      XmlPullParserException, ErrorResponseException {
    final String testContent = "Ich denke mir einen schönen Satz aus";
    final String filename = "filenameUploadStringContentIsStored";
    fileService.uploadContentConvertToMd(testContent, filename);
    final InputStream inputStream = minioClient.getObject("grossbrandmeister", filename);
    final String miniocontent = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    assertEquals(testContent, miniocontent);
    inputStream.close();
  }

  @Test
  void testIfInputstreamIsSame() throws IOException, XmlPullParserException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException,
      InvalidResponseException, ErrorResponseException, NoResponseException,
      InvalidBucketNameException, InsufficientDataException, InternalException {
    final InputStream testInputStream = new ByteArrayInputStream("testcontent".getBytes(
        Charset.forName("UTF-8")));
    final String filename = "filenameInputstreamIsSame";
    minioClient.putObject("großbrandmeister", filename,
        testInputStream, null, null, null, null);
    final InputStream minioInputsream = fileService.getFileInputStream(filename);
    assertEquals(testInputStream, minioInputsream);
    minioInputsream.close();
    testInputStream.close();
  }

  @Test
  void testMarkdownToPdfConervion() throws IOException {
    final String filename = "filenameMarkdownToPdfConervion";
    final String testcontentinMd = "# Sample Markdown\n"
        + "\n"
        + "This is some basic, sample markdown.\n"
        + "\n"
        + "## Second Heading\n"
        + "\n"
        + " * Unordered lists, and:\n"
        + "  1. One\n"
        + "  1. Two\n"
        + "  1. Three\n"
        + " * More\n"
        + "\n"
        + "> Blockquote\n"
        + "\n"
        + "And **bold**, *italics*, and even *italics and later **bold***. Even ~~strikethrough~~. [A link](https://markdowntohtml.com) to somewhere.\n"
        + "And code highlighting:\n"
        + "```js\n"
        + "var foo = 'bar';\n"
        + "\n"
        + "function baz(s) {\n"
        + "   return foo + ':' + s;\n"
        + "}\n"
        + "```\n"
        + "Or inline code like `var foo = 'bar';`.\n"
        + "The end ...\n";
    final String testContentInHtml = "<h1 id=\"sample-markdown\">Sample Markdown</h1>\n"
        + "<p>This is some basic, sample markdown.</p>\n"
        + "<h2 id=\"second-heading\">Second Heading</h2>\n"
        + "<ul>\n"
        + "<li>Unordered lists, and:<ol>\n"
        + "<li>One</li>\n"
        + "<li>Two</li>\n"
        + "<li>Three</li>\n"
        + "</ol>\n"
        + "</li>\n"
        + "<li>More</li>\n"
        + "</ul>\n"
        + "<blockquote>\n"
        + "<p>Blockquote</p>\n"
        + "</blockquote>\n"
        + "<p>And <strong>bold</strong>, <em>italics</em>, and even <em>italics and later <strong>bold</strong></em>. Even <del>strikethrough</del>. <a href=\"https://markdowntohtml.com\">A link</a> to somewhere.\n"
        + "And code highlighting:</p>\n"
        + "<pre><code class=\"lang-js\"><span class=\"hljs-keyword\">var</span> foo = <span class=\"hljs-string\">'bar'</span>;\n"
        + "\n"
        + "<span class=\"hljs-function\"><span class=\"hljs-keyword\">function</span> <span class=\"hljs-title\">baz</span><span class=\"hljs-params\">(s)</span> </span>{\n"
        + "   <span class=\"hljs-keyword\">return</span> foo + <span class=\"hljs-string\">':'</span> + s;\n"
        + "}\n"
        + "</code></pre>\n"
        + "<p>Or inline code like <code>var foo = &#39;bar&#39;;</code>.\n"
        + "The end ...</p>\n";
    fileService.uploadContentConvertToMd(testcontentinMd, filename);
    final String testString = fileService.getFileAsHtmlString(filename);
    assertEquals(testContentInHtml, testString);
  }
  */
}
