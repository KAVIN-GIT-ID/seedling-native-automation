package com.company.automation.pages;

import com.company.automation.driver.DriverManager;

public final class LoginPageFactory {

    private LoginPageFactory() {}

    public static ILoginPage create() {
        return DriverManager.getPlatform().equals("ios")
                ? new com.company.automation.pages.ios.LoginPage()
                : new com.company.automation.pages.android.LoginPage();
    }
}
