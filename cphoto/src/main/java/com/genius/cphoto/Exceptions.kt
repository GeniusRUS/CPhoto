package com.genius.cphoto

/**
 * Created by Genius on 03.12.2017.
 */
class CancelOperationException(@TypeRequest val typeRequest: String) : Exception(typeRequest)

class ExternalStorageWriteException : Exception()