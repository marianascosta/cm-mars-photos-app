/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marsphotos.R
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import com.example.marsphotos.network.FirebaseService
import com.example.marsphotos.network.RollsCounter

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    picsumUiState: PicsumState,
    modifier: Modifier = Modifier,
    reloadImages: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    when (marsUiState) {
        is MarsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is MarsUiState.Success -> {
            when (picsumUiState) {
                is PicsumState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
                is PicsumState.Success ->
                    ResultScreen(marsUiState, picsumUiState, reloadImages, modifier.fillMaxWidth())
                is PicsumState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
            }
        }

        is MarsUiState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
    }
    Spacer(modifier = Modifier.size(16.dp))
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}


@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
    }
}


@Composable
fun ResultScreen(
    marsUiState: MarsUiState.Success,
    picsumState: PicsumState.Success,
    reloadImages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val picsumURL = remember { mutableStateOf(picsumState.randomPhoto.downloadUrl) }
    val grayscaleMode = remember { mutableStateOf(false) }
    val blurMode = remember { mutableStateOf(false) }
    val saveMessage = remember { mutableStateOf("") }
    val marsPhotoState = remember { mutableStateOf(marsUiState.randomPhoto) }
    val picsumPhotoState = remember { mutableStateOf(picsumState.randomPhoto) }

    picsumURL.value = buildString {
        append(picsumPhotoState.value.downloadUrl)
        if (blurMode.value || grayscaleMode.value) append("?")
        if (grayscaleMode.value) append("grayscale")
        if (grayscaleMode.value && blurMode.value) append("&")
        if (blurMode.value) append("blur")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mars photo
        Text(text = marsUiState.photos)
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(marsPhotoState.value.imgSrc)
                .crossfade(true)
                .build(),
            contentDescription = "A photo",
        )
        // Picsum photo
        Text(text = picsumState.photos)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(picsumURL.value)
                .crossfade(true)
                .build(),
            contentDescription = "A photo",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = {
                reloadImages()
                RollsCounter.incrementRolls()
            }) {
                Text(text = "Roll")
            }

            Button(onClick = { blurMode.value = !blurMode.value }) {
                Text(text = "Blur")
            }

            Button(onClick = { grayscaleMode.value = !grayscaleMode.value }) {
                Text(text = "Gray")
            }

            Button(onClick = {
                val updatedPicsumPhoto = picsumState.randomPhoto.copy(
                    downloadUrl = picsumURL.value,
                    isBlurry = blurMode.value,
                    isBlackAndWhite = grayscaleMode.value
                )
                try {
                    FirebaseService.savePhotos(
                        marsUiState.randomPhoto,
                        updatedPicsumPhoto
                    )
                    saveMessage.value = "Photos saved successfully."
                } catch (e: Exception) {
                    saveMessage.value = "Failed to save photos."
                    Log.e("ResultScreen", "Failed to save photos", e)
                }
            }) {
                Text(text = "Save")
            }
        }
        Text(
            text = "Rolls: ${RollsCounter.rolls}",
        )
        Button(onClick = {
            FirebaseService.getLastSavedPhotos { (oldMarsPhoto, oldPicsumPhoto) ->
                Log.d("ResultScreen", "oldMarsPhoto: $oldMarsPhoto")
                Log.d("ResultScreen", "oldPicsumPhoto: $oldPicsumPhoto")
                if (oldMarsPhoto != null && oldPicsumPhoto != null) {
                    marsPhotoState.value = oldMarsPhoto
                    picsumPhotoState.value = oldPicsumPhoto
                    saveMessage.value = "Last saved photos loaded successfully."
                } else {
                    saveMessage.value = "Failed to load last saved photos."
                }
            }
        }) {
            Text(text = "Get Last Saved Photos")
        }
        Text(
            text = saveMessage.value,
            modifier = Modifier.padding(16.dp)
        )
    }

}


@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MarsPhotosTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MarsPhotosTheme {
        ErrorScreen()
    }
}
