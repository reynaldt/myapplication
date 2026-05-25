package com.example.myapplication.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRoleTest {

    @Test
    fun `fromString is case insensitive and defaults to viewer`() {
        assertEquals(UserRole.ADMIN, UserRole.fromString("admin"))
        assertEquals(UserRole.STAFF, UserRole.fromString("Staff"))
        assertEquals(UserRole.VIEWER, UserRole.fromString("unknown"))
    }

    @Test
    fun `role permissions are assigned correctly`() {
        assertTrue(UserRole.ADMIN.canCheckIn())
        assertTrue(UserRole.ADMIN.canCheckOut())
        assertTrue(UserRole.ADMIN.canDeleteItem())
        assertTrue(UserRole.STAFF.canCheckIn())
        assertTrue(UserRole.STAFF.canCheckOut())
        assertFalse(UserRole.STAFF.canDeleteItem())
        assertFalse(UserRole.VIEWER.canCheckIn())
        assertFalse(UserRole.VIEWER.canCheckOut())
        assertFalse(UserRole.VIEWER.canExportReports())
        assertTrue(UserRole.VIEWER.canViewInventory())
    }
}
