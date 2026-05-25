package com.example.myapplication.data.util

import com.example.myapplication.domain.model.ItemCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryCodeGeneratorTest {

    @Test
    fun `generate returns a 12 character inventory code with current date suffix`() {
        val dateSuffix = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
        val code = InventoryCodeGenerator.generate(ItemCategory.GOODS, existingCount = 0)

        assertEquals(12, code.length)
        assertTrue(code.startsWith("GD0001"))
        assertEquals(dateSuffix, code.takeLast(6))
    }

    @Test
    fun `generate increments sequence from existing count`() {
        val code = InventoryCodeGenerator.generate(ItemCategory.CONSUMABLE, existingCount = 5)

        assertEquals(12, code.length)
        assertTrue(code.startsWith("CS0006"))
    }
}
