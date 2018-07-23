package com.genius.cphoto.exceptions

/**
 * Created by Genius on 03.12.2017.
 */
class NotPermissionException(source: RequestEnum): Exception(source.name) {

    enum class RequestEnum {
        CAMERA,
        GALLERY
    }
}