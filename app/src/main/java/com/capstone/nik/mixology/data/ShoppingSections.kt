package com.capstone.nik.mixology.data

data class ShoppingSection(
    val drinkId: String,
    val drink: Drink?,
    val items: List<ShoppingItemEntity>,
) {
    fun measure(item: ShoppingItemEntity): String {
        val match = drink?.ingredients?.firstOrNull {
            it.ingredient.equals(item.name, ignoreCase = true)
        } ?: return ""
        return match.measure.trim()
    }
}

fun isOnShoppingList(drink: Drink, items: List<ShoppingItemEntity>): Boolean {
    val needed = drink.ingredients
        .map { it.ingredient.trim() }
        .filter { it.isNotEmpty() }
        .map { it.lowercase() }
        .toSet()
    if (needed.isEmpty()) return false
    val existing = items
        .filter { it.drinkId == drink.id }
        .map { it.name.trim().lowercase() }
        .toSet()
    return needed.all { it in existing }
}

fun groupShoppingSections(
    items: List<ShoppingItemEntity>,
    drinks: Map<String, Drink>,
): List<ShoppingSection> {
    return items.groupBy { it.drinkId }
        .map { (drinkId, group) ->
            val drink = drinkId.takeIf { it.isNotEmpty() }?.let { drinks[it] }
            ShoppingSection(
                drinkId = drinkId,
                drink = drink,
                items = orderShoppingItems(group, drink),
            )
        }
        .sortedWith(
            compareBy<ShoppingSection> { it.drinkId.isEmpty() }
                .thenBy { (it.drink?.name ?: it.drinkId).lowercase() }
                .thenBy { it.drinkId },
        )
}

fun orderShoppingItems(
    items: List<ShoppingItemEntity>,
    drink: Drink?,
): List<ShoppingItemEntity> {
    if (drink == null) return items.sortedBy { it.name.lowercase() }
    val order = LinkedHashMap<String, Int>()
    drink.ingredients.forEachIndexed { index, measure ->
        val key = measure.ingredient.trim().lowercase()
        if (key.isNotEmpty()) order.putIfAbsent(key, index)
    }
    return items.sortedWith(
        compareBy<ShoppingItemEntity> { order[it.name.trim().lowercase()] ?: Int.MAX_VALUE }
            .thenBy { it.name.lowercase() },
    )
}
