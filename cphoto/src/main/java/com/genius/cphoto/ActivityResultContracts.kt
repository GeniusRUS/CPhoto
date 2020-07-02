package com.genius.cphoto

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import com.genius.cphoto.util.CRUtils

@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class TakeDocumentFromSaf : ActivityResultContract<Boolean?, Uri?>() {
    override fun createIntent(context: Context, input: Boolean?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, input ?: true)
            .setType("image/*")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (Activity.RESULT_OK == resultCode) intent?.data else null
    }
}

internal class TakeLocalPhoto : ActivityResultContract<Void?, Uri?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (Activity.RESULT_OK == resultCode) intent?.data else null
    }
}

internal class TakePhotoFromCamera(private val fileUri: Uri?) : ActivityResultContract<Void?, Uri?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            if (!CRUtils.isExternalStorageWritable()) {
                throw ExternalStorageWriteException()
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        } else throw ActivityNotFoundException()
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (Activity.RESULT_OK == resultCode) fileUri else null
    }
}

internal class TakeCombineImage(private val fileUri: Uri?,
                                private val title: String?,
                                private val excludedPackages: List<String>?) : ActivityResultContract<Boolean, List<Uri>?>() {
    override fun createIntent(context: Context, input: Boolean?): Intent {
        if (!CRUtils.isExternalStorageWritable()) {
            throw ExternalStorageWriteException()
        }

        var intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, input)
        }
        intentList = CRUtils.addIntentsToList(context, intentList, pickIntent, excludedPackages)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intentList = CRUtils.addIntentsToList(context, intentList, takePhotoIntent)
        return Intent.createChooser(intentList.removeAt(intentList.size - 1), title).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray<Parcelable>())
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? {
        return if (Activity.RESULT_OK == resultCode) {
            if (intent != null && intent.clipData != null) {
                intent.clipData?.let { clipData ->
                    val uris = (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
                    ArrayList(uris)
                } ?: listOf()
//            removeUnusedFile()
            } else if (intent?.data != null) {
                intent.data?.let { uri ->
                    arrayListOf(uri)
                } ?: listOf()
//            removeUnusedFile()
            } else fileUri?.let { listOf(it) } ?: listOf()
        } else null
    }
}