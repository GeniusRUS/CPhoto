package com.genius.cphoto

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.genius.cphoto.exceptions.CancelOperationException
import com.genius.cphoto.exceptions.ExternalStorageWriteException
import com.genius.cphoto.exceptions.NotPermissionException
import com.genius.cphoto.shared.TypeRequest
import com.genius.cphoto.util.CRUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Genius on 03.12.2017.
 */
class OverlapFragment : Fragment() {

    private var fileUri: Uri? = null
    private lateinit var typeRequest: String
    private var crPhoto: CRPhoto? = null

    init {
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_STORAGE_CODE && PackageManager.PERMISSION_DENIED == grantResults.getOrNull(0)) {
            crPhoto?.propagateThrowable(NotPermissionException(typeRequest))
            return
        }

        @SuppressLint("NewApi")
        when (typeRequest) {
            TypeRequest.CAMERA -> camera()
            TypeRequest.GALLERY -> gallery()
            TypeRequest.COMBINE -> combine(false)
            TypeRequest.COMBINE_MULTIPLE -> combine(true)
            TypeRequest.FROM_DOCUMENT -> document()
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (typeRequest) {
                TypeRequest.COMBINE_MULTIPLE -> if (data != null && data.clipData != null) {
                    data.clipData?.let { clipData ->
                        val uris = (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }

                        crPhoto?.onActivityResult(uris)
                    }

                    removeUnusedFile()
                } else if (data?.data != null) {
                    data.data?.let { uri ->
                        val uris = ArrayList<Uri>()
                        uris.add(uri)
                        crPhoto?.onActivityResult(uris)
                    }

                    removeUnusedFile()
                } else if (fileUri != null) {
                    fileUri?.let { uri ->
                        val uris = listOf(uri)
                        crPhoto?.onActivityResult(uris)
                    }
                } else {
                    crPhoto?.onActivityResult(fileUri)
                }
                TypeRequest.CAMERA -> crPhoto?.onActivityResult(fileUri)
                TypeRequest.GALLERY, TypeRequest.COMBINE -> if (data != null && data.data != null) {
                    crPhoto?.onActivityResult(data.data)
                    removeUnusedFile()
                } else {
                    crPhoto?.onActivityResult(fileUri)
                }
                TypeRequest.FROM_DOCUMENT -> if (data != null && data.data != null) {
                    crPhoto?.onActivityResult(data.data)
                }
            }
        } else {
            crPhoto?.propagateThrowable(CancelOperationException(typeRequest))
            removeUnusedFile()
        }
    }

    /**
     * Обрабатывает новый запрос на картинку
     * В процессе задает с помощью [setArguments] новый тип реквеста [typeRequest]
     */
    fun newRequest(@TypeRequest typeRequest: String, caller: CRPhoto) {
        val bundle = Bundle().apply {
            putString(CRPhoto.REQUEST_TYPE_EXTRA, typeRequest)
        }
        this.arguments = bundle

        handleIntent()
        this.crPhoto = caller
    }

    private fun handleIntent() {
        typeRequest = arguments?.getString(CRPhoto.REQUEST_TYPE_EXTRA) ?: return

        if (hasPermission()) {
            @SuppressLint("NewApi")
            when (typeRequest) {
                TypeRequest.GALLERY -> gallery()
                TypeRequest.CAMERA -> camera()
                TypeRequest.COMBINE -> combine(false)
                TypeRequest.COMBINE_MULTIPLE -> combine(true)
                TypeRequest.FROM_DOCUMENT -> document()
            }
        } else {
            requestPermission()
        }
    }

    @SuppressLint("InlinedApi")
    private fun document() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            .setType("image/*")
        startActivityForResult(intent, CRPhoto.REQUEST_DOCUMENT)
    }

    private fun combine(isMultiple: Boolean) {
        if (!CRUtils.isExternalStorageWritable()) {
            crPhoto?.propagateThrowable(ExternalStorageWriteException())
            return
        }

        fileUri = createImageUri()
        var intentList: MutableList<Intent> = ArrayList()
        var chooserIntent: Intent? = null
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultiple)
        }
        intentList = CRUtils.addIntentsToList(requireContext(), intentList, pickIntent)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intentList = CRUtils.addIntentsToList(requireContext(), intentList, takePhotoIntent)
        if (intentList.isNotEmpty()) {
            val title = crPhoto?.title ?: getString(R.string.picker_header)
            chooserIntent = Intent.createChooser(intentList.removeAt(intentList.size - 1), title)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray<Parcelable>())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isMultiple) {
            startActivityForResult(chooserIntent, CRPhoto.REQUEST_COMBINE_MULTIPLE)
        } else {
            startActivityForResult(chooserIntent, CRPhoto.REQUEST_COMBINE)
        }
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, CRPhoto.REQUEST_ATTACH_IMAGE)
    }

    private fun camera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            if (!CRUtils.isExternalStorageWritable()) {
                crPhoto?.propagateThrowable(ExternalStorageWriteException())
                return
            }
            fileUri = createImageUri()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(takePictureIntent, CRPhoto.REQUEST_TAKE_PICTURE)
        }
    }

    private fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_CODE)
        }
    }

    /**
     * If we not choose camera, temp file is unused and must be removed
     */
    private fun removeUnusedFile() {
        fileUri?.let {
            context?.contentResolver?.delete(it, null, null)
        }
    }

    private fun createImageUri(): Uri? {
        val contentResolver = context?.contentResolver
        val cv = ContentValues()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        cv.put(MediaStore.Images.Media.TITLE, timeStamp)
        return contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }

    companion object {
        internal const val TAG = "OverlapFragment"
        private const val REQUEST_STORAGE_CODE = 141

        fun newInstance(@TypeRequest typeRequest: String, caller: CRPhoto): OverlapFragment {
            return OverlapFragment().apply {
                val bundle = Bundle().apply {
                    putString(CRPhoto.REQUEST_TYPE_EXTRA, typeRequest)
                }
                this.arguments = bundle
                this.crPhoto = caller
            }
        }
    }
}