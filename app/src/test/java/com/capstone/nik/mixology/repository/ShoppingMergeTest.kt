package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.RemoteShoppingItem
import com.capstone.nik.mixology.data.ShoppingItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingMergeTest {

    private val lime = RemoteShoppingItem(id = 1, name = "Lime", drinkId = "11007")
    private val salt = RemoteShoppingItem(id = 2, name = "Salt", drinkId = "11007")
    private val mint = RemoteShoppingItem(id = 3, name = "Mint", drinkId = "17207")

    private fun row(id: Long, item: RemoteShoppingItem, checked: Boolean = item.checked) =
        ShoppingItemEntity(id = id, name = item.name, checked = checked, drinkId = item.drinkId.orEmpty(), remoteId = item.id)

    @Test
    fun keepsAdditionsAndDeletionsFromBothSides() {
        val base = listOf(lime, salt, mint)
        // Deleted Mint here; added Sugar here.
        val local = listOf(
            row(10, lime),
            row(11, salt),
            ShoppingItemEntity(id = 12, name = "Sugar"),
        )
        // Deleted Salt elsewhere; added Ice elsewhere.
        val ice = RemoteShoppingItem(id = 4, name = "Ice")
        val remote = listOf(lime, mint, ice)

        val merged = mergeShoppingEdits(base, local, remote)

        assertEquals(listOf("Lime", "Ice", "Sugar"), merged.map { it.name })
        assertEquals(listOf(10L, 0L, 12L), merged.map { it.id })
        assertEquals(listOf(1L, 4L, null), merged.map { it.remoteId })
    }

    @Test
    fun aCheckboxFlippedHereWinsOtherwiseTheServersDoes() {
        val base = listOf(lime, salt)
        val local = listOf(row(10, lime, checked = true), row(11, salt))
        val remote = listOf(lime, salt.copy(checked = true))

        val merged = mergeShoppingEdits(base, local, remote).associateBy { it.name }

        assertEquals(true, merged.getValue("Lime").checked)
        assertEquals(true, merged.getValue("Salt").checked)
    }

    @Test
    fun theServersCheckboxWinsWhenThisDeviceLeftItAlone() {
        val base = listOf(lime.copy(checked = true))
        val local = listOf(row(10, lime, checked = true))
        val remote = listOf(lime.copy(checked = false))

        assertEquals(false, mergeShoppingEdits(base, local, remote).single().checked)
    }

    @Test
    fun unsentRowsAlreadyOnTheServerAreNotDoubled() {
        val local = listOf(ShoppingItemEntity(id = 12, name = "lime", drinkId = "11007"))

        val merged = mergeShoppingEdits(emptyList(), local, listOf(lime))

        assertEquals(listOf("Lime"), merged.map { it.name })
        assertEquals(1L, merged.single().remoteId)
    }
}
