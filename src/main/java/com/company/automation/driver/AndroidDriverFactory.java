package com.company.automation.driver;

import com.company.automation.config.ConfigManager;
import com.company.automation.config.ExecutionMode;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class AndroidDriverFactory {

    private static final Logger log = LoggerFactory.getLogger(AndroidDriverFactory.class);

    private AndroidDriverFactory() {}

    public static AndroidDriver create() {
        ExecutionMode mode = ConfigManager.getExecutionMode();
        log.info("Initializing AndroidDriver with execution mode: {}", mode);

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

    private static AndroidDriver createLocalDriver() {
        UiAutomator2Options options = new UiAutomator2Options();

        String udid = ConfigManager.getUdid("android");
        if (udid != null && !udid.isEmpty()) {
            options.setUdid(udid);
        }

        options.setDeviceName(ConfigManager.getDeviceName("android"))
                .setPlatformVersion(ConfigManager.getPlatformVersion("android"))
                .setAppPackage(ConfigManager.env("android.appPackage"))
                .setAppActivity(ConfigManager.env("android.appActivity"))
                .setAutoGrantPermissions(true)
                .setNoReset(true)
                .setAdbExecTimeout(Duration.ofSeconds(90))
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(90))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        String appPath = ConfigManager.getAppPath();
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        return createDriverInstance(ConfigManager.getAppiumUrl(), options);
    }

    private static AndroidDriver createCiDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setAppPackage(ConfigManager.env("android.appPackage"))
                .setAppActivity(ConfigManager.env("android.appActivity"))
                .setAutoGrantPermissions(true)
                .setNoReset(false)
                .setAdbExecTimeout(Duration.ofSeconds(120))
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(120))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        // Only set platformVersion / deviceName if explicitly overridden via -D flags
        String explicitVersion = System.getProperty("platformVersion");
        if (explicitVersion != null && !explicitVersion.trim().isEmpty()) {
            options.setPlatformVersion(explicitVersion.trim());
        }

        String explicitDevice = System.getProperty("deviceName");
        if (explicitDevice != null && !explicitDevice.trim().isEmpty()) {
            options.setDeviceName(explicitDevice.trim());
        }

        String appPath = ConfigManager.getAppPath();
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        }

        return createDriverInstance(ConfigManager.getAppiumUrl(), options);
    }


    private static AndroidDriver createBrowserStackDriver() {
        String username = ConfigManager.resolve("bstackUser", "BROWSERSTACK_USERNAME", null, null,
                ConfigManager.getCloudProperty("browserstack.user", ""));
        String accessKey = ConfigManager.resolve("bstackKey", "BROWSERSTACK_ACCESS_KEY", null, null,
                ConfigManager.getCloudProperty("browserstack.key", ""));

        if (username.isEmpty() || accessKey.isEmpty()) {
            throw new IllegalStateException("BrowserStack credentials missing! Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY.");
        }

        UiAutomator2Options options = new UiAutomator2Options();
        options.setCapability("platformName", "android");

        String app = ConfigManager.resolve("app", "APP", null, null,
                ConfigManager.getCloudProperty("browserstack.app.android", ""));
        if (!app.isEmpty()) {
            options.setApp(app);
        }

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("deviceName", ConfigManager.getDeviceName("android"));
        bstackOptions.put("osVersion", ConfigManager.getPlatformVersion("android"));
        bstackOptions.put("projectName", ConfigManager.getCloudProperty("browserstack.project", "Seedling Mobile App"));
        bstackOptions.put("buildName", ConfigManager.getCloudProperty("browserstack.build", "Build-" + System.currentTimeMillis()));
        bstackOptions.put("sessionName", "Android Test - " + ConfigManager.getEnvironment());
        bstackOptions.put("appiumVersion", "2.0.0");

        options.setCapability("bstack:options", bstackOptions);

        String hubUrl = "https://" + username + ":" + accessKey + "@" + ConfigManager.getCloudProperty("browserstack.server", "hub-cloud.browserstack.com") + "/wd/hub";
        return createDriverInstance(hubUrl, options);
    }

    private static AndroidDriver createDriverInstance(String urlString, UiAutomator2Options options) {
        try {
            log.info("Connecting to Appium server at: {}", urlString.replaceAll(":[^:@]+@", ":****@"));
            return new AndroidDriver(new URL(urlString), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + urlString, e);
        }
    }
}

