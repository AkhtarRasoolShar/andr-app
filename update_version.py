import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 5', content)
content = re.sub(r'versionName = "[\d\.]+"', 'versionName = "1.4"', content)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
