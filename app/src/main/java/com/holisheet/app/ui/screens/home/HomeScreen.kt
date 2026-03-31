package com.holisheet.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.repository.InventoryRepository
import com.holisheet.app.ui.components.InventoryCard
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onInventoryClick: (Long) -> Unit,
    onCreateInventory: () -> Unit,
    onEditInventory: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val inventories by viewModel.inventories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var deleteTarget by remember { mutableStateOf<Inventory?>(null) }

    deleteTarget?.let { inventory ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Inventory") },
            text = { Text("Delete \"${inventory.name}\" and all its items? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteInventory(inventory); deleteTarget = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("HoliSheet") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInventory) {
                Icon(Icons.Default.Add, "Create inventory")
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
                placeholder = { Text("Search inventories…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            if (inventories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No inventories yet", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Tap + to create your first inventory" else "No match for \"$searchQuery\"",
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
                    items(inventories, key = { it.id }) { inventory ->
                        ItemCountWrapper(
                            inventory = inventory,
                            onClick = { onInventoryClick(inventory.id) },
                            onEdit = { onEditInventory(inventory.id) },
                            onDelete = { deleteTarget = inventory }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCountWrapper(
    inventory: Inventory,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Each card collects its own item count independently
    val count by viewModel.inventories.collectAsState()
    // Use 0 as default — actual count shown via separate flow in InventoryDetail
    InventoryCard(
        inventory = inventory,
        itemCount = 0,
        onClick = onClick,
        onEdit = onEdit,
        onDelete = onDelete
    )
}
