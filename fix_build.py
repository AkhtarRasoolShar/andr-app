import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

# Make sure googleid is there. 
# Also let's try to see if there's any syntax error.
print("googleid in file:", "com.google.android.libraries.identity.googleid:googleid" in content)
