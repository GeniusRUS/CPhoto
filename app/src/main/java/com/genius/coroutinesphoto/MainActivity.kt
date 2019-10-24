package com.genius.coroutinesphoto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

import com.genius.cphoto.CRPhoto
import com.genius.cphoto.exceptions.CancelOperationException
import com.genius.cphoto.shared.TypeRequest
import kotlinx.android.synthetic.main.activity_main.*
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

        get.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.GALLERY, 300, 300))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        take.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.CAMERA))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        combine.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).titleCombine("Custom chooser title").requestBitmap(TypeRequest.COMBINE))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        combine_multiple.setOnClickListener { v ->
            clear()

            launch {
                try {
                    Toast.makeText(this@MainActivity, CRPhoto(v.context).requestMultiPath().toString(), Toast.LENGTH_LONG).show()
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        document.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context).requestBitmap(TypeRequest.FROM_DOCUMENT))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }

        fragment.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fl_container, MainFragment(), "FragmentTag")
                .addToBackStack(null)
                .commit()
        }
    }

    fun clear() {
        image.setImageBitmap(null)
        thumbs.removeAllViews()
    }
}
