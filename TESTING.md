# Testing the redstr OWASP ZAP Add-on

This guide explains how to test the redstr integration add-on for OWASP ZAP.

## Prerequisites

Before testing, ensure you have:

1. **OWASP ZAP 2.12.0 or later** installed
   - Download from: https://www.zaproxy.org/download/
   
2. **Java 11 or later** (required to run ZAP)
   ```bash
   java -version  # Should show 11 or higher
   ```

3. **redstr-server** running (optional but recommended for full testing)
   ```bash
   # Clone and run redstr-server
   git clone https://github.com/arvid-berndtsson/redstr-server.git
   cd redstr-server
   cargo build --release
   cargo run --release
   ```
   The server should start on `http://localhost:8080`

## Building the Add-on

1. **Build the ZAP add-on file**:
   ```bash
   cd /path/to/redstr-owasp-zap
   ./gradlew build
   ```

2. **Locate the built add-on**:
   ```bash
   ls -l build/zapAddOn/bin/redstr-alpha-1.0.0.zap
   ```

## Installing the Add-on in ZAP

### Method 1: Load Add-on File (Recommended for Testing)

1. **Open OWASP ZAP**

2. **Navigate to Add-on Manager**:
   - Click `Tools` → `Manage Add-ons`
   - OR press `Ctrl+Shift+M` (Windows/Linux) or `Cmd+Shift+M` (Mac)

3. **Load the Add-on**:
   - Click the `File` button (folder icon) at the bottom
   - Navigate to `build/zapAddOn/bin/`
   - Select `redstr-alpha-1.0.0.zap`
   - Click `Open`

4. **Confirm Installation**:
   - The add-on should appear in the list with status "Installed"
   - You may need to restart ZAP if prompted

### Method 2: Copy to ZAP Plugin Directory

Alternatively, copy the `.zap` file directly to ZAP's plugin directory:

```bash
# Linux
cp build/zapAddOn/bin/redstr-alpha-1.0.0.zap ~/.ZAP/plugin/

# macOS
cp build/zapAddOn/bin/redstr-alpha-1.0.0.zap ~/Library/Application\ Support/ZAP/plugin/

# Windows
copy build\zapAddOn\bin\redstr-alpha-1.0.0.zap %USERPROFILE%\OWASP ZAP\plugin\
```

Then restart ZAP.

## Verifying Installation

### 1. Check ZAP Logs

After starting ZAP, check the logs for redstr extension messages:

1. **Open the Output tab** at the bottom of ZAP
2. Look for log messages from the redstr extension:
   ```
   INFO o.z.z.e.redstr.RedstrExtension - Initializing redstr extension
   INFO o.z.z.e.redstr.RedstrExtension - redstr extension initialized
   INFO o.z.z.e.redstr.RedstrExtension - redstr extension hooked successfully
   INFO o.z.z.e.redstr.RedstrExtension - Starting redstr extension
   ```

3. **Check connection status**:
   - If redstr-server is running: You should see "Successfully connected to redstr server"
   - If server is not running: You'll see a warning about connection failure (this is expected)

### 2. Check Extension List

1. Go to `Tools` → `Manage Add-ons`
2. Navigate to the `Installed` tab
3. Look for **"redstr Integration"** in the list
4. Check the details:
   - Name: redstr Integration
   - Version: 1.0.0
   - Author: Arvid Berndtsson
   - Status: Alpha

## Testing Scenarios

### Test 1: Verify Extension Loads

**What to test**: The extension loads without errors

**Steps**:
1. Install the add-on as described above
2. Start/restart ZAP
3. Check the Output tab for any errors

**Expected result**: No errors, extension starts successfully

---

### Test 2: Test with redstr-server Running

**What to test**: The HTTP client can communicate with redstr-server

**Prerequisites**: redstr-server must be running on `http://localhost:8080`

**Steps**:
1. Start redstr-server:
   ```bash
   cd /path/to/redstr-server
   cargo run --release
   ```

2. Start ZAP with the add-on installed

3. Check ZAP logs for connection confirmation:
   ```
   INFO - Successfully connected to redstr server at http://localhost:8080
   ```

**Expected result**: Connection successful message in logs

---

### Test 3: Test without redstr-server (Graceful Degradation)

**What to test**: The extension handles missing server gracefully

**Steps**:
1. Ensure redstr-server is NOT running
2. Start ZAP with the add-on installed
3. Check ZAP logs

**Expected result**: 
- Warning message: "Could not connect to redstr server at http://localhost:8080"
- ZAP continues to function normally
- No crashes or errors

---

### Test 4: Manual API Test (Advanced)

**What to test**: Direct API calls work via Java scripting

