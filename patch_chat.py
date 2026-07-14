import re

with open('app/src/main/java/com/example/ui/screens/SupportChatScreen.kt', 'r') as f:
    content = f.read()

new_request = """                                                    val request = com.example.network.GenerateContentRequest(
                                                        contents = listOf(com.example.network.Content(
                                                            parts = listOf(com.example.network.Part(text = prompt))
                                                        )),
                                                        tools = listOf(com.example.network.Tool(googleSearch = emptyMap(), googleMaps = emptyMap()))
                                                    )"""

content = re.sub(r'                                                    val request = com\.example\.network\.GenerateContentRequest\([^)]+\)', new_request, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/SupportChatScreen.kt', 'w') as f:
    f.write(content)
