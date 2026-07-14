import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\\"" + (System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "") + "\\"")"""

replacement = """    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\\"" + (System.getenv("GOOGLE_WEB_CLIENT_ID")?.takeIf { it.isNotBlank() } ?: "864429531493-tflfa9v46rb14tm13inc81ji1eqmd2cq.apps.googleusercontent.com") + "\\"")"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Target not found")

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
