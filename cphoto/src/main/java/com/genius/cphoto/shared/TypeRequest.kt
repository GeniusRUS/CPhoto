package com.genius.cphoto.shared

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringDef

/**
 * Created by Genius on 03.12.2017.
 */

@SuppressLint("NewApi")
@StringDef(
    TypeRequest.CAMERA,
    TypeRequest.GALLERY,
    TypeRequest.COMBINE,
    TypeRequest.COMBINE_MULTIPLE,
    TypeRequest.FROM_DOCUMENT)
@Retention(AnnotationRetention.SOURCE)
annotation class TypeRequest {
    companion object {
        const val CAMERA = "CAMERA"
        const val GALLERY = "GALLERY"
        const val COMBINE = "COMBINE"
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        const val COMBINE_MULTIPLE = "COMBINE_MULTIPLE"
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        const val FROM_DOCUMENT = "FROM_DOCUMENT"
    }
}