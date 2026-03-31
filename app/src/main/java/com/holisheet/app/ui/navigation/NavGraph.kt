package com.holisheet.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.holisheet.app.ui.screens.home.HomeScreen
import com.holisheet.app.ui.screens.inventory.CreateEditInventoryScreen
import com.holisheet.app.ui.screens.inventory.InventoryDetailScreen
import com.holisheet.app.ui.screens.item.CreateEditItemScreen
import com.holisheet.app.ui.screens.item.ItemDetailScreen
import com.holisheet.app.ui.screens.scan.ScanScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onInventoryClick = { id -> navController.navigate(Screen.InventoryDetail.createRoute(id)) },
                onCreateInventory = { navController.navigate(Screen.CreateInventory.route) },
                onEditInventory = { id -> navController.navigate(Screen.EditInventory.createRoute(id)) }
            )
        }

        composable(Screen.CreateInventory.route) {
            CreateEditInventoryScreen(
                inventoryId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditInventory.route,
            arguments = listOf(navArgument("inventoryId") { type = NavType.LongType })
        ) { backStack ->
            CreateEditInventoryScreen(
                inventoryId = backStack.arguments?.getLong("inventoryId"),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.InventoryDetail.route,
            arguments = listOf(navArgument("inventoryId") { type = NavType.LongType })
        ) { backStack ->
            val inventoryId = backStack.arguments!!.getLong("inventoryId")
            InventoryDetailScreen(
                inventoryId = inventoryId,
                onBack = { navController.popBackStack() },
                onItemClick = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                onScanAdd = { navController.navigate(Screen.Scan.createRoute(inventoryId)) },
                onManualAdd = { navController.navigate(Screen.CreateItem.createRoute(inventoryId)) },
                onEditInventory = { navController.navigate(Screen.EditInventory.createRoute(inventoryId)) }
            )
        }

        composable(
            route = Screen.Scan.route,
            arguments = listOf(navArgument("inventoryId") { type = NavType.LongType })
        ) { backStack ->
            val inventoryId = backStack.arguments!!.getLong("inventoryId")
            ScanScreen(
                inventoryId = inventoryId,
                onBack = { navController.popBackStack() },
                onContinue = { name, ocrText, photoPath, labels ->
                    navController.navigate(
                        Screen.CreateItem.createRoute(inventoryId, name, ocrText, photoPath, labels)
                    ) {
                        // Remove scan screen from back stack so Back from CreateItem goes to InventoryDetail
                        popUpTo(Screen.Scan.createRoute(inventoryId)) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CreateItem.route,
            arguments = listOf(
                navArgument("inventoryId") { type = NavType.LongType },
                navArgument("name")      { type = NavType.StringType; defaultValue = "" },
                navArgument("ocrText")   { type = NavType.StringType; defaultValue = "" },
                navArgument("photoPath") { type = NavType.StringType; defaultValue = "" },
                navArgument("labels")    { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStack ->
            CreateEditItemScreen(
                itemId = null,
                inventoryId = backStack.arguments!!.getLong("inventoryId"),
                prefillName = backStack.arguments?.getString("name")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "",
                prefillOcrText = backStack.arguments?.getString("ocrText")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "",
                prefillPhotoPath = backStack.arguments?.getString("photoPath")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "",
                prefillLabels = backStack.arguments?.getString("labels")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStack ->
            CreateEditItemScreen(
                itemId = backStack.arguments?.getLong("itemId"),
                inventoryId = null,
                prefillName = "",
                prefillOcrText = "",
                prefillPhotoPath = "",
                prefillLabels = "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStack ->
            ItemDetailScreen(
                itemId = backStack.arguments!!.getLong("itemId"),
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.EditItem.createRoute(id)) }
            )
        }
    }
}
