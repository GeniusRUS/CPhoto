package com.genius.cphoto.exceptions

import com.genius.cphoto.shared.TypeRequest

/**
 * Created by Genius on 03.12.2017.
 */
class CancelOperationException(@TypeRequest typeRequest: String): Exception(typeRequest)