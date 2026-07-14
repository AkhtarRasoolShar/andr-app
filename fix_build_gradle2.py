import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    applicationId = "com.aistudio.craftmarket.qvxwrx"
    minSdk = 24"""

replacement = """    applicationId = "com.aistudio.craftmarket.qvxwrx"
    minSdk = 24
    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\\"\\"" + (System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "") + "\\"\\"")"""

content = content.replace(target, replacement)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
