import re

with open('app/src/main/java/com/example/network/GeminiRetrofitClient.kt', 'r') as f:
    content = f.read()

tools_class = """
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
    val tools: List<Map<String, Any>>? = null
)
"""

content = re.sub(r'data class GenerateContentRequest.*?val systemInstruction: Content\? = null\n\)', tools_class.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/network/GeminiRetrofitClient.kt', 'w') as f:
    f.write(content)
