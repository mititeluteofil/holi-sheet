package com.holisheet.app.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.holisheet.app.data.model.ItemWithLabels
import com.holisheet.app.ui.components.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    inventoryId: Long,
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    onScanAdd: () -> Unit,
    onManualAdd: () -> Unit,
    onEditInventory: () -> Unit,
    viewModel: InventoryDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(inventoryId) { viewModel.setInventoryId(inventoryId) }

    val inventory by viewModel.inventory.collectAsState()
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var showFab by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ItemWithLabels?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Item") },
            text = { Text("Remove \"${target.item.name}\" from this inventory?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteItem(target.item); deleteTarget = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(inventory?.name ?: "Inventory") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) { Icon(Icons.Default.Sort, "Sort") }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Newest first") }, onClick = { viewModel.onSortOrderChange(SortOrder.DATE_DESC); showSortMenu = false },
                                trailingIcon = { if (sortOrder == SortOrder.DATE_DESC) Icon(Icons.Default.Check, null) })
                            DropdownMenuItem(text = { Text("Name A→Z") }, onClick = { viewModel.onSortOrderChange(SortOrder.NAME_ASC); showSortMenu = false },
                                trailingIcon = { if (sortOrder == SortOrder.NAME_ASC) Icon(Icons.Default.Check, null) })
                            DropdownMenuItem(text = { Text("Quantity") }, onClick = { viewModel.onSortOrderChange(SortOrder.QUANTITY_DESC); showSortMenu = false },
                                trailingIcon = { if (sortOrder == SortOrder.QUANTITY_DESC) Icon(Icons.Default.Check, null) })
                        }
                    }
                    IconButton(onClick = onEditInventory) { Icon(Icons.Default.Edit, "Edit inventory") }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showFab) {
                    SmallFloatingActionButton(onClick = { showFab = false; onScanAdd() }) {
                        Icon(Icons.Default.CameraAlt, "Scan & Add")
                    }
                    SmallFloatingActionButton(onClick = { showFab = false; onManualAdd() }) {
                        Icon(Icons.Default.Add, "Add manually")
                    }
                }
                FloatingActionButton(onClick = { showFab = !showFab }) {
                    Icon(if (showFab) Icons.Default.Close else Icons.Default.Add, "Add item")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search items…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "No items yet" else "No match",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Tap + to scan or add an item" else "Try a different search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.item.id }) { itemWithLabels ->
                        SwipeToDeleteWrapper(
                            onDelete = { deleteTarget = itemWithLabels }
                        ) {
                            ItemCard(
                                itemWithLabels = itemWithLabels,
                                onClick = { onItemClick(itemWithLabels.item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWrapper(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        content()
    }
}
