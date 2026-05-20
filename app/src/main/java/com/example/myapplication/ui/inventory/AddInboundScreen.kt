package com.example.myapplication.ui.inventory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.PrimaryOutlinedTextField
import com.example.myapplication.ui.components.PrimaryTextLabel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInboundScreen(
    viewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val operationState by viewModel.operationState.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(ItemCategory.GOODS) }
    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var pic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingPhotoFile
        if (success && captured != null) {
            photoFile?.takeIf { it != captured }?.delete()
            photoFile = captured
        } else {
            captured?.delete()
        }
        pendingPhotoFile = null
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val newFile = context.createInventoryImageFile()
            pendingPhotoFile = newFile
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
                itemName = ""; itemDescription = ""; pic = ""; notes = ""; quantity = "1"; photoFile = null
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
                title = { Text("Add Inbound Item") },
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
                                    onClick = { selectedCategory = cat; categoryDropdownExpanded = false }
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

                    PrimaryOutlinedTextField(
                        value = pic,
                        onValueChange = { pic = it },
                        label = "PIC (Person-in-Charge)",
                        leadingIcon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                            val bitmap = remember(file.absolutePath) { BitmapFactory.decodeFile(file.absolutePath) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Captured photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val newFile = context.createInventoryImageFile()
                                    pendingPhotoFile = newFile
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
                            enabled = itemName.isNotBlank() && pic.isNotBlank() && photoFile != null
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
