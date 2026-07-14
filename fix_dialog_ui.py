import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = """                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Select Service Category: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catList.forEachIndexed { idx, catString ->
                            val active = selectedCatIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCatIndex = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catString,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))"""

replacement = """                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Select Service Category: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catList.forEachIndexed { idx, catString ->
                            val active = selectedCatIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCatIndex = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catString,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (catList[selectedCatIndex] == "Pickup and Drop") {
                        Spacer(modifier = Modifier.height(10.dp))
                        val subCatList = listOf("Wash & Fold", "Ironing", "Express Laundry")
                        var expandedSubCat by remember { mutableStateOf(false) }
                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = expandedSubCat,
                            onExpandedChange = { expandedSubCat = !expandedSubCat }
                        ) {
                            OutlinedTextField(
                                value = subCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sub-Category") },
                                trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubCat) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            androidx.compose.material3.ExposedDropdownMenu(
                                expanded = expandedSubCat,
                                onDismissRequest = { expandedSubCat = false }
                            ) {
                                subCatList.forEach { selectionOption ->
                                    androidx.compose.material3.DropdownMenuItem(
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
                }
                Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