**Prerequisites**: redstr-server running

**Steps**:
1. In ZAP, go to `Tools` → `Scripts Console`
2. Create a new script (Java or Groovy)
3. Use this test code:

```java
// Get the extension
def ext = org.parosproxy.paros.control.Control.getSingleton()
    .getExtensionLoader()
    .getExtension("RedstrExtension")

if (ext != null) {
    println("Extension found!")
    
    // Get the HTTP client
    def client = ext.getHttpClient()
    println("API URL: " + client.getApiUrl())
    
    // Test connection
    boolean connected = client.testConnection()
    println("Connection test: " + (connected ? "SUCCESS" : "FAILED"))
    
    // Try a transformation (if server is running)
    if (connected) {
        try {
            def result = client.transform("base64_encode", "test")
            println("Transformation result: " + result)
        } catch (Exception e) {
            println("Transformation failed: " + e.getMessage())
        }
    }
} else {
    println("Extension not found!")
}
```

4. Run the script

**Expected result** (with server running):
```
Extension found!
API URL: http://localhost:8080
Connection test: SUCCESS
Transformation result: dGVzdA==
```

---

## Unit Tests

The project includes automated unit tests. Run them with:

```bash
./gradlew test
```

**View test results**:
```bash
# Command line summary
./gradlew test --info

# HTML report
open build/reports/tests/test/index.html  # macOS
xdg-open build/reports/tests/test/index.html  # Linux
start build/reports/tests/test/index.html  # Windows
```

**Expected result**: All 9 tests should pass:
- testConstructorWithDefaults()
- testConstructorWithCustomUrl()
- testConstructorWithNullUrl()
- testSetApiUrl()
- testSetApiUrlToNull()
- testTransformWithNullFunction()
- testTransformWithEmptyFunction()
- testSetTimeout()
- testGetApiUrl()

## Troubleshooting

### Add-on Won't Load

**Problem**: Add-on doesn't appear in ZAP

**Solutions**:
1. Check ZAP version (must be 2.12.0+)
2. Check Java version (must be 11+)
3. Look for errors in ZAP logs
4. Try rebuilding: `./gradlew clean build`
5. Verify the .zap file is valid: `unzip -l build/zapAddOn/bin/redstr-alpha-1.0.0.zap`

### Connection to redstr-server Fails

**Problem**: "Could not connect to redstr server" warning

**Solutions**:
1. Verify redstr-server is running: `curl http://localhost:8080/health`
2. Check if port 8080 is in use: `netstat -an | grep 8080`
3. Check firewall settings
4. Try accessing in browser: http://localhost:8080

### ZAP Crashes on Startup

**Problem**: ZAP crashes after installing add-on

**Solutions**:
1. Remove the add-on file from ZAP's plugin directory
2. Check ZAP logs in `~/.ZAP/zap.log`
3. Report the issue with log details

## What's Currently Testable

At this stage, the following features are implemented and testable:

✅ **Add-on Installation**: Load and unload in ZAP  
✅ **Extension Lifecycle**: Init, start, stop, unload  
✅ **HTTP Client**: Connect to redstr-server  
✅ **Connection Testing**: Verify server availability  
✅ **Basic Transformations**: Call redstr functions via HTTP  
✅ **Error Handling**: Graceful failures when server unavailable  
✅ **Logging**: Comprehensive debug information  

## What's NOT Yet Implemented

The following features are planned but not yet available:

❌ **UI Components**: No context menus or panels yet  
❌ **Fuzzer Integration**: Cannot generate payloads yet  
❌ **Active Scan**: No scan rule enhancements yet  
❌ **Script Engine**: No JavaScript/Python bindings yet  
❌ **Configuration UI**: Settings must be hardcoded  

## Next Steps for Development

To extend testing capabilities:

1. **Add UI Components**: Implement context menus for manual transformation
2. **Fuzzer Integration**: Create payload generators
3. **Add Configuration Panel**: Allow URL/timeout changes via UI
4. **Script Bindings**: Enable scripting support
5. **Integration Tests**: Test with actual security scenarios

## Reporting Issues

If you encounter problems:

1. Check the troubleshooting section above
2. Collect ZAP logs from the Output tab
3. Note your environment (OS, ZAP version, Java version)
4. Create an issue on GitHub with details

## Summary

This add-on currently provides the **foundation** for redstr integration with ZAP. You can:

- ✅ Install it in ZAP successfully
- ✅ Verify it loads and initializes
- ✅ Test HTTP connectivity to redstr-server
- ✅ Perform basic transformations via scripts

The core infrastructure is complete and working. Future enhancements will add user-facing features like UI components, fuzzer integration, and active scan support.
