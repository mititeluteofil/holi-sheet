package com.holisheet.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object CreateInventory : Screen("inventory/create")

    object EditInventory : Screen("inventory/edit/{inventoryId}") {
        fun createRoute(inventoryId: Long) = "inventory/edit/$inventoryId"
    }

    object InventoryDetail : Screen("inventory/{inventoryId}") {
        fun createRoute(inventoryId: Long) = "inventory/$inventoryId"
    }

    object Scan : Screen("scan/{inventoryId}") {
        fun createRoute(inventoryId: Long) = "scan/$inventoryId"
    }

    /**
     * Optional query params carry pre-filled data from the scan result.
     * All params are URL-encoded before being passed.
     */
    object CreateItem : Screen("item/create/{inventoryId}?name={name}&ocrText={ocrText}&photoPath={photoPath}&labels={labels}") {
        fun createRoute(
            inventoryId: Long,
            name: String = "",
            ocrText: String = "",
            photoPath: String = "",
            labels: String = ""
        ) = "item/create/$inventoryId?name=${encode(name)}&ocrText=${encode(ocrText)}&photoPath=${encode(photoPath)}&labels=${encode(labels)}"

        private fun encode(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
    }

    object EditItem : Screen("item/edit/{itemId}") {
        fun createRoute(itemId: Long) = "item/edit/$itemId"
    }

    object ItemDetail : Screen("item/{itemId}") {
        fun createRoute(itemId: Long) = "item/$itemId"
    }
}
