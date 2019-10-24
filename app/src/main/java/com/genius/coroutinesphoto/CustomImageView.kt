package com.genius.coroutinesphoto

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Toast
import com.genius.cphoto.CRPhoto
import com.genius.cphoto.exceptions.CancelOperationException
import com.genius.cphoto.shared.TypeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomImageView(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    fun requestPic() = launch {
        try {
            val bitmap = CRPhoto(context)
                .titleCombine("Select picture from CustomImageView")
                .requestBitmap(TypeRequest.COMBINE)

            setImageBitmap(bitmap)
        } catch (e: CancelOperationException) {
            Toast.makeText(context, "Operation cancelled", Toast.LENGTH_LONG).show()
        }
    }
}