package com.company.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.company.automation.driver.DriverManager;
import com.company.automation.reports.ExtentManager;
import com.company.automation.utils.ScreenshotUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getReporter();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String platform = result.getTestContext().getCurrentXmlTest().getParameter("platform");
        if (platform == null || platform.isEmpty()) {
            platform = DriverManager.getPlatform();
        }
        String displayName = "[" + platform.toUpperCase() + "] " + methodName;

        ExtentTest test = ExtentManager.getReporter().createTest(displayName);
        ExtentManager.setTest(test);
        ExtentManager.getTest().log(Status.INFO, "Test Execution Started on " + platform.toUpperCase() + ": " + methodName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(Status.PASS, "Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getTest().log(Status.FAIL, "Test Failed: " + result.getThrowable());
        
        try {
            // Attach Base64 screenshot on failure
            String base64Screenshot = ScreenshotUtils.captureBase64(DriverManager.getDriver());
            if (base64Screenshot != null) {
                ExtentManager.getTest().addScreenCaptureFromBase64String(base64Screenshot, "Failure Screenshot");
            }
        } catch (Exception e) {
            ExtentManager.getTest().log(Status.WARNING, "Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentManager.getTest().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getReporter().flush();
    }
}
