package com.genius.cphoto.shared

import android.os.Build
import android.support.annotation.RequiresApi

/**
 * Created by Genius on 03.12.2017.
 */
enum class TypeRequest(value: Int) {
    CAMERA(1),
    GALLERY(2),
    COMBINE(3),
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    COMBINE_MULTIPLE(4),
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    FROM_DOCUMENT(5);
}