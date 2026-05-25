# InventoryApp

A fully offline Android inventory management app built with Kotlin, Jetpack Compose, Room, Koin, and Clean Architecture.

InventoryApp helps a small team manage physical inventory from item check-in to checkout. It supports local authentication, role-based access, real-time inventory search, photo evidence with PIC and timestamp overlays, audit logs, CSV export, and accountable PIC tracking tied to the logged-in user profile.

The project focuses on production-minded Android engineering, including offline-first data handling, reactive UI state, permission-aware screens, database migrations, and testable business logic.

## What The App Does

- Lets users log in as Admin or Staff using locally seeded accounts.
- Stores all inventory data on-device with Room.
- Adds inbound inventory with category, quantity, description, notes, PIC, and photo evidence.
- Keeps the add-item form stable when returning from the Android camera flow.
- Automatically generates readable inventory codes such as `GD000120052026`.
- Checks out available items and records who handled the movement.
- Matches PIC ownership to the logged-in user profile using both display name and stable user id.
- Tracks every important action in an audit log.
- Provides real-time search and filtering by item name, code, category, status, and movement.
- Shows a dashboard with live inventory metrics.
- Exports inventory and audit logs to CSV through Android's share sheet.

## Why This Project Matters

This is not only a CRUD app. The app models real inventory workflows where accountability matters.

For example, when a staff user checks in or checks out an item, the app does not rely only on manually typed PIC text. It links the movement to the authenticated user profile by saving:

- `pic`: the display name shown in the UI
- `picUserId`: the stable user id used for reliable tracking

That makes the audit trail more trustworthy and easier to explain in a workplace system.

## Main Features

| Feature | Description |
|---|---|
| Offline-first inventory | All data is stored locally using Room. No server or runtime API key is required. |
| Authentication | Local login with seeded users and encrypted session storage. |
| Role-based access control | Admin and Staff can manage inventory. Viewer-style permissions are supported in the role model. |
| User-aware PIC tracking | Inventory movements are tied to the logged-in profile to reduce manual input errors. |
| Auto inventory code generation | Codes are generated from category, sequence number, and creation date. |
| Reactive search and filters | `StateFlow` and `flatMapLatest` keep the UI synced with filter changes and database updates. |
| Dashboard | Live counts for total, available, checked-out, lost, inbound, outbound, and category breakdowns. |
| Audit log | Important actions are stamped with item data, user identity, timestamp, notes, and optional photo path. |
| Photo evidence | Camera capture is used when adding inbound items. Previews preserve the original ratio, respect EXIF orientation, show PIC and timestamp overlays, and compress images for local storage. |
| CSV export | Inventory and logs can be exported and shared from the device. |
| Room migrations | Schema changes are handled with explicit migrations instead of destructive resets. |
| Tests | Repository and utility logic are covered with unit tests. Compose screens have instrumented test coverage. |

## Architecture

The app follows an MVVM + Repository structure with clear separation between UI, domain, data, and dependency injection layers.

```text
app/
|-- data/
|   |-- local/
|   |   |-- dao/          InventoryDao, LogDao, UserDao
|   |   |-- entity/       InventoryEntity, LogEntity, UserEntity
|   |   `-- AppDatabase   Room database, migrations, seed callback
|   |-- repository/       Auth, inventory, profile, export implementations
|   `-- util/             InventoryCodeGenerator
|-- domain/
|   |-- model/            UserRole, ItemCategory, ItemStatus, LoggedInUser
|   `-- repository/       Repository contracts
|-- ui/
|   |-- dashboard/        Dashboard screen and view model
|   |-- inventory/        Add, checkout, list, logs, and inventory view model
|   |-- login/            Login screen and view model
|   |-- components/       Shared Compose components and role guards
|   `-- navigation/       App navigation
`-- di/                   Koin modules
```

## Technical Highlights

- Kotlin + Jetpack Compose UI
- Material 3 design components
- Room database with Flow-based DAO queries
- Koin dependency injection
- `StateFlow` for lifecycle-aware UI state
- `flatMapLatest` for reactive search/filter behavior
- EncryptedSharedPreferences for local session data
- FileProvider for safe local photo sharing
- Saveable Compose state for camera round-trips, so users return to the same form after taking a photo
- EXIF-aware photo preview and JPEG compression for clear local evidence images
- Explicit Room migrations from v2 to v3 and v3 to v4
- Unit tests with JUnit, coroutines test, and Mockito Kotlin

## Role Permissions

| Permission | Admin | Staff | Viewer |
|---|:---:|:---:|:---:|
| Check in item | Yes | Yes | No |
| Checkout item | Yes | Yes | No |
| View inventory | Yes | Yes | Yes |
| View audit logs | Yes | Yes | Yes |
| Export CSV | Yes | Yes | No |
| Delete item | Yes | No | No |

Permissions are enforced in the UI with reusable role guard components, so restricted actions are hidden from users who should not access them.

## Inventory Code Format

Inventory codes are generated automatically when an item is created.

```text
GD000120052026
| |   |
| |   `-- date: 20 May 2026
| `------ sequence number: 0001
`-------- category code: GD
```

