package com.company.automation.driver;

import com.company.automation.config.ConfigManager;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public final class IOSDriverFactory {

    private IOSDriverFactory() {}

    public static IOSDriver create() {
        XCUITestOptions options = new XCUITestOptions()
                .setUdid(ConfigManager.device("ios.udid"))
                .setDeviceName(ConfigManager.device("ios.deviceName"))
                .setPlatformVersion(ConfigManager.device("ios.platformVersion"))
                .setBundleId(ConfigManager.env("ios.bundleId"))
                // Reuses your dev team's existing Xcode signing to build/sign WebDriverAgent
                .setXcodeCertificate(new io.appium.java_client.ios.options.wda.XcodeCertificate(
                        ConfigManager.device("ios.xcodeOrgId"),
                        ConfigManager.device("ios.xcodeSigningId")
                ))
                .setNoReset(true)
                .setAutoAcceptAlerts(true)
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        // ⚡ MNC Performance & Speed Optimizations
        options.setCapability("appium:waitForQuiescence", false);
        options.setCapability("appium:wdaEventloopIdleDelay", 0);
        options.setCapability("appium:shouldUseCompactResponses", true);
        options.setCapability("appium:simpleIsVisibleCheck", true);
        options.setCapability("appium:skipLogCapture", true);
        options.setCapability("appium:maxTypingFrequency", 60);

        try {
            return new IOSDriver(new URL("http://127.0.0.1:4723/"), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }
}
