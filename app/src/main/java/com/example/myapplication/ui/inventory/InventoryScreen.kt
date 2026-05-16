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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.PrimaryOutlinedTextField
import com.example.myapplication.ui.components.PrimaryTextLabel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = koinViewModel()
) {
    val state by viewModel.addInventoryState.collectAsState()
    val context = LocalContext.current

    var type by remember { mutableStateOf("good") }
    var description by remember { mutableStateOf("") }
    var pic by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedFile = pendingPhotoFile
        if (success && capturedFile != null) {
            photoFile?.takeIf { it != capturedFile }?.delete()
            photoFile = capturedFile
        } else {
            capturedFile?.delete()
        }
        pendingPhotoFile = null
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val newFile = context.createInventoryImageFile()
            val newUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                newFile
            )
            pendingPhotoFile = newFile
            takePictureLauncher.launch(newUri)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is AddInventoryState.Success -> {
                val message = (state as AddInventoryState.Success).response.message ?: "Inventory added"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                description = ""
                pic = ""
                photoFile = null
                viewModel.resetState()
            }

            is AddInventoryState.Error -> {
                Toast.makeText(context, (state as AddInventoryState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryTextLabel(text = "Add Inventory")

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
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("letter", "good").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                PrimaryOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    leadingIcon = Icons.Default.Description,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                PrimaryOutlinedTextField(
                    value = pic,
                    onValueChange = { pic = it },
                    label = "PIC",
                    leadingIcon = Icons.Default.Badge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                PhotoCaptureBox(
                    photoFile = photoFile,
                    onTakePhoto = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val newFile = context.createInventoryImageFile()
                            val newUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                newFile
                            )
                            pendingPhotoFile = newFile
                            takePictureLauncher.launch(newUri)
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                if (state is AddInventoryState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PrimaryButton(
                        text = "SUBMIT",
                        onClick = {
                            val image = photoFile ?: return@PrimaryButton
                            viewModel.addInventory(
                                type = type,
                                description = description,
                                pic = pic,
                                picture = image
                            )
                        },
                        enabled = description.isNotBlank() && pic.isNotBlank() && photoFile != null
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCaptureBox(
    photoFile: File?,
    onTakePhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Picture",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        photoFile?.let { file ->
            val bitmap = remember(file.absolutePath) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured inventory picture",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        OutlinedButton(
            onClick = onTakePhoto,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(if (photoFile == null) "Take Picture" else "Retake Picture")
            }
        }
    }
}

private fun Context.createInventoryImageFile(): File {
    val directory = File(cacheDir, "inventory_images").apply { mkdirs() }
    return File.createTempFile("inventory_", ".jpg", directory)
}
