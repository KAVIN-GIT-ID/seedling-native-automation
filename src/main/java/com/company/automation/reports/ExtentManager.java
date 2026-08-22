package com.company.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.company.automation.config.ConfigManager;

public class ExtentManager {

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public synchronized static ExtentReports getReporter() {
        if (extentReports == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/index.html");
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("Automation Test Report");
            spark.config().setReportName("Mobile Automation Report");

            extentReports = new ExtentReports();
            extentReports.attachReporter(spark);
            extentReports.setSystemInfo("Platform", System.getProperty("platform", "android"));
            extentReports.setSystemInfo("Environment", System.getProperty("env", "qa"));
            extentReports.setSystemInfo("Execution Target", ConfigManager.getExecutionMode().name());
            extentReports.setSystemInfo("Device", ConfigManager.getDeviceName(System.getProperty("platform", "android")));
        }
        return extentReports;
    }

    public static ExtentTest getTest() {
        return extentTest.get();
    }

    public static void setTest(ExtentTest test) {
        extentTest.set(test);
    }

    public static void removeTest() {
        extentTest.remove();
    }
}
