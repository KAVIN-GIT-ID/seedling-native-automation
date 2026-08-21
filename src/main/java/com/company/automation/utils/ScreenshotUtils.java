package com.company.automation.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtils {

    public static String capture(AppiumDriver driver, String testName) {
        try {
            Path dir = Path.of("screenshots");
            Files.createDirectories(dir);

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                    .format(java.time.LocalDateTime.now());
            Path target = dir.resolve(testName + "_" + timestamp + ".png");

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target);
            return target.toString();
        } catch (IOException e) {
            return "Screenshot capture failed: " + e.getMessage();
        }
    }

    public static String captureBase64(AppiumDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            return null;
        }
    }
}
