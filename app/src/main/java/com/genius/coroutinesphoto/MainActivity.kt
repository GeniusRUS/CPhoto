package com.genius.coroutinesphoto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import com.genius.cphoto.CRPhoto
import com.genius.cphoto.exceptions.CancelOperationException
import com.genius.cphoto.shared.TypeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val thumbsContent = findViewById<LinearLayout>(R.id.thumbs)
        val image = findViewById<ImageView>(R.id.image)

        findViewById<View>(R.id.get).setOnClickListener { v ->
            image.setImageBitmap(null)
            thumbsContent.removeAllViews()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.GALLERY, 300, 300))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<View>(R.id.take).setOnClickListener { v ->
            image.setImageBitmap(null)
            thumbsContent.removeAllViews()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.CAMERA))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<View>(R.id.combine).setOnClickListener { v ->
            image.setImageBitmap(null)
            thumbsContent.removeAllViews()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).titleCombine("Custom chooser title").requestBitmap(TypeRequest.COMBINE))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<View>(R.id.combine_multiple).setOnClickListener { v ->
            image.setImageBitmap(null)
            thumbsContent.removeAllViews()

            launch {
                try {
                    Toast.makeText(this@MainActivity, CRPhoto(v.context).requestMultiPath().toString(), Toast.LENGTH_LONG).show()
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<View>(R.id.document).setOnClickListener { v ->
            image.setImageBitmap(null)
            thumbsContent.removeAllViews()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.FROM_DOCUMENT))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