Example category prefixes:

| Category | Code |
|---|---|
| Goods | `GD` |
| Letter / Document | `LT` |
| Consumable | `CS` |
| Asset | `AS` |
| Other | `OT` |

## Database Overview

### `inventory_items`

| Column | Purpose |
|---|---|
| `id` | UUID primary key |
| `inventoryCode` | Auto-generated inventory code |
| `itemName` | Item name |
| `category` | Item category |
| `quantity` | Item quantity |
| `status` | `AVAILABLE`, `CHECKED_OUT`, or `LOST` |
| `pic` | Display name of the current person-in-charge |
| `picUserId` | Stable user id for accountable PIC tracking |
| `picture` | Local photo path |
| `movement` | `inbound` or `outbound` |
| `notes` | Optional movement notes |
| `createdAt`, `updatedAt` | Timestamps |

### `audit_logs`

| Column | Purpose |
|---|---|
| `inventoryItemId` | Related inventory item |
| `inventoryCode` | Copied item code for historical display |
| `itemName` | Copied item name for historical display |
| `action` | `CHECK_IN`, `CHECK_OUT`, `MARK_LOST`, or `DELETE` |
| `performedByUserId` | User id that performed the action |
| `performedByUsername` | Username at the time of action |
| `notes` | Optional details |
| `photoPath` | Optional photo evidence |
| `timestamp` | Action time |

## Default Accounts

The app seeds local users on first install:

```text
Username: admin
Password: admin
Role: ADMIN

Username: user
Password: user
Role: STAFF
```

## Run The Project

Requirements:

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 28+

Build:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

## Key Files

| File | Purpose |
|---|---|
| `app/src/main/java/com/example/myapplication/data/local/AppDatabase.kt` | Room database, migrations, and user seed callback |
| `app/src/main/java/com/example/myapplication/data/local/entity/InventoryEntity.kt` | Inventory schema, including PIC identity tracking |
| `app/src/main/java/com/example/myapplication/data/repository/InventoryRepositoryImpl.kt` | Inventory business logic, code generation, status transitions, and audit logging |
| `app/src/main/java/com/example/myapplication/ui/inventory/InventoryViewModel.kt` | Reactive inventory state and mutation actions |
| `app/src/main/java/com/example/myapplication/ui/inventory/AddInboundScreen.kt` | Add-item form, camera capture flow, saveable form state, and photo compression |
| `app/src/main/java/com/example/myapplication/ui/components/EvidencePhoto.kt` | EXIF-aware photo preview with PIC and timestamp overlays |
| `app/src/main/java/com/example/myapplication/ui/dashboard/DashboardViewModel.kt` | Live dashboard metrics from Room flows |
| `app/src/main/java/com/example/myapplication/ui/components/RoleGuard.kt` | Reusable role-based UI guard |
| `app/src/test/java/com/example/myapplication/data/repository/InventoryRepositoryImplTest.kt` | Repository behavior tests |

## Project Summary

InventoryApp is built to show more than screen-level implementation. It includes local authentication, permission-aware UI, offline persistence, reactive data flows, explicit database migrations, audit logging, camera/photo handling, CSV export, and tests around business rules. The PIC/profile matching and photo evidence flow emphasize accountability, data integrity, and practical mobile UX in real workplace workflows.
