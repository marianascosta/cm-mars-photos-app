package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.model.PicsumPhoto
import com.example.marsphotos.network.PicSumApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface PicsumState {
    data class Success(val photos: String, val randomPhoto: PicsumPhoto) : PicsumState
    object Error : PicsumState
    object Loading : PicsumState
}

class PicSumViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var picsumUiState: PicsumState by mutableStateOf(PicsumState.Loading)
        private set

    init {
        getPicSumPhotos()
    }

    fun getPicSumPhotos() {
        viewModelScope.launch {
            picsumUiState = PicsumState.Loading
            picsumUiState = try {
                val listResult = PicSumApi.retrofitService.getPhotos()
                PicsumState.Success(
                    "Success: ${listResult.size} PicSum photos retrieved",
                    listResult.random()
                )
            } catch (e: IOException) {
                PicsumState.Error
            } catch (e: HttpException) {
                PicsumState.Error
            }
        }
    }
}
