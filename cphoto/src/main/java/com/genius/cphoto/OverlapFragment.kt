package com.genius.cphoto

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.genius.cphoto.util.CRUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Genius on 03.12.2017.
 */
class OverlapFragment : Fragment() {

    private var fileUri: Uri? = null
    private lateinit var typeRequest: String
    private var receiver: ResultReceiver? = null

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
            receiver?.send(ERROR_CODE,
                Bundle().apply {
                    putSerializable(ERROR_PAYLOAD, NotPermissionException(typeRequest))
                }
            )
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

                        receiver?.send(SUCCESS_CODE,
                            Bundle().apply {
                                putParcelableArrayList(MULTI_PAYLOAD, ArrayList(uris))
                            }
                        )
                    }
                } else if (data?.data != null) {
                    data.data?.let { uri ->
                        receiver?.send(SUCCESS_CODE,
                            Bundle().apply {
                                putParcelableArrayList(MULTI_PAYLOAD, arrayListOf(uri))
                            }
                        )
                    }
                } else if (fileUri != null) {
                    fileUri?.let { uri ->
                        receiver?.send(SUCCESS_CODE,
                            Bundle().apply {
                                putParcelableArrayList(MULTI_PAYLOAD, arrayListOf(uri))
                            }
                        )
                    }
                } else {
                    receiver?.send(SUCCESS_CODE,
                        Bundle().apply {
                            putParcelable(SINGLE_PAYLOAD, fileUri)
                        }
                    )
                }
                TypeRequest.CAMERA -> receiver?.send(SUCCESS_CODE,
                    Bundle().apply {
                        putParcelable(SINGLE_PAYLOAD, fileUri)
                    }
                )
                TypeRequest.GALLERY, TypeRequest.COMBINE -> if (data != null && data.data != null) {
                    receiver?.send(SUCCESS_CODE,
                        Bundle().apply {
                            putParcelable(SINGLE_PAYLOAD, data.data)
                        }
                    )
                } else {
                    receiver?.send(SUCCESS_CODE,
                        Bundle().apply {
                            putParcelable(SINGLE_PAYLOAD, fileUri)
                        }
                    )
                }
                TypeRequest.FROM_DOCUMENT -> if (data != null && data.data != null) {
                    receiver?.send(SUCCESS_CODE,
                        Bundle().apply {
                            putParcelable(SINGLE_PAYLOAD, data.data)
                        }
                    )
                }
            }
        } else {
            receiver?.send(ERROR_CODE,
                Bundle().apply {
                    putSerializable(ERROR_PAYLOAD, CancelOperationException(typeRequest))
                }
            )
        }
    }

    /**
     * Обрабатывает новый запрос на картинку
     * В процессе задает с помощью [setArguments] новый тип реквеста [typeRequest]
     */
    fun newRequest(@TypeRequest typeRequest: String, receiver: ResultReceiver, title: String?, appPackages: List<String>?) {
        arguments = packageNewRequestToBundle(typeRequest, receiver, title, appPackages)
        handleIntent()
    }

    private fun handleIntent() {
        typeRequest = arguments?.getString(CRPhoto.REQUEST_TYPE_EXTRA) ?: return
        receiver = arguments?.getParcelable(CRPhoto.RECEIVER_EXTRA) as ResultReceiver? ?: return

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
            receiver?.send(ERROR_CODE,
                Bundle().apply {
                    putSerializable(ERROR_PAYLOAD, ExternalStorageWriteException())
                }
            )
            return
        }

        fileUri = createImageUri()
        var intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultiple)
        }
        intentList = CRUtils.addIntentsToList(requireContext(), intentList, pickIntent, arguments?.getStringArrayList(CRPhoto.EXCLUDED_PACKAGES_EXTRA))
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentList = CRUtils.addIntentsToList(requireContext(), intentList, takePhotoIntent)
        val chooserIntent = if (intentList.isNotEmpty()) {
            val title = arguments?.getString(CRPhoto.TITLE_EXTRA)
            Intent.createChooser(intentList.removeAt(intentList.size - 1), title).apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray<Parcelable>())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else null

        chooserIntent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isMultiple) {
                startActivityForResult(it, CRPhoto.REQUEST_COMBINE_MULTIPLE)
            } else {
                startActivityForResult(it, CRPhoto.REQUEST_COMBINE)
            }
        } ?: receiver?.send(ERROR_CODE,
            Bundle().apply {
                putSerializable(ERROR_PAYLOAD, ActivityNotFoundException())
            }
        )
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, CRPhoto.REQUEST_ATTACH_IMAGE)
    }

    private fun camera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            if (!CRUtils.isExternalStorageWritable()) {
                receiver?.send(ERROR_CODE,
                    Bundle().apply {
                        putSerializable(ERROR_PAYLOAD, ExternalStorageWriteException())
                    }
                )
                return
            }
            fileUri = createImageUri()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(takePictureIntent, CRPhoto.REQUEST_TAKE_PICTURE)
        } else receiver?.send(ERROR_CODE,
            Bundle().apply {
                putSerializable(ERROR_PAYLOAD, ActivityNotFoundException())
            }
        )
    }

    private fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_CODE)
        }
    }

    private fun createImageUri(): Uri? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context?.cacheDir,
            "$timeStamp.jpg")
        return FileProvider.getUriForFile(context ?: return null,"${BuildConfig.LIBRARY_PACKAGE_NAME}.fileprovider",file);
    }

    companion object {
        internal const val TAG = "OverlapFragment"
        private const val REQUEST_STORAGE_CODE = 141

        const val SUCCESS_CODE = 1100
        const val ERROR_CODE = 1101
        const val SINGLE_PAYLOAD = "single_payload"
        const val MULTI_PAYLOAD = "multi_payload"
        const val ERROR_PAYLOAD = "error_payload"

        fun newInstance(@TypeRequest typeRequest: String, receiver: ResultReceiver, title: String?, appPackages: List<String>?): OverlapFragment {
            return OverlapFragment().apply {
                arguments = packageNewRequestToBundle(typeRequest, receiver, title, appPackages)
            }
        }

        private fun packageNewRequestToBundle(
            @TypeRequest typeRequest: String,
            receiver: ResultReceiver,
            title: String?,
            appPackages: List<String>?): Bundle {
            return Bundle().apply {
                putString(CRPhoto.REQUEST_TYPE_EXTRA, typeRequest)
                putParcelable(CRPhoto.RECEIVER_EXTRA, receiver)
                title?.let { putString(CRPhoto.TITLE_EXTRA, it) }
                appPackages?.let { putStringArrayList(CRPhoto.EXCLUDED_PACKAGES_EXTRA, ArrayList(it)) }
            }
        }
    }
}