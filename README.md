# Enterprise Mobile Automation Framework (Appium + Java + TestNG)

High-performance, enterprise-grade test automation framework for React Native mobile applications on **Android** and **iOS** real devices. Built using industry-standard **Page Object Model (POM)**, **Interface-driven Factory Pattern**, **ThreadLocal Driver Management**, and **Cross-Platform Parallel Execution**.

---

## 🏛️ Framework Architecture

```
                               ┌──────────────────────────┐
                               │       LoginTests         │ (Unified Cross-Platform Test)
                               └────────────┬─────────────┘
                                            │
                               ┌────────────▼─────────────┐
                               │    LoginPageFactory      │ (Thread-Safe Page Resolver)
                               └────────────┬─────────────┘
                                            │
                               ┌────────────▼─────────────┐
                               │        ILoginPage        │ (Shared Interface)
                               └──────┬────────────┬──────┘
                                      │            │
                    ┌─────────────────┘            └─────────────────┐
                    ▼                                                ▼
     ┌──────────────────────────────┐                ┌──────────────────────────────┐
     │  pages.android.LoginPage     │                │     pages.ios.LoginPage      │
     │  (UiAutomator2 Locators)     │                │   (iOS Class Chain Locators) │
     └──────────────┬───────────────┘                └──────────────┬───────────────┘
                    │                                                │
                    └─────────────────┐            ┌─────────────────┘
                                      ▼            ▼
                               ┌──────────────────────────┐
                               │         BasePage         │ (Resilient waits, gestures & logging)
                               └────────────┬─────────────┘
                                            │
                               ┌────────────▼─────────────┐
                               │      DriverManager       │ (ThreadLocal AppiumDriver & Platform)
                               └──────────────────────────┘
```

---

## 🚀 Fast-Track Execution Commands

### 1. Simultaneous Parallel Execution (Android & iOS Together)
Run both iOS and Android tests simultaneously, generate Extent report, auto-deploy to Surge, and send email report:
```bash
npm run test:parallel
```
Or via Maven directly:
```bash
mvn test -Denv=qa -DsuiteXmlFile=testng-parallel.xml
```

### 2. Single Platform Full Suite
```bash
# Android QA Suite
npm run test:android

# iOS QA Suite
npm run test:ios
```

### 3. Running Specific Test Classes or Methods
```bash
# Run single test class on Android
mvn test -Dtest=LoginTests -Denv=qa -Dplatform=android

# Run single test class on iOS
mvn test -Dtest=LoginTests -Denv=qa -Dplatform=ios

# Run single test method
mvn test -Dtest=LoginTests#testValidLogin -Denv=qa -Dplatform=android
```

### 4. Deploy & Send Email Report Manually
```bash
node utils/send-report.js
```

---

## ⚙️ Environment & Device Configurations

- **Device Definitions:** [config/devices.properties](file:///Users/uitglobalsolutions/Downloads/Seedling%20Native%20Automation/config/devices.properties) (UDIDs, OS versions, Apple Signing details)
- **QA Environment:** [config/qa.properties](file:///Users/uitglobalsolutions/Downloads/Seedling%20Native%20Automation/config/qa.properties) (App package, bundle ID, environment URLs)
- **Prod Environment:** [config/prod.properties](file:///Users/uitglobalsolutions/Downloads/Seedling%20Native%20Automation/config/prod.properties) (Read-only verification safety gate)
- **Credentials:** `config/credentials.properties` *(Local only, gitignored)*
- **Email & Surge Config:** `.env` *(Local only, gitignored)*

---

## 🛡️ Best Practices for Adding New Screens

1. **Create the Shared Interface:** Define UI contracts under `src/test/java/com/company/automation/pages/I<ScreenName>Page.java`.
2. **Implement Platform Page Objects:**
   - Android: `src/test/java/com/company/automation/pages/android/<ScreenName>Page.java` (using `AppiumBy.accessibilityId` or UiAutomator selectors).
   - iOS: `src/test/java/com/company/automation/pages/ios/<ScreenName>Page.java` (using `AppiumBy.accessibilityId` or `AppiumBy.iOSClassChain`).
3. **Add Factory:** Create `src/test/java/com/company/automation/pages/<ScreenName>PageFactory.java` delegating via `DriverManager.getPlatform()`.
4. **Write Unified Tests:** Add tests under `src/test/java/com/company/automation/tests/<Feature>Tests.java` extending `BaseTest`.
