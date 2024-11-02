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

package com.example.marsphotos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This data class defines a Mars photo which includes an ID, and the image URL.
 */
@Serializable
data class PicsumPhoto(
    val id: String,
    @SerialName(value = "author")
    val author: String,
    @SerialName(value = "width")
    val width: Int,
    @SerialName(value = "height")
    val height: Int,
    @SerialName(value = "url")
    val url: String,
    @SerialName(value = "download_url")
    var downloadUrl: String,
    //extra field is_blurry, is_black_and_white
    var isBlurry: Boolean = false,
    var isBlackAndWhite: Boolean = false
)


