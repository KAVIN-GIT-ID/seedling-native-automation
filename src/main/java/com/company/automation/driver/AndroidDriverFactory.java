package com.company.automation.driver;

import com.company.automation.config.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public final class AndroidDriverFactory {

    private AndroidDriverFactory() {}

    public static AndroidDriver create() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid(ConfigManager.device("android.udid"))
                .setDeviceName(ConfigManager.device("android.deviceName"))
                .setPlatformVersion(ConfigManager.device("android.platformVersion"))
                .setAppPackage(ConfigManager.env("android.appPackage"))
                .setAppActivity(ConfigManager.env("android.appActivity"))
                .setAutoGrantPermissions(true)
                .setNoReset(true)
                .setAdbExecTimeout(Duration.ofSeconds(60))
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(60))
                .setNewCommandTimeout(Duration.ofSeconds(240));

        try {
            return new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }
}
