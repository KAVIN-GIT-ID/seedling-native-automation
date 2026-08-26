package com.company.automation.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single place everything else reads config from.
 * Precedence: -Denv / -Dplatform / -Dexecution system properties
 * decide which files get loaded; nothing is hard-coded in Java.
 */
public final class ConfigManager {

    private static final Properties envProps = new Properties();
    private static final Properties deviceProps = new Properties();
    private static final Properties credentialProps = new Properties();
    private static final Properties cloudProps = new Properties();
    private static Environment currentEnv;

    static {
        load();
    }

    private ConfigManager() {}

    private static void load() {
        String envName = System.getProperty("env", "qa");
        currentEnv = Environment.from(envName);

        String envFile = "config/" + envName.toLowerCase() + ".properties";
        loadInto(envProps, envFile, true);
        loadInto(deviceProps, "config/devices.properties", false);
        // Credentials file is optional - prod runs don't need it at all
        loadInto(credentialProps, "config/credentials.properties", false);
        // Cloud file is optional - local runs don't need it at all
        loadInto(cloudProps, "config/cloud.properties", false);
    }

    private static void loadInto(Properties target, String path, boolean required) {
        try (InputStream in = new FileInputStream(path)) {
            target.load(in);
        } catch (IOException e) {
            if (required) {
                throw new RuntimeException("Required config file not found or unreadable: " + path
                        + " (run from the project root so relative paths resolve).", e);
            }
            // optional file missing - fine, leave properties empty
        }
    }

    public static Environment getEnvironment() {
        return currentEnv;
    }

    public static String getPlatform() {
        return System.getProperty("platform", "android").toLowerCase();
    }

    public static ExecutionMode getExecutionMode() {
        return ExecutionMode.from(System.getProperty("execution", "local"));
    }

    public static boolean isWriteAllowed() {
        return Boolean.parseBoolean(envProps.getProperty("allow.write.operations", "false"));
    }

    public static String env(String key) {
        String value = envProps.getProperty(key);
        if (value == null) throw new RuntimeException("Missing env property: " + key);
        return value;
    }

    public static String device(String key) {
        String value = deviceProps.getProperty(key);
        if (value == null) throw new RuntimeException("Missing device property: " + key);
        return value;
    }

    public static String credential(String key) {
        String envKey = key.toUpperCase().replace('.', '_');
        String value = resolve(key, envKey, credentialProps, key, null);
        if (value == null) {
            throw new RuntimeException("Missing credential: " + key
                    + ". Copy config/credentials.properties.template to config/credentials.properties and fill it in, or pass via GitHub Secrets/env vars.");
        }
        return value;
    }


    /**
     * Resolves property with order: System Property -> Environment Variable -> Config File -> Default
     */
    public static String resolve(String sysProp, String envVar, Properties props, String propKey, String defaultValue) {
        if (sysProp != null && System.getProperty(sysProp) != null && !System.getProperty(sysProp).trim().isEmpty()) {
            return System.getProperty(sysProp).trim();
        }
        if (envVar != null && System.getenv(envVar) != null && !System.getenv(envVar).trim().isEmpty()) {
            return System.getenv(envVar).trim();
        }
        if (props != null && propKey != null && props.getProperty(propKey) != null && !props.getProperty(propKey).trim().isEmpty()) {
            return props.getProperty(propKey).trim();
        }
        return defaultValue;
    }

    public static String getCloudProperty(String key, String defaultValue) {
        String envKey = key.toUpperCase().replace('.', '_');
        return resolve(key, envKey, cloudProps, key, defaultValue);
    }

    public static String getAppiumUrl() {
        return resolve("appiumUrl", "APPIUM_URL", null, null, "http://127.0.0.1:4723/");
    }

    public static String getAppPath() {
        return getAppPath(getPlatform());
    }

    public static String getAppPath(String platform) {
        String customPath = resolve("appPath", "APP_PATH", cloudProps, "app.path", null);
        if (customPath != null && !customPath.isEmpty()) {
            return customPath;
        }

        String envName = (currentEnv != null) ? currentEnv.name().toLowerCase() : "qa";
        String targetExt = platform.equalsIgnoreCase("ios") ? ".app" : ".apk";

        // 1. Check apps/<env>/ folder (e.g. apps/qa/ or apps/prod/)
        java.io.File envAppsDir = new java.io.File("apps/" + envName);
        if (envAppsDir.exists() && envAppsDir.isDirectory()) {
            String found = findAppInDir(envAppsDir, platform, targetExt);
            if (found != null) return found;
        }

        // 2. Check root apps/ folder for env-matched files (e.g. seedling-prod.apk or .app)
        java.io.File appsDir = new java.io.File("apps");
        if (appsDir.exists() && appsDir.isDirectory()) {
            String found = findAppInDir(appsDir, platform, targetExt);
            if (found != null) return found;
        }
        return null;
    }

    private static String findAppInDir(java.io.File dir, String platform, String targetExt) {
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            // First priority: Exact extension match (.app directory / .apk file)
            for (java.io.File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(targetExt)) {
                    return file.getAbsolutePath();
                }
            }

            // Check subdirectories (e.g. apps/qa/Payload/SeedlingApp.app)
            for (java.io.File file : files) {
                if (file.isDirectory() && !file.getName().toLowerCase().endsWith(".app")) {
                    String subFound = findAppInDir(file, platform, targetExt);
                    if (subFound != null && subFound.endsWith(targetExt)) {
                        return subFound;
                    }
                }
            }
        }
        return null;
    }

    public static String getDeviceName(String platform) {
        String prefix = platform.toLowerCase();
        String sysProp = "deviceName";
        String envVar = "DEVICE_NAME";
        String configKey = prefix + ".deviceName";
        String cloudKey = "browserstack." + prefix + ".device";
        String fallback = prefix.equals("ios") ? "iPhone 15" : "Pixel_7";

        if (getExecutionMode() == ExecutionMode.BROWSERSTACK) {
            return resolve(sysProp, envVar, cloudProps, cloudKey, prefix.equals("ios") ? "iPhone 15" : "Google Pixel 7");
        }
        return resolve(sysProp, envVar, deviceProps, configKey, fallback);
    }

    public static String getPlatformVersion(String platform) {
        String prefix = platform.toLowerCase();
        String sysProp = "platformVersion";
        String envVar = "PLATFORM_VERSION";
        String configKey = prefix + ".platformVersion";
        String cloudKey = "browserstack." + prefix + ".os_version";
        String fallback = prefix.equals("ios") ? "17.2" : "13.0";

        if (getExecutionMode() == ExecutionMode.BROWSERSTACK) {
            return resolve(sysProp, envVar, cloudProps, cloudKey, prefix.equals("ios") ? "17.0" : "13.0");
        }
        return resolve(sysProp, envVar, deviceProps, configKey, fallback);
    }

    public static String getUdid(String platform) {
        String configKey = platform.toLowerCase() + ".udid";
        return resolve("udid", "UDID", deviceProps, configKey, null);
    }
}

