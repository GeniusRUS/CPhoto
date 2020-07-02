package com.genius.coroutinesphoto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

import com.genius.cphoto.CRPhoto
import com.genius.cphoto.CancelOperationException
import com.genius.cphoto.NotPermissionException
import com.genius.cphoto.TypeRequest
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

        gallery.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context, this@MainActivity).requestBitmap(TypeRequest.GALLERY, 300, 300))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                } catch (e: NotPermissionException) {
                    Toast.makeText(this@MainActivity, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
                }
            }
        }

        camera.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context, this@MainActivity).requestBitmap(TypeRequest.CAMERA))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                } catch (e: NotPermissionException) {
                    Toast.makeText(this@MainActivity, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
                }
            }
        }

        combine.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context, this@MainActivity)
                        .titleCombine("Custom chooser title")
                        .excludedApplicationsFromCombine("ru.yandex.disk")
                        .requestBitmap(TypeRequest.COMBINE))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                } catch (e: NotPermissionException) {
                    Toast.makeText(this@MainActivity, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
                }
            }
        }

        combine_multiple.setOnClickListener { v ->
            clear()

            launch {
                try {
                    Toast.makeText(this@MainActivity, CRPhoto(v.context, this@MainActivity)
                        .excludedApplicationsFromCombine("ru.yandex.disk")
                        .requestMultiPath().toString(), Toast.LENGTH_LONG).show()
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                } catch (e: NotPermissionException) {
                    Toast.makeText(this@MainActivity, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
                }
            }
        }

        document.setOnClickListener { v ->
            clear()

            launch {
                try {
                    image.setImageBitmap(CRPhoto(v.context, this@MainActivity).requestBitmap(TypeRequest.FROM_DOCUMENT))
                } catch (e: CancelOperationException) {
                    Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
                } catch (e: NotPermissionException) {
                    Toast.makeText(this@MainActivity, "Permission not granted for ${e.typeRequest}", Toast.LENGTH_LONG).show()
                }
            }
        }

        fragment.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fl_container, MainFragment(), MainFragment.TAG)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun clear() {
        image.setImageBitmap(null)
    }
}
