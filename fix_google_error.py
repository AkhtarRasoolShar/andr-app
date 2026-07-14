import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

target = """                                } catch (e: Exception) {
                                    formError = "Google Sign-In Error: ${e.localizedMessage}"
                                }"""

replacement = """                                } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                                    formError = "No Google account found. Please add an account in Device Settings."
                                } catch (e: Exception) {
                                    formError = "Google Sign-In Error: ${e.localizedMessage}"
                                }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
