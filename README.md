# redstr-owasp-zap

An OWASP ZAP add-on that integrates [redstr](https://github.com/arvid-berndtsson/redstr)'s string transformation capabilities into OWASP ZAP's security testing workflow.

## Overview

This add-on enables security professionals to use redstr transformations within OWASP ZAP for:
- Active scan payload generation
- Request/response transformation
- Fuzzer integration
- Context menu actions
- Custom script integration

## Prerequisites

- OWASP ZAP 2.12.0 or later
- Java 11+ (for ZAP add-on development)
- [redstr-server](https://github.com/arvid-berndtsson/redstr-server) running, OR
- redstr CLI installed locally

## Installation

### 1. Start redstr HTTP Server (Recommended)

```bash
# Clone and build redstr-server
git clone https://github.com/arvid-berndtsson/redstr-server.git
cd redstr-server
cargo build --release
cargo run --release
```

The server should be running at `http://localhost:8080`.

### 2. Install Add-on (Coming Soon)

The OWASP ZAP add-on implementation is planned. The structure will include:

```
redstr-owasp-zap/
├── build.gradle.kts        # Gradle build configuration
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── zaproxy/
│                   └── zap/
│                       └── extension/
│                           └── redstr/
│                               ├── RedstrExtension.java
│                               ├── RedstrHttpClient.java
│                               ├── RedstrFuzzerPayloadGenerator.java
│                               ├── RedstrContextMenuFactory.java
│                               └── RedstrScriptEngine.java
└── README.md
```

## Planned Features

### 1. Fuzzer Integration

Generate payloads using redstr transformations:
- SQL injection variations
- XSS payload obfuscation
- Command injection patterns
- Encoding chains
- Path traversal variations

### 2. Active Scan Enhancement

Enhance active scanning with redstr transformations:
- WAF bypass detection
- Advanced injection testing
- Obfuscated payload scanning
- Multi-encoding attack chains

### 3. Context Menu

Right-click menu items for quick transformations:
- Transform selection with redstr
- Generate payload variations
- Encode/decode operations
- Send to Fuzzer with transformations

### 4. Script Engine Integration

Custom script engine for ZAP scripts:
- JavaScript/Python bindings
- Direct redstr function access
- Automation support

### 5. Custom Panel

Dedicated panel for batch operations:
- Input/output text areas
- Method selector dropdown
- Transform button
- History of transformations

## Usage Examples (Planned)

### Fuzzer Payload Generator

```java
// Configure Fuzzer to use redstr payload generator
Fuzzer → Payloads → Add → redstr Payload Generator
Select method: sql_comment_injection
```

### Context Menu

```
Right-click on request/response → redstr → SQL Injection
Right-click on parameter → redstr → XSS Variation
Right-click on header → redstr → Encode
```

### Active Scan

```
1. Configure active scan with redstr transformations
2. Select target URL
3. Enable redstr payload generation
4. Start scan
```

## API Integration

The add-on communicates with the redstr HTTP server:

```java
public class RedstrHttpClient {
    private final String apiUrl = "http://localhost:8080";
    
    public String transform(String method, String input) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl + "/transform"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(
                String.format("{\"function\":\"%s\",\"input\":\"%s\"}", 
                    method, escapeJson(input))
            ))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            BodyHandlers.ofString());
        
        return parseResponse(response.body());
    }
}
```

## Building

### Gradle Build

```bash
cd redstr-owasp-zap
./gradlew build
```

Output: `build/zapAddOn/bin/redstr-alpha-1.0.0.zap`

### Loading Add-on

1. Open OWASP ZAP
2. Go to `Tools` → `Manage Add-ons`
3. Click the `File` button (folder icon)
4. Navigate to `build/zapAddOn/bin/`
5. Select `redstr-alpha-1.0.0.zap`
6. Click `Open`
7. Restart ZAP if prompted

## Testing

For detailed testing instructions, see [TESTING.md](TESTING.md).

### Quick Test

1. **Build the add-on**:
   ```bash
   ./gradlew build
   ```

2. **Start redstr-server** (optional):
   ```bash
   git clone https://github.com/arvid-berndtsson/redstr-server.git
   cd redstr-server
   cargo run --release
   ```

3. **Install in ZAP**:
   - Open ZAP
   - Go to `Tools` → `Manage Add-ons`
   - Click `File` and select `build/zapAddOn/bin/redstr-alpha-1.0.0.zap`

4. **Verify installation**:
   - Check the Output tab for "redstr extension initialized"
   - If redstr-server is running, you should see "Successfully connected to redstr server"

### Running Tests

```bash
# Run unit tests
./gradlew test

# View test report
open build/reports/tests/test/index.html  # macOS
xdg-open build/reports/tests/test/index.html  # Linux
```

### Current Status

**What's Working** ✅
- Add-on loads in ZAP 2.12.0+
- Connects to redstr-server API
- HTTP client with transformation support
- Error handling and logging
- All unit tests passing

**What's Not Yet Implemented** 🚧
- UI components (context menus, panels)
- Fuzzer payload generation
- Active scan integration
- Script engine bindings
- Configuration UI

## Configuration

Add-on settings will include:
- API endpoint URL
- Request timeout
- Default transformation methods
- Logging options
- Active scan integration settings

## Use Cases

### 1. SQL Injection Testing

```
Input: SELECT * FROM users WHERE id=1
Method: sql_comment_injection
Output: SELECT/**/FROM/**/users/**/WHERE/**/id=1
```

### 2. XSS with WAF Bypass

```
Input: <script>alert('XSS')</script>
Chain: xss_tag_variations → html_entity_encode → mixed_encoding
Output: Highly obfuscated XSS payload
```

### 3. JWT Manipulation

```
Input: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Methods: 
  - jwt_algorithm_confusion
  - jwt_header_manipulation
  - jwt_signature_bypass
```

### 4. API Fuzzing

```
Use Fuzzer with redstr payload generator:
1. Mark insertion points
2. Select payload type: redstr
3. Configure methods: sql, xss, command injection
4. Start fuzzing
```

### 5. Active Scan Enhancement

```
Configure active scan to use redstr transformations:
1. Enable redstr payload generation
2. Select transformation methods
3. Run active scan
4. Review findings with obfuscated payloads
```

## Script Integration

### JavaScript Example

```javascript
// ZAP script using redstr transformations
var RedstrClient = Java.type('org.zaproxy.zap.extension.redstr.RedstrHttpClient');
var client = new RedstrClient();

var input = "SELECT * FROM users";
var transformed = client.transform("sql_comment_injection", input);
print("Transformed: " + transformed);
```

### Python Example

```python
# ZAP script using redstr transformations
from org.zaproxy.zap.extension.redstr import RedstrHttpClient

client = RedstrHttpClient()
input = "SELECT * FROM users"
transformed = client.transform("sql_comment_injection", input)
print("Transformed: " + transformed)
```

## Troubleshooting

### Add-on Won't Load

- Check Java version (must be 11+)
- Verify ZAP version (must be 2.12.0+)
- Check ZAP log for errors
- Verify add-on file format

### API Connection Fails

- Ensure redstr HTTP server is running on `localhost:8080`
- Check firewall settings
- Verify API URL in add-on configuration
- Test connection with curl

### Transformations Produce Errors

- Check function name spelling
- Verify input format
- Check API server logs
- Test transformation directly via curl

### Performance Issues

- Reduce request timeout
- Use connection pooling
- Implement request caching
- Monitor memory usage

## Development

### Setup Development Environment

```bash
# Clone repository
git clone https://github.com/arvid-berndtsson/redstr-owasp-zap.git
cd redstr-owasp-zap

# Build add-on
./gradlew build

# Run tests
./gradlew test
```

### Loading in ZAP for Development

1. Build the add-on
2. In ZAP, go to Manage Add-ons
3. Click "Load Add-on File"
4. Select the built `.zap` file
5. Changes require rebuild and reload

## Testing

### Unit Tests

```java
@Test
public void testSqlTransformation() {
    RedstrHttpClient client = new RedstrHttpClient();
    String result = client.transform("sql_comment_injection", "SELECT * FROM users");
    assertTrue(result.contains("/**/"));
}
```

### Integration Tests

Test with ZAP API:
- Mock HTTP service
- Test payload generation
- Verify fuzzer integration
- Validate context menu actions

## Future Enhancements

- [ ] Implement Java add-on
- [ ] Create fuzzer payload generator
- [ ] Add active scan integration
- [ ] Build context menu actions
- [ ] Create script engine bindings
- [ ] Add configuration UI
- [ ] Implement custom panel
- [ ] Create detailed documentation
- [ ] Submit to ZAP Marketplace
- [ ] Create video tutorials

## Documentation

See the comprehensive integration guide:
- [OWASP ZAP Integration Documentation](https://github.com/arvid-berndtsson/redstr/blob/main/docs/INTEGRATION_GUIDELINES.md)

## References

- [OWASP ZAP Documentation](https://www.zaproxy.org/docs/)
- [ZAP Add-on Development Guide](https://www.zaproxy.org/docs/developer/)
- [ZAP API Documentation](https://www.zaproxy.org/docs/api/)
- [ZAP Extension Examples](https://github.com/zaproxy/zap-extensions)
- [redstr Core Library](https://github.com/arvid-berndtsson/redstr)
- [redstr-server](https://github.com/arvid-berndtsson/redstr-server)

## Support

For support and questions:
- Check OWASP ZAP documentation
- Visit ZAP User Group
- File issues on GitHub

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request
5. Follow ZAP add-on best practices

## License

MIT License - See LICENSE file in repository root.

---

**Status:** This integration is currently in planning phase. The HTTP API bridge is ready for use with custom ZAP add-ons. Full add-on implementation coming soon.

**Important:** This add-on is designed for authorized security testing only. Users must obtain proper authorization before conducting any security assessments.

