<div align="center">

# 📦 InventoryApp

### A production-ready, offline-first Android inventory management system

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-4285F4?style=flat-square&logo=jetpackcompose)
![Room](https://img.shields.io/badge/Room-2.6.1-00796B?style=flat-square&logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-brightgreen?style=flat-square)
![Build](https://img.shields.io/badge/Build-Passing-success?style=flat-square)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-blue?style=flat-square)

</div>

---

## 📱 Overview

InventoryApp is a fully offline Android application for managing physical inventory — from check-in to checkout — with role-based access control, real-time reactive search, a metrics dashboard, and a complete audit trail. Built to showcase production-level mobile engineering: clean architecture, reactive data flows, local auth with encryption, and zero external network dependencies.

> **All data lives on-device.** No server. No cloud. No API keys at runtime.

---

## ✨ Features At a Glance

| Feature | Details |
|---|---|
| 🔐 **Auth + RBAC** | 3 roles (Admin / Staff / Viewer) with SHA-256 hashed credentials in EncryptedSharedPreferences |
| 🔍 **Reactive Search** | Live search by name, inventory code, or description — zero polling via `flatMapLatest` |
| 🏷️ **Auto Inventory Codes** | 12-char format `[catcode][seq4][ddmmyy]` e.g. `GD000120052026` |
| 📊 **Dashboard** | Real-time stats (total, available, checked-out, lost) with animated category breakdown |
| 📋 **Audit Log** | Every action stamped with user identity, timestamp, item code, and notes |
| 📁 **CSV Export** | One-tap export of inventory or logs to CSV, shared via Android share sheet |
| 📷 **Photo Evidence** | Camera capture required on check-in; images stored locally and shown in cards |
| 🔄 **DB Migration** | Written Room `Migration(2→3)` — no destructive reset on existing installs |

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # InventoryDao, LogDao, UserDao (Flow-based)
│   │   ├── entity/       # InventoryEntity, LogEntity, UserEntity
│   │   └── AppDatabase   # Room v3 + migration + seed callback
│   ├── repository/       # AuthRepositoryImpl, InventoryRepositoryImpl, ExportRepository
│   └── util/             # InventoryCodeGenerator
├── domain/
│   ├── model/            # UserRole, ItemCategory, ItemStatus, LoggedInUser, SearchFilterState
│   └── repository/       # Interfaces: AuthRepository, InventoryRepository
├── ui/
│   ├── auth/             # AuthViewModel
│   ├── dashboard/        # DashboardViewModel, DashboardScreen
│   ├── inventory/        # InventoryViewModel, AddInboundScreen, CheckoutScreen, LogsScreen
│   ├── home/             # HomeScreen (search + filter)
│   ├── login/            # LoginViewModel, LoginScreen
│   └── components/       # RequireRole, InventoryItemDetailDialog, RoleGuard
└── di/                   # Koin modules: local, repository, viewModel
```

**Pattern:** MVVM + Repository + Clean Architecture layers  
**State:** `StateFlow` + `flatMapLatest` for reactive, lifecycle-aware UI  
**DI:** Koin — no code generation overhead  

---

## 🔐 Role-Based Access Control

| Permission | Admin | Staff | Viewer |
|---|:---:|:---:|:---:|
| Check-In Item | ✅ | ✅ | ❌ |
| Checkout Item | ✅ | ✅ | ❌ |
| View Inventory | ✅ | ✅ | ✅ |
| View Audit Logs | ✅ | ✅ | ✅ |
| Export CSV | ✅ | ✅ | ❌ |
| Delete Item | ✅ | ❌ | ❌ |

Permissions are enforced via a `RequireRole {}` composable wrapper — UI elements are hidden, not just disabled:

```kotlin
RequireRole({ it.canCheckOut() }) {
    Button(onClick = onCheckoutClick) { Text("Checkout") }
}
```

**Default credentials** (seeded on first install):
- `admin` / `admin` → ADMIN role
- `user` / `user` → STAFF role

---

## 🏷️ Inventory Code Format

Auto-generated on item creation — **12 characters, no user input needed:**

```
GD  0001  200526
│   │     └── ddmmyy (date of creation)
│   └──────── 4-digit sequential number
└──────────── 2-char category prefix
```

| Category | Code | Example |
|---|---|---|
| Goods | `GD` | `GD000120052026` |
| Letter / Document | `LT` | `LT000220052026` |
| Consumable | `CS` | `CS000120052026` |
| Asset | `AS` | `AS000320052026` |
| Other | `OT` | `OT000120052026` |

---

## ⚡ Reactive Data Layer

Search and filter state is managed as a `StateFlow<SearchFilterState>`. Every time the filter changes, `flatMapLatest` cancels the previous query and fires a new one — the UI always reflects the exact current database state:

```kotlin
val inventoryList: StateFlow<List<InventoryEntity>> = _filter
    .flatMapLatest { f ->
        repository.searchInventory(
            query    = f.query,
            category = f.category?.name ?: "",
            status   = f.status?.name ?: "",
            movement = f.movement ?: ""
        )
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

The SQL query uses SQLite's `LIKE` wildcard syntax and empty-string-as-wildcard convention, avoiding Kotlin-side filtering entirely.

---

## 📊 Dashboard — Live Stats

The dashboard `combine`s **11 separate `Flow<Int>` DAO queries** into a single `DashboardStats` object:

```kotlin
val stats = combine(
    dao.countAll(),
    dao.countByStatus("AVAILABLE"),
    dao.countByStatus("CHECKED_OUT"),
    dao.countByStatus("LOST"),
    dao.countInbound(),
    dao.countOutbound(),
    // ...per-category counts
) { values -> DashboardStats(...) }
```

Any insert, update, or delete in Room triggers all relevant flows automatically — no manual refresh needed.

---

## 🔒 Security

- **Password hashing:** SHA-256 via `java.security.MessageDigest` (no plain-text storage ever)
- **Session storage:** `EncryptedSharedPreferences` with `MasterKey.KeyScheme.AES256_GCM`
- **Audit log integrity:** User identity is **denormalized at write time** — logs remain accurate even if a user account is later renamed or deleted
- **FileProvider:** All photo URIs shared through `FileProvider` — no `file://` exposure

---

## 🗃️ Database Schema

### `inventory_items`
| Column | Type | Notes |
|---|---|---|
| id | TEXT PK | UUID |
| inventoryCode | TEXT | Auto-generated 12-char code |
| itemName | TEXT | Human-readable name |
| category | TEXT | GOODS / LETTER / CONSUMABLE / ASSET / OTHER |
| itemDescription | TEXT? | Optional |
| quantity | INT | Default 1 |
| status | TEXT | AVAILABLE / CHECKED_OUT / LOST |
| pic | TEXT? | Person-in-charge |
| picture | TEXT? | Absolute path to local photo |
| movement | TEXT? | inbound / outbound |
| notes | TEXT? | Optional |
| createdAt / updatedAt | TEXT? | ISO timestamp |

### `audit_logs`
| Column | Type | Notes |
|---|---|---|
| id | INT PK (autoincrement) | |
| inventoryItemId | TEXT | FK reference |
| inventoryCode | TEXT | Denormalized for display |
| itemName | TEXT | Denormalized for display |
| action | TEXT | CHECK_IN / CHECK_OUT / MARK_LOST / DELETE |
| performedByUserId | TEXT | Stamped at write time |
| performedByUsername | TEXT | Stamped at write time |
| notes / photoPath | TEXT? | Optional |
| timestamp | TEXT | ISO timestamp |

### `users`
| Column | Type | Notes |
|---|---|---|
| id | TEXT PK | UUID |
| username | TEXT | Unique login name |
| passwordHash | TEXT | SHA-256 hex |
| role | TEXT | ADMIN / STAFF / VIEWER |
| displayName | TEXT | Shown in UI |
| createdAt | TEXT | ISO timestamp |

---

## 🛠️ Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material3 |
| Architecture | ViewModel, StateFlow, Flow |
| Database | Room 2.6.1 + KSP |
| DI | Koin 3.5.6 |
| Image Loading | Coil 2.6.0 |
| Security | EncryptedSharedPreferences, SecurityCrypto |
| Navigation | Compose Navigation 2.7.7 |
| Async | Kotlin Coroutines, flatMapLatest |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+
- JDK 17

### Run
```bash
git clone https://github.com/reynaldt/inventoryapp.git
cd inventoryapp
./gradlew assembleDebug
```

Install the APK or run directly from Android Studio. The database is seeded with default users automatically on first launch.

### Default Logins
```
Username: admin    Password: admin    Role: ADMIN
Username: user     Password: user     Role: STAFF
```

---

## 📂 Key Files Reference

| File | Purpose |
|---|---|
| [`AppDatabase.kt`](app/src/main/java/com/example/myapplication/data/local/AppDatabase.kt) | Room v3, Migration 2→3, seed callback |
| [`InventoryRepositoryImpl.kt`](app/src/main/java/com/example/myapplication/data/repository/InventoryRepositoryImpl.kt) | Business logic: code gen, auto-logging, status transitions |
| [`InventoryViewModel.kt`](app/src/main/java/com/example/myapplication/ui/inventory/InventoryViewModel.kt) | Reactive search/filter via flatMapLatest |
| [`DashboardViewModel.kt`](app/src/main/java/com/example/myapplication/ui/dashboard/DashboardViewModel.kt) | 11-Flow combine → DashboardStats |
| [`AuthRepositoryImpl.kt`](app/src/main/java/com/example/myapplication/data/repository/AuthRepositoryImpl.kt) | SHA-256 credential check against Room |
| [`RoleGuard.kt`](app/src/main/java/com/example/myapplication/ui/components/RoleGuard.kt) | `RequireRole {}` permission composable |
| [`ExportRepository.kt`](app/src/main/java/com/example/myapplication/data/repository/ExportRepository.kt) | CSV export via FileProvider |

---

## 📸 Screenshots

> *(Add screenshots here — Login, Dashboard, Home with filters, Add Item, Audit Log, Checkout)*

---

<div align="center">

Built with ❤️ by **reynaldt**  
*Demonstrating offline-first architecture, reactive state management, and production-grade Android patterns.*

</div>
