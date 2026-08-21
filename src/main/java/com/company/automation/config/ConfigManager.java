package com.company.automation.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single place everything else reads config from.
 * Precedence: -Denv / -Dplatform system properties (set via `mvn test -Denv=prod -Dplatform=ios`)
 * decide which files get loaded; nothing is hard-coded in Java.
 */
public final class ConfigManager {

    private static final Properties envProps = new Properties();
    private static final Properties deviceProps = new Properties();
    private static final Properties credentialProps = new Properties();
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
        loadInto(deviceProps, "config/devices.properties", true);
        // Credentials file is optional - prod runs don't need it at all
        loadInto(credentialProps, "config/credentials.properties", false);
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
        String value = credentialProps.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing credential: " + key
                    + ". Copy config/credentials.properties.template to config/credentials.properties and fill it in.");
        }
        return value;
    }
}
