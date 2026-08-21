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
        log.info("Starting test on platform={} env={}", platform, env);

        if (platform.equals("ios")) {
            DriverManager.setDriver(IOSDriverFactory.create());
        } else {
            DriverManager.setDriver(AndroidDriverFactory.create());
        }

        String bundleId = platform.equals("ios") ? ConfigManager.env("ios.bundleId") : ConfigManager.env("android.appPackage");
        try {
            io.appium.java_client.AppiumDriver driver = DriverManager.getDriver();
            if (driver instanceof io.appium.java_client.InteractsWithApps) {
                log.info("Forcefully terminating and restarting app for a clean session on {}...", platform);
                io.appium.java_client.InteractsWithApps appsDriver = (io.appium.java_client.InteractsWithApps) driver;
                appsDriver.terminateApp(bundleId);
                appsDriver.activateApp(bundleId);
            }
        } catch (Exception e) {
            log.warn("Could not forcefully restart app on {}: {}", platform, e.getMessage());
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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

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
        DriverManager.quitDriver();
    }
}
