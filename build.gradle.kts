import org.zaproxy.gradle.addon.AddOnStatus

plugins {
    java
    id("org.zaproxy.add-on") version "0.9.0"
}

group = "org.zaproxy.zap.extension"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    compileOnly("org.zaproxy:zap:2.12.0")
    compileOnly("org.apache.logging.log4j:log4j-api:2.17.2")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testImplementation("org.apache.logging.log4j:log4j-api:2.17.2")
    testImplementation("org.apache.logging.log4j:log4j-core:2.17.2")
}

zapAddOn {
    addOnId.set("redstr")
    addOnName.set("redstr Integration")
    addOnStatus.set(AddOnStatus.ALPHA)
    
    manifest {
        author.set("Arvid Berndtsson")
        url.set("https://github.com/arvid-berndtsson/redstr-owasp-zap")
        
        extensions {
            register("org.zaproxy.zap.extension.redstr.RedstrExtension")
        }
        
        dependencies {
            addOns {
                // No additional add-on dependencies
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
