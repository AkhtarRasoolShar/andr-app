import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("debugConfig")
    }"""

replacement = """    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }"""

if target in content:
    content = content.replace(target, replacement)

content = re.sub(r'versionCode = 5', 'versionCode = 6', content)
content = re.sub(r'versionName = "1.4"', 'versionName = "1.5"', content)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
