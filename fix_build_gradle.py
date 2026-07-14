import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    defaultConfig {
        applicationId = "com.aistudio.craftmarket.qvxwrx"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }"""

replacement = """    defaultConfig {
        applicationId = "com.aistudio.craftmarket.qvxwrx"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\\"\\"" + (System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "") + "\\"\\"")
    }"""

content = content.replace(target, replacement)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
