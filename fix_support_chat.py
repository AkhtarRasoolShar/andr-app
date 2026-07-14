import re

with open('app/src/main/java/com/example/ui/screens/SupportChatScreen.kt', 'r') as f:
    content = f.read()

bad_block = """                                                    val request = com.example.network.GenerateContentRequest(
                                                        contents = listOf(com.example.network.Content(
                                                            parts = listOf(com.example.network.Part(text = prompt))
                                                        )),
                                                        tools = listOf(com.example.network.Tool(googleSearch = emptyMap(), googleMaps = emptyMap()))
                                                    ))
                                                        ))
                                                    )"""

good_block = """                                                    val request = com.example.network.GenerateContentRequest(
                                                        contents = listOf(com.example.network.Content(
                                                            parts = listOf(com.example.network.Part(text = prompt))
                                                        )),
                                                        tools = listOf(com.example.network.Tool(googleSearch = emptyMap(), googleMaps = emptyMap()))
                                                    )"""

content = content.replace(bad_block, good_block)

with open('app/src/main/java/com/example/ui/screens/SupportChatScreen.kt', 'w') as f:
    f.write(content)

