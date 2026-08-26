package com.company.automation.driver;

import com.company.automation.config.ConfigManager;
import com.company.automation.config.ExecutionMode;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class IOSDriverFactory {

    private static final Logger log = LoggerFactory.getLogger(IOSDriverFactory.class);

    private IOSDriverFactory() {}

    public static IOSDriver create() {
        ExecutionMode mode = ConfigManager.getExecutionMode();
        log.info("Initializing IOSDriver with execution mode: {}", mode);

        switch (mode) {
            case BROWSERSTACK:
                return createBrowserStackDriver();
            case CI:
                return createCiDriver();
            case LOCAL:
            default:
                return createLocalDriver();
        }
    }

    private static void applyPerformanceOptimizations(XCUITestOptions options) {
        options.setCapability("appium:waitForQuiescence", false);
        options.setCapability("appium:wdaEventloopIdleDelay", 0);
        options.setCapability("appium:shouldUseCompactResponses", true);
        options.setCapability("appium:simpleIsVisibleCheck", true);
        options.setCapability("appium:skipLogCapture", true);
        options.setCapability("appium:maxTypingFrequency", 60);
    }

    private static IOSDriver createLocalDriver() {
        XCUITestOptions options = new XCUITestOptions();

        String udid = ConfigManager.getUdid("ios");
        if (udid != null && !udid.isEmpty()) {
            options.setUdid(udid);
        }

        options.setDeviceName(ConfigManager.getDeviceName("ios"))
                .setPlatformVersion(ConfigManager.getPlatformVersion("ios"))
                .setBundleId(ConfigManager.env("ios.bundleId"))
                .setNoReset(true)
                .setAutoAcceptAlerts(true)
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        // Reuses your dev team's existing Xcode signing for real device if configured
        try {
            String orgId = ConfigManager.device("ios.xcodeOrgId");
            String signId = ConfigManager.device("ios.xcodeSigningId");
            if (orgId != null && !orgId.isEmpty() && signId != null && !signId.isEmpty()) {
                options.setXcodeCertificate(new io.appium.java_client.ios.options.wda.XcodeCertificate(orgId, signId));
            }
        } catch (Exception ignored) {}

        String appPath = ConfigManager.getAppPath();
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        applyPerformanceOptimizations(options);
        return createDriverInstance(ConfigManager.getAppiumUrl(), options);
    }

    private static IOSDriver createCiDriver() {
        XCUITestOptions options = new XCUITestOptions()
                .setBundleId(ConfigManager.env("ios.bundleId"))
                .setNoReset(false)
                .setAutoAcceptAlerts(true)
                .setWdaLaunchTimeout(Duration.ofSeconds(60))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        String udid = System.getProperty("udid");
        if (udid == null || udid.trim().isEmpty()) {
            udid = System.getenv("IOS_UDID");
        }
        if (udid != null && !udid.trim().isEmpty()) {
            options.setUdid(udid.trim());
        } else {
            String explicitDevice = System.getProperty("deviceName");
            options.setDeviceName((explicitDevice != null && !explicitDevice.trim().isEmpty()) 
                    ? explicitDevice.trim() 
                    : ConfigManager.getDeviceName("ios"));
        }

        String explicitVersion = System.getProperty("platformVersion");
        if (explicitVersion != null && !explicitVersion.trim().isEmpty()) {
            options.setPlatformVersion(explicitVersion.trim());
        }

        String appPath = ConfigManager.getAppPath();
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        applyPerformanceOptimizations(options);
        return createDriverInstance(ConfigManager.getAppiumUrl(), options);
    }

    private static IOSDriver createBrowserStackDriver() {
        String username = ConfigManager.resolve("bstackUser", "BROWSERSTACK_USERNAME", null, null,
                ConfigManager.getCloudProperty("browserstack.user", ""));
        String accessKey = ConfigManager.resolve("bstackKey", "BROWSERSTACK_ACCESS_KEY", null, null,
                ConfigManager.getCloudProperty("browserstack.key", ""));

        if (username.isEmpty() || accessKey.isEmpty()) {
            throw new IllegalStateException("BrowserStack credentials missing! Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY.");
        }

        XCUITestOptions options = new XCUITestOptions();
        options.setCapability("platformName", "ios");

        String app = ConfigManager.resolve("app", "APP", null, null,
                ConfigManager.getCloudProperty("browserstack.app.ios", ""));
        if (!app.isEmpty()) {
            options.setApp(app);
        }

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("deviceName", ConfigManager.getDeviceName("ios"));
        bstackOptions.put("osVersion", ConfigManager.getPlatformVersion("ios"));
        bstackOptions.put("projectName", ConfigManager.getCloudProperty("browserstack.project", "Seedling Mobile App"));
        bstackOptions.put("buildName", ConfigManager.getCloudProperty("browserstack.build", "Build-" + System.currentTimeMillis()));
        bstackOptions.put("sessionName", "iOS Test - " + ConfigManager.getEnvironment());
        bstackOptions.put("appiumVersion", "2.0.0");

        options.setCapability("bstack:options", bstackOptions);
        applyPerformanceOptimizations(options);

        String hubUrl = "https://" + username + ":" + accessKey + "@" + ConfigManager.getCloudProperty("browserstack.server", "hub-cloud.browserstack.com") + "/wd/hub";
        return createDriverInstance(hubUrl, options);
    }

    private static IOSDriver createDriverInstance(String urlString, XCUITestOptions options) {
        try {
            log.info("Connecting to Appium server at: {}", urlString.replaceAll(":[^:@]+@", ":****@"));
            return new IOSDriver(new URL(urlString), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + urlString, e);
        }
    }
}

