package com.company.automation.tests;

import com.company.automation.base.BaseTest;
import com.company.automation.config.ConfigManager;
import com.company.automation.pages.ILoginPage;
import com.company.automation.pages.LoginPageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {

    @Test
    public void testValidLogin() {
        log.info("Executing valid username & password login test");
        ILoginPage loginPage = LoginPageFactory.create();
        
        loginPage.enterUsername(ConfigManager.credential("qa.username"));
        loginPage.enterPassword(ConfigManager.credential("qa.password"));
        loginPage.tapLogin();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException ignored) {}

        // Validation
        Assert.assertFalse(loginPage.isErrorDisplayed(), "Login failed! Expected success but saw error.");
    }
}
