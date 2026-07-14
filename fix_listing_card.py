import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = """                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )"""

replacement = """                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.subCategory != null) {
                    Text(
                        text = product.subCategory,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
