package com.genius.coroutinesphoto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

import com.genius.cphoto.TakeCombineImage
import com.genius.cphoto.TakeDocumentFromSaf
import com.genius.cphoto.TakeLocalPhoto
import com.genius.cphoto.TakePhotoFromCamera
import com.genius.cphoto.createImageUriNew
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val takeLocalPhoto = registerForActivityResult(TakeLocalPhoto()) { result ->
        result?.let { imageUri ->
            image.setImageURI(imageUri)
        } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
    }

    private val takePhotoLauncher = registerForActivityResult(TakePhotoFromCamera()) { result ->
        result?.let { list ->
            image.setImageURI(list)
        } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
    }

    private val combineTakePhoto = registerForActivityResult(TakeCombineImage(title = "Title")) { result ->
        result?.firstOrNull()?.let { imageUri ->
            image.setImageURI(imageUri)
        } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
    }

    private val combineMultipleTakePhoto = registerForActivityResult(TakeCombineImage(isMultiple = true, title = "Multiple title", excludedPackages = listOf("ru.yandex.disk"))) { result ->
        result?.let { list ->
            Toast.makeText(this@MainActivity, "Image count picked: " + list.size.toString(), Toast.LENGTH_LONG).show()
        } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
    }

    private val safImage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        registerForActivityResult(TakeDocumentFromSaf()) { result ->
            result?.let { imageUri ->
                image.setImageURI(imageUri)
            } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
        }
    } else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )*/

        gallery.setOnClickListener {
            clear()
            takeLocalPhoto.launch(null)
        }

        camera.setOnClickListener {
            clear()
            takePhotoLauncher.launch(createImageUriNew())
        }

        combine.setOnClickListener {
            clear()
            combineTakePhoto.launch(createImageUriNew())
        }

        combine_multiple.setOnClickListener {
            clear()
            combineMultipleTakePhoto.launch(createImageUriNew())
        }

        document.isEnabled = safImage != null
        document.setOnClickListener {
            clear()
            safImage?.launch(true)
        }
    }

    private fun clear() {
        image.setImageBitmap(null)
    }
}
