package com.example.myapplication.ui.inventory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.ui.components.EvidencePhoto
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.PrimaryOutlinedTextField
import com.example.myapplication.ui.components.PrimaryTextLabel
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInboundScreen(
    viewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val operationState by viewModel.operationState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var selectedCategoryName by rememberSaveable { mutableStateOf(ItemCategory.GOODS.name) }
    val selectedCategory = ItemCategory.fromString(selectedCategoryName)
    var itemName by rememberSaveable { mutableStateOf("") }
    var itemDescription by rememberSaveable { mutableStateOf("") }
    var pic by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    val photoFile = photoPath?.let(::File)

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { user -> pic = user.displayName }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingPhotoPath?.let(::File)
        if (success && captured != null) {
            captured.compressForInventoryPreview()
            photoFile?.takeIf { it.absolutePath != captured.absolutePath }?.delete()
            photoPath = captured.absolutePath
        } else {
            captured?.delete()
        }
        pendingPhotoPath = null
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val newFile = context.createInventoryImageFile()
            pendingPhotoPath = newFile.absolutePath
            takePictureLauncher.launch(
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
            )
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(operationState) {
        when (val s = operationState) {
            is OperationState.Success -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
                itemName = ""; itemDescription = ""; pic = ""; notes = ""; quantity = "1"; photoPath = null
                viewModel.resetOperationState()
                onBack()
            }
            is OperationState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.resetOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Inbound Item")
                        currentUser?.let { user ->
                            Text(
                                text = "PIC: ${user.displayName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrimaryTextLabel(text = "Item Information")

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "[${selectedCategory.code}] ${selectedCategory.label}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            ItemCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("[${cat.code}] ${cat.label}") },
                                    onClick = { selectedCategoryName = cat.name; categoryDropdownExpanded = false }
                                )
                            }
                        }
                    }

                    PrimaryOutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "Item Name",
                        leadingIcon = Icons.Default.Label,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    PrimaryOutlinedTextField(
                        value = itemDescription,
                        onValueChange = { itemDescription = it },
                        label = "Description (optional)",
                        leadingIcon = Icons.Default.Description,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = pic,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("PIC (Logged-in Profile)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    PrimaryOutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.all(Char::isDigit)) quantity = it },
                        label = "Quantity",
                        leadingIcon = Icons.Default.Numbers,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    PrimaryOutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes (optional)",
                        leadingIcon = Icons.Default.Notes,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    // Photo capture
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Photo Evidence",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        photoFile?.let { file ->
                            EvidencePhoto(
                                file = file,
                                picName = currentUser?.displayName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val newFile = context.createInventoryImageFile()
                                    pendingPhotoPath = newFile.absolutePath
                                    takePictureLauncher.launch(
                                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
                                    )
                                } else {
                                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (photoFile == null) "Take Picture" else "Retake Picture")
                        }
                    }

                    if (operationState is OperationState.Loading) {
                        Box(Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        PrimaryButton(
                            text = "ADD ITEM",
                            onClick = {
                                val image = photoFile ?: return@PrimaryButton
                                viewModel.addInventory(
                                    category = selectedCategory,
                                    itemName = itemName,
                                    itemDescription = itemDescription,
                                    pic = pic,
                                    picture = image,
                                    notes = notes,
                                    quantity = quantity.toIntOrNull() ?: 1
                                )
                            },
                            enabled = itemName.isNotBlank() && currentUser != null && photoFile != null
                        )
                    }
                }
            }
        }
    }
}

private fun Context.createInventoryImageFile(): File {
    val directory = File(cacheDir, "inventory_images").apply { mkdirs() }
    return File.createTempFile("inventory_", ".jpg", directory)
}

private fun File.compressForInventoryPreview(maxSize: Int = 1600, quality: Int = 88) {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return

    val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
    var sampleSize = 1
    while (largestSide / sampleSize > maxSize) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val decoded = BitmapFactory.decodeFile(absolutePath, options) ?: return
    val rotated = decoded.rotateFromExif(this)

    FileOutputStream(this).use { output ->
        rotated.compress(Bitmap.CompressFormat.JPEG, quality, output)
    }

    if (rotated !== decoded) decoded.recycle()
    rotated.recycle()
}

private fun Bitmap.rotateFromExif(file: File): Bitmap {
    val orientation = ExifInterface(file.absolutePath).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    if (rotation == 0f) return this

    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
