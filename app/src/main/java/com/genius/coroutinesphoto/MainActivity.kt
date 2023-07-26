package com.genius.coroutinesphoto

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import com.genius.cphoto.TakeCombineImage
import com.genius.cphoto.TakeDocumentFromSaf
import com.genius.cphoto.TakeLocalPhoto
import com.genius.cphoto.TakePhotoFromCamera
import com.genius.cphoto.createImageUriNew

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var image: ImageView
    private lateinit var gallery: Button
    private lateinit var camera: Button
    private lateinit var combine: Button
    private lateinit var combineMultiple: Button
    private lateinit var document: Button

    private val cameraPermissionCaller = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted[Manifest.permission.CAMERA] == true) {
            combineTakePhoto.launch(createImageUriNew())
        }
    }

    private val cameraMultiplePermissionCaller = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted[Manifest.permission.CAMERA] == true) {
            combineMultipleTakePhoto.launch(createImageUriNew())
        }
    }

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

    private val combineTakePhoto = registerForActivityResult(
        TakeCombineImage(
            title = {
                getString(R.string.pick_image_with)
            }
        )
    ) { result ->
        result?.firstOrNull()?.let { imageUri ->
            image.setImageURI(imageUri)
        } ?: Toast.makeText(this@MainActivity, "Operation cancelled", Toast.LENGTH_LONG).show()
    }

    private val combineMultipleTakePhoto = registerForActivityResult(
        TakeCombineImage(
            isMultiple = true,
            title = {
                getString(R.string.pick_images_with)
            },
            excludedPackages = listOf("ru.yandex.disk")
        )
    ) { result ->
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

        image = findViewById(R.id.image)
        gallery = findViewById(R.id.gallery)
        camera = findViewById(R.id.camera)
        combine = findViewById(R.id.combine)
        combineMultiple = findViewById(R.id.combine_multiple)
        document = findViewById(R.id.document)

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
            cameraPermissionCaller.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
        }

        combineMultiple.setOnClickListener {
            clear()
            cameraMultiplePermissionCaller.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
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
