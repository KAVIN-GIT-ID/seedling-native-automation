package com.company.automation.base;

import com.company.automation.config.ConfigManager;
import com.company.automation.config.Environment;
import com.company.automation.driver.AndroidDriverFactory;
import com.company.automation.driver.DriverManager;
import com.company.automation.driver.IOSDriverFactory;
import com.company.automation.listeners.TestListener;
import com.company.automation.pages.ILoginPage;
import com.company.automation.pages.LoginPageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import java.io.File;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestContext context) {
        Environment env = ConfigManager.getEnvironment();

        if (env == Environment.PROD) {
            log.info("Running PROD-safe test");
        }

        String platform = null;
        if (context != null && context.getCurrentXmlTest() != null) {
            platform = context.getCurrentXmlTest().getParameter("platform");
        }
        if (platform == null || platform.trim().isEmpty()) {
            platform = ConfigManager.getPlatform();
        }
        platform = platform.toLowerCase().trim();

        DriverManager.setPlatform(platform);
        // Clean old MP4 recordings so logs/ only contains the latest current run video
        try {
            File logsDir = new File("logs");
            if (logsDir.exists() && logsDir.isDirectory()) {
                File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".mp4"));
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Exception ignored) {}

        if (platform.equals("ios")) {
            DriverManager.setDriver(IOSDriverFactory.create());
        } else {
            DriverManager.setDriver(AndroidDriverFactory.create());
        }

        // Small pause to allow the app UI and JS bundle to finish loading
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        // Start screen recording for UI test execution video
        try {
            if (DriverManager.getDriver() instanceof io.appium.java_client.screenrecording.CanRecordScreen) {
                ((io.appium.java_client.screenrecording.CanRecordScreen) DriverManager.getDriver()).startRecordingScreen();
                log.info("🎥 Started Appium screen recording");
            }
        } catch (Exception e) {
            log.info("Screen recording notice: {}", e.getMessage());
        }
    }


    /**
     * Common reusable login flow for Registered User scenarios (Seedling creation, donations, profile, etc.).
     */
    protected void loginAsRegisteredUser() {
        log.info("Performing standard registered user login");
        ILoginPage loginPage = LoginPageFactory.create();
        loginPage.handlePermissionIfPresent();
        loginPage.enterUsername(ConfigManager.credential("qa.username"));
        loginPage.enterPassword(ConfigManager.credential("qa.password"));
        loginPage.tapLogin();

        log.info("Waiting for API authentication & Home screen transition...");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 20000) {
            try {
                // Check if password field is gone (indicating login form cleared)
                List<org.openqa.selenium.WebElement> pwdFields = DriverManager.getDriver()
                        .findElements(org.openqa.selenium.By.xpath("//android.widget.EditText[@password='true']"));
                if (pwdFields.isEmpty()) {
                    log.info("✅ Login screen cleared — transition to Home screen detected");
                    break;
                }
            } catch (Exception ignored) {}
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }

        // Give React Native Home Screen bundle time to render
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        Assert.assertFalse(loginPage.isErrorDisplayed(), "Login failed for registered user flow");
        log.info("Registered user logged in successfully");
    }

    /**
     * Common flow for Unregistered/Guest User scenarios (Public search, explore, share, etc.).
     */
    protected void proceedAsUnregisteredUser() {
        log.info("Proceeding as unregistered / guest user");
        ILoginPage loginPage = LoginPageFactory.create();
        loginPage.handlePermissionIfPresent();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.warn("Test failed on {}: {}. Screenshot and details attached to ExtentReport.",
                    DriverManager.getPlatform(), result.getMethod().getMethodName());
        }

        // Stop screen recording and save MP4 video artifact
        try {
            if (DriverManager.getDriver() != null && DriverManager.getDriver() instanceof io.appium.java_client.screenrecording.CanRecordScreen) {
                String base64Video = ((io.appium.java_client.screenrecording.CanRecordScreen) DriverManager.getDriver()).stopRecordingScreen();
                if (base64Video != null && !base64Video.isEmpty()) {
                    byte[] videoBytes = java.util.Base64.getDecoder().decode(base64Video);
                    
                    // Save to logs/ (for GitHub Artifacts)
                    java.nio.file.Path logsDir = java.nio.file.Path.of("logs");
                    java.nio.file.Files.createDirectories(logsDir);
                    String videoName = result.getMethod().getMethodName() + "_" + System.currentTimeMillis() + ".mp4";
                    java.nio.file.Files.write(logsDir.resolve(videoName), videoBytes);
                    log.info("🎥 Saved screen recording video to logs/{}", videoName);

                    // Save to target/videos/ (for Surge deployment & ExtentReport link)
                    java.nio.file.Path targetVideosDir = java.nio.file.Path.of("target", "videos");
                    java.nio.file.Files.createDirectories(targetVideosDir);
                    java.nio.file.Files.write(targetVideosDir.resolve(videoName), videoBytes);

                    // Attach video link inside Extent HTML Report
                    if (com.company.automation.reports.ExtentManager.getTest() != null) {
                        com.company.automation.reports.ExtentManager.getTest().info(
                            "🎥 <b>Execution Video Recording:</b> <a href='videos/" + videoName + "' target='_blank' style='color:#007bff;font-weight:bold;'>Click to Watch Video (.mp4)</a>"
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.info("Screen recording save notice: {}", e.getMessage());
        }

        DriverManager.quitDriver();
    }
}
