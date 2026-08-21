package com.company.automation.pages.android;

import com.company.automation.base.BasePage;
import com.company.automation.pages.IHomePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class HomePage extends BasePage implements IHomePage {

    private final By headerTitle = AppiumBy.accessibilityId("home-header-title");
    private final By productList = AppiumBy.accessibilityId("home-product-list");

    @Override
    public boolean isHomeScreenLoaded() {
        return isDisplayed(headerTitle);
    }

    @Override
    public boolean isProductListVisible() {
        return isDisplayed(productList);
    }

    @Override
    public String getHeaderTitle() {
        return getText(headerTitle, "Header Title");
    }
}
