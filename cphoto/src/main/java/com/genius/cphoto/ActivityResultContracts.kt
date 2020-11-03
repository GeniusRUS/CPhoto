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
class TakeDocumentFromSaf : ActivityResultContract<Boolean?, Uri?>() {
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

class TakeLocalPhoto : ActivityResultContract<Void?, Uri?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (Activity.RESULT_OK == resultCode) intent?.data else null
    }
}

class TakePhotoFromCamera : ActivityResultContract<Uri?, Uri?>() {
    private var fileUri: Uri? = null
    override fun createIntent(context: Context, input: Uri?): Intent {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
        fileUri = input
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            if (!CRUtils.isExternalStorageWritable()) {
                throw ExternalStorageWriteException()
            }
        } else throw ActivityNotFoundException()
        return takePictureIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (Activity.RESULT_OK == resultCode) fileUri else null
    }
}

class TakeCombineImage(
    private val isMultiple: Boolean = false,
    private val title: String?,
    private val excludedPackages: List<String>? = null
) : ActivityResultContract<Uri, List<Uri>?>() {
    private var fileUri: Uri? = null
    override fun createIntent(context: Context, input: Uri?): Intent {
        if (!CRUtils.isExternalStorageWritable()) {
            throw ExternalStorageWriteException()
        }

        var intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultiple)
        }
        intentList = CRUtils.addIntentsToList(context, intentList, pickIntent, excludedPackages)
        fileUri = input
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentList = CRUtils.addIntentsToList(context, intentList, takePhotoIntent)
        return Intent.createChooser(intentList.removeAt(intentList.size - 1), title).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray<Parcelable>())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? {
        return if (Activity.RESULT_OK == resultCode) {
            if (intent != null && intent.clipData != null) {
                intent.clipData?.let { clipData ->
                    val uris = (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
                    ArrayList(uris)
                } ?: listOf()
            } else if (intent?.data != null) {
                intent.data?.let { uri ->
                    arrayListOf(uri)
                } ?: listOf()
            } else fileUri?.let { listOf(it) } ?: listOf()
        } else null
    }
}