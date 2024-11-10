package com.example.marsphotos.ui.screens
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyCameraScreen() {
    val context = LocalContext.current
    val outputDirectory = context.cacheDir

    CameraPreviewScreen(
        outputDirectory = outputDirectory,
        onImageCaptured = { uri ->
            Toast.makeText(context, "Image saved: $uri", Toast.LENGTH_SHORT).show()
        },
        onError = { exception ->
            Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    outputDirectory: File,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var shouldShowCamera by remember { mutableStateOf(true) }
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(shouldShowCamera) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.get().unbindAll()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (shouldShowCamera) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    val photoFile = File(
                        outputDirectory,
                        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                photoUri = Uri.fromFile(photoFile)
                                onImageCaptured(photoUri!!)
                                shouldShowCamera = false
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .width(150.dp)
            ) {
                Text("Capture")
            }
        } else {
            photoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
                Button(
                    onClick = {
                        photoUri = null
                        shouldShowCamera = true
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .width(150.dp)
                ) {
                    Text("Retake")
                }
            }
        }
    }
}

