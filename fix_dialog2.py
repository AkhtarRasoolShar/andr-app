import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target1 = """    onSave: (title: String, price: Double, stock: Int, imageUrl: String, description: String, category: String) -> Unit,
    onUploadImage: ((android.net.Uri, (String?) -> Unit) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }"""

replacement1 = """    onSave: (title: String, price: Double, stock: Int, imageUrl: String, description: String, category: String, subCategory: String?) -> Unit,
    onUploadImage: ((android.net.Uri, (String?) -> Unit) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var subCategory by remember { mutableStateOf("") }"""

target2 = """                        catList.forEachIndexed { index, cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCatIndex = index
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())"""

replacement2 = """                        catList.forEachIndexed { index, cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCatIndex = index
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
                if (catList[selectedCatIndex] == "Pickup and Drop") {
                    val subCatList = listOf("Wash & Fold", "Ironing", "Express Laundry")
                    var expandedSubCat by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedSubCat,
                        onExpandedChange = { expandedSubCat = it }
                    ) {
                        OutlinedTextField(
                            value = subCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sub-Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubCat) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubCat,
                            onDismissRequest = { expandedSubCat = false }
                        ) {
                            subCatList.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        subCategory = selectionOption
                                        expandedSubCat = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())"""

target3 = """                if (title.isNotBlank()) {
                    onSave(title, p, s, imageUrl, description, catList[selectedCatIndex])
                }
            }) {"""

replacement3 = """                if (title.isNotBlank()) {
                    val finalSubCat = if (catList[selectedCatIndex] == "Pickup and Drop") subCategory else null
                    onSave(title, p, s, imageUrl, description, catList[selectedCatIndex], finalSubCat)
                }
            }) {"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
