package com.genius.coroutinesphoto

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Toast
import com.genius.cphoto.CRPhoto
import com.genius.cphoto.CancelOperationException
import com.genius.cphoto.NotPermissionException
import com.genius.cphoto.TypeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    fun requestPic(scope: CoroutineScope) = scope.launch {
        try {
//            val bitmap = CRPhoto(context, this)
//                .titleCombine("Select picture from CustomImageView")
//                .requestBitmap(TypeRequest.COMBINE)
//
//            setImageBitmap(bitmap)
        } catch (e: CancelOperationException) {
            Toast.makeText(context, "Operation cancelled", Toast.LENGTH_LONG).show()
        } catch (e: NotPermissionException) {
            Toast.makeText(context, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
        }
    }
}