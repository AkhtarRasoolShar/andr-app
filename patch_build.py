import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

deps = """
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
"""

content = content.replace('implementation(libs.firebase.auth)', 'implementation(libs.firebase.auth)' + deps)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
