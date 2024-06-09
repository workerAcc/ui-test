package uiTest;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.PushGateway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@ExtendWith(ExecutionWatcher.class)
public class TestProductStar {
    private Locators locators = new Locators();
    private static WebDriver driver;

    static CollectorRegistry registry;
    static Counter requests;
    public static Counter failedRequests;
    public static Counter passedRequest;

    private static void configurationRemote() throws URISyntaxException, MalformedURLException {
        Configuration.baseUrl = "https://productstar.ru/";

        String startRemote = System.getenv("StartRemote");
        String selenoidUri = System.getenv("SELENOID_URI");

        if("yes".equals(startRemote) && selenoidUri!= null) {
            Configuration.remote = System.getenv("SELENOID_URI");
            Configuration.browser = "chrome";

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--window-size=1920,1080");
            options.setCapability("browserName", "chrome");
            //options.setCapability("acceptInsecureCerts", true);
            options.setCapability("selenoid:options", Map.of(
                    "enableVNC", true
            ));
            URI selenoidURI = new URI(Configuration.remote);
            driver = new RemoteWebDriver(selenoidURI.toURL(), options);
        }
    }

    @BeforeAll
    public static void setUp() throws URISyntaxException, MalformedURLException {
        configurationRemote();

        registry = new CollectorRegistry();
        requests = Counter.build()
                .name("total_tests")
                .help("Number of tests run")
                .register(registry);
        failedRequests = Counter.build()
                .name("failed_tests")
                .help("Number of failed tests")
                .register(registry);
        passedRequest = Counter.build()
                .name("passed_tests")
                .help("Number of passed tests")
                .register(registry);
    }
    @AfterAll
    public static void tearDown() throws IOException {
        Selenide.closeWebDriver();
        PushGateway pg = new PushGateway("127.0.0.1:9091");
        pg.push(registry,"my_batch_job");
    }

    @AfterEach
    public void testTearDown(TestInfo testInfo) {
        requests.inc();
        if (testInfo.getTags().contains("failed")) {
            failedRequests.inc();
        } else {
            passedRequest.inc();
        }
    }

    @BeforeEach
    public void prepareForTest() {
        open("/");
    }

    @Test
    @DisplayName("Переход в раздел Все программы")
    public void productStarTest() {
        locators.checkTextInTitle("Обучение востребованным ");
        locators.clickButtonLink("Выбрать направление");
        locators.clickButtonLink("Перейти в каталог");
        locators.checkTextInTitle("Все программы обучения ProductStar");
    }
}
