package com.example.myapplication.data.repository

import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import com.example.myapplication.domain.model.LoggedInUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryRepositoryImplTest {

    private val dao: InventoryDao = mock()
    private val logDao: LogDao = mock()
    private val sessionManager: SessionManager = mock()
    private val repository = InventoryRepositoryImpl(dao, logDao, sessionManager)

    @Before
    fun setup() {
        whenever(sessionManager.getLoggedInUser()).thenReturn(
            LoggedInUser(
                id = "user-1",
                username = "admin",
                displayName = "Admin User",
                role = com.example.myapplication.domain.model.UserRole.ADMIN
            )
        )
    }

    @Test
    fun `addInventory generates a new code and writes a check in log`() = runTest {
        val dateSuffix = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
        whenever(dao.countItemsForCodeGeneration("GD", dateSuffix)).thenReturn(0)

        val pictureFile = File.createTempFile("inventory", ".jpg")
        val result = repository.addInventory(
            category = ItemCategory.GOODS,
            itemName = "Widget",
            itemDescription = "Sample item",
            pic = "Jane",
            picture = pictureFile,
            notes = "Initial stock",
            quantity = 3
        )

        assertTrue(result.isSuccess)
        val itemCaptor = argumentCaptor<InventoryEntity>()
        verify(dao).insertInventoryItem(itemCaptor.capture())
        assertEquals("Admin User", itemCaptor.firstValue.pic)
        assertEquals("user-1", itemCaptor.firstValue.picUserId)
        verify(logDao).insertLog(any())
    }

    @Test
    fun `checkoutInventory updates existing item and writes checkout log`() = runTest {
        val dateSuffix = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
        val pictureFile = File.createTempFile("inventory", ".jpg")
        val existingItem = InventoryEntity(
            id = "item-1",
            inventoryCode = "GD0001$dateSuffix",
            itemName = "Widget",
            category = ItemCategory.GOODS.name,
            itemDescription = "Sample item",
            quantity = 2,
            status = ItemStatus.AVAILABLE.name,
            pic = "Jane",
            picUserId = null,
            picture = pictureFile.absolutePath,
            movement = "inbound",
            notes = "Initial stock",
            createdAt = "2026-01-01 10:00:00",
            updatedAt = "2026-01-01 10:00:00"
        )

        whenever(dao.getInventoryItemById("item-1")).thenReturn(existingItem)

        val result = repository.checkoutInventory(
            id = "item-1",
            picName = "John",
            notes = "Checked out to John",
            photoFile = null
        )

        assertTrue(result.isSuccess)
        val itemCaptor = argumentCaptor<InventoryEntity>()
        verify(dao).updateInventoryItem(itemCaptor.capture())
        assertEquals("Admin User", itemCaptor.firstValue.pic)
        assertEquals("user-1", itemCaptor.firstValue.picUserId)
        verify(logDao).insertLog(any())
    }
}
