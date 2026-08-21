package com.company.automation.driver;

import com.company.automation.config.ConfigManager;
import io.appium.java_client.AppiumDriver;

public final class DriverManager {

    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<String> platform = new ThreadLocal<>();

    private DriverManager() {}

    public static AppiumDriver getDriver() {
        AppiumDriver d = driver.get();
        if (d == null) {
            throw new IllegalStateException("Driver not initialized. Call DriverManager.setDriver() in test setup.");
        }
        return d;
    }

    public static void setDriver(AppiumDriver appiumDriver) {
        driver.set(appiumDriver);
    }

    public static String getPlatform() {
        String p = platform.get();
        return (p != null && !p.isEmpty()) ? p : ConfigManager.getPlatform();
    }

    public static void setPlatform(String plat) {
        platform.set(plat);
    }

    public static void quitDriver() {
        AppiumDriver d = driver.get();
        if (d != null) {
            try {
                d.quit();
            } catch (Exception ignored) {}
            driver.remove();
            platform.remove();
        }
    }
}
