package org.zaproxy.zap.extension.redstr;

import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

/**
 * Main extension class for the redstr integration.
 * Provides the entry point and lifecycle management for the add-on.
 */
public class RedstrExtension extends ExtensionAdaptor {
    
    private static final Logger LOGGER = LogManager.getLogger(RedstrExtension.class);
    
    public static final String NAME = "RedstrExtension";
    public static final String PREFIX = "redstr";
    
    private RedstrHttpClient httpClient;
    private boolean enabled = true;
    
    /**
     * Default constructor required by ZAP.
     */
    public RedstrExtension() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }
    
    @Override
    public void init() {
        super.init();
        
        LOGGER.info("Initializing redstr extension");
        
        // Initialize the HTTP client with default settings
        httpClient = new RedstrHttpClient();
        
        LOGGER.info("redstr extension initialized");
    }
    
    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        
        if (hasView()) {
            LOGGER.debug("Hooking redstr extension with UI components");
            // Future: Add UI components like menus, panels, etc.
        }
        
        LOGGER.info("redstr extension hooked successfully");
    }
    
    @Override
    public void start() {
        super.start();
        
        LOGGER.info("Starting redstr extension");
        
        if (enabled) {
            // Test connection to redstr server
            boolean connected = httpClient.testConnection();
            if (connected) {
                LOGGER.info("Successfully connected to redstr server at {}", 
                        httpClient.getApiUrl());
                logMessage("redstr.info.ready");
            } else {
                LOGGER.warn("Could not connect to redstr server at {}. " +
                        "Ensure redstr-server is running.", 
                        httpClient.getApiUrl());
            }
        } else {
            LOGGER.info("redstr extension is disabled");
            logMessage("redstr.info.disabled");
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        LOGGER.info("Stopping redstr extension");
    }
    
    @Override
    public void unload() {
        super.unload();
        LOGGER.info("Unloading redstr extension");
    }
    
    @Override
    public String getUIName() {
        return getMessage("redstr.extension.name");
    }
    
    @Override
    public String getDescription() {
        return getMessage("redstr.extension.desc");
    }
    
    /**
     * Gets the HTTP client instance.
     * 
     * @return The RedstrHttpClient instance
     */
    public RedstrHttpClient getHttpClient() {
        return httpClient;
    }
    
    /**
     * Checks if the extension is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the extension is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.info("redstr extension enabled: {}", enabled);
    }
    
    /**
     * Gets a localized message.
     * 
     * @param key The message key
     * @return The localized message
     */
    private String getMessage(String key) {
        try {
            return Constant.messages.getString(key);
        } catch (Exception e) {
            LOGGER.warn("Failed to get message for key: {}", key);
            return key;
        }
    }
    
    /**
     * Logs a localized message to the ZAP output.
     * 
     * @param key The message key
     */
    private void logMessage(String key) {
        String message = getMessage(key);
        LOGGER.info(message);
    }
    
    @Override
    public boolean canUnload() {
        // Allow the extension to be unloaded
        return true;
    }
    
    @Override
    public boolean supportsDb(String type) {
        // Extension doesn't use database
        return true;
    }
}
