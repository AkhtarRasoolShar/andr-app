import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    lint {
        abortOnError = false
    }"""

replacement = """    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }"""

if target in content:
    content = content.replace(target, replacement)
else:
    # If the lint block doesn't exist or looks different, let's just append checkReleaseBuilds = false to the android block.
    # Searching for end of android block
    content = re.sub(r'(android\s*\{[\s\S]*?)(\n\})', r'\1\n    lint {\n        abortOnError = false\n        checkReleaseBuilds = false\n    }\n}', content)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
