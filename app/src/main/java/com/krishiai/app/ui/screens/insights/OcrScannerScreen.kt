package com.krishiai.app.ui.screens.insights

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerScreen(
    navController: NavController,
    viewModel: OcrScannerViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    var flashEnabled by remember { mutableStateOf(false) }
    var camera: androidx.camera.core.Camera? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    // Live Guidance State
    var guidanceMessage by remember { mutableStateOf("Hold steady to align document") }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is OcrScanState.Success || uiState is OcrScanState.Error) {
            showBottomSheet = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermissionState.status.isGranted) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        // ImageAnalysis for live guidance (e.g. brightness check)
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    // Basic analysis to guide the user
                                    val planes = imageProxy.planes
                                    if (planes.isNotEmpty()) {
                                        val buffer = planes[0].buffer
                                        val data = ByteArray(buffer.remaining())
                                        buffer.get(data)
                                        // Sample a few pixels to determine brightness
                                        var totalLuminance = 0L
                                        for (i in 0 until data.size step 100) {
                                            totalLuminance += (data[i].toInt() and 0xFF)
                                        }
                                        val avgLuminance = if (data.isNotEmpty()) totalLuminance / (data.size / 100) else 0
                                        
                                        guidanceMessage = when {
                                            avgLuminance < 40 -> "Improve Lighting (Too Dark)"
                                            avgLuminance > 220 -> "Reduce Glare (Too Bright)"
                                            else -> "Align Document & Hold Steady"
                                        }
                                    }
                                    imageProxy.close()
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanner Overlay
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Crop area for documents (usually rectangular)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.6f)
                        .border(2.dp, Color.Green.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                )
                
                // Guidance Message
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = guidanceMessage, color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (uiState == OcrScanState.Processing) {
                    CircularProgressIndicator(color = Color.Green, modifier = Modifier.size(60.dp))
                }
            }

            // Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(
                    onClick = { 
                        flashEnabled = !flashEnabled
                        camera?.cameraControl?.enableTorch(flashEnabled)
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(if (flashEnabled) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff, "Flash", tint = Color.White)
                }
            }

            // Capture Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            if (uiState == OcrScanState.Processing) return@clickable
                            
                            val capture = imageCapture ?: return@clickable
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                                        val rotation = image.imageInfo.rotationDegrees.toFloat()
                                        image.close()
                                        viewModel.processImage(bitmap, rotation)
                                    }
                                    override fun onError(exception: ImageCaptureException) {}
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White))
                        Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = Color.Black)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Camera permission is required to scan documents.", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showBottomSheet = false
                viewModel.reset()
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                when (val result = uiState) {
                    is OcrScanState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Scan Complete",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Category: ${result.parsedData.category}", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                if (result.parsedData.npkRatio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("NPK Ratio Found: ${result.parsedData.npkRatio}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Extracted Text", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = result.rawText,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { 
                                showBottomSheet = false
                                viewModel.reset()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Scan Another Document", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    is OcrScanState.Error -> {
                        Text(
                            text = "Scan Failed",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.Red)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(result.message)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    else -> {}
                }
            }
        }
    }
}
