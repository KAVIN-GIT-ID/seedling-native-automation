package com.company.automation.pages;

import com.company.automation.driver.DriverManager;

public final class HomePageFactory {

    private HomePageFactory() {}

    public static IHomePage create() {
        return DriverManager.getPlatform().equals("ios")
                ? new com.company.automation.pages.ios.HomePage()
                : new com.company.automation.pages.android.HomePage();
    }
}
