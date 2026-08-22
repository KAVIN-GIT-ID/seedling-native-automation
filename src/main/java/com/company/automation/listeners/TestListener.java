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
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.PASS, "🎉 Test Passed Successfully: " + result.getMethod().getMethodName());
            try {
                String base64 = ScreenshotUtils.captureBase64(DriverManager.getDriver());
                if (base64 != null) {
                    ExtentManager.getTest().addScreenCaptureFromBase64String(base64, "Final Verified State");
                }
            } catch (Exception ignored) {}
        }
        ExtentManager.getReporter().flush();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        String errorMsg = (throwable != null) ? throwable.getMessage() : "Unknown Error";
        
        System.err.println("❌ [TEST FAILED] " + result.getMethod().getMethodName() + ": " + errorMsg);

        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.FAIL, "❌ <b>Test Failed at Step:</b> " + errorMsg);
            
            try {
                // Attach Base64 screenshot on failure showing the exact breakpoint
                String base64Screenshot = ScreenshotUtils.captureBase64(DriverManager.getDriver());
                if (base64Screenshot != null) {
                    ExtentManager.getTest().addScreenCaptureFromBase64String(base64Screenshot, "❌ Failure Breakpoint Screenshot");
                }
            } catch (Exception e) {
                ExtentManager.getTest().log(Status.WARNING, "Failed to capture failure screenshot: " + e.getMessage());
            }

            if (throwable != null) {
                ExtentManager.getTest().fail(throwable);
            }
        }
        ExtentManager.getReporter().flush();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().log(Status.SKIP, "⚠️ Test Skipped: " + result.getThrowable());
        }
        ExtentManager.getReporter().flush();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getReporter().flush();
    }
}
