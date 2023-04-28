package com.genius.cphoto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Pair
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringDef
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.genius.cphoto.util.CRFileUtils
import com.genius.cphoto.util.CRUtils
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Genius. 03.12.2017
 *
 * @param context - context for resolving strings or bitmap manage
 * @param caller - a class that implements [androidx.activity.result.ActivityResultRegistry] for a call. Usually, AppCompatFragment or Fragment
 */
@Suppress("UNUSED", "DEPRECATION")
@Deprecated("Please use ActivityResultApi")
class CRPhoto(private val context: Context, private val caller: ActivityResultCaller) {

    private var bitmapSizes: Pair<Int, Int>? = null
    private var publishSubject: CompletableDeferred<*>? = null
    private lateinit var response: String
    private var excludedPackages: List<String>? = null

    var title: String? = null
        private set

    /**
     * Generic request for
     * @param typeRequest - selected source for bitmap
     * @return - observable that emits bitmaps
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage()",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestBitmap(@TypeRequest typeRequest: String): Bitmap {
        return requestBitmap(typeRequest, Pair(IMAGE_SIZE, IMAGE_SIZE))
    }

    /**
     * Request for single bitmap with explicitly set of size
     * @param typeRequest - selected source for bitmap
     * @param width - width of resized bitmap
     * @param height - height of resized bitmap
     * @return - observable that emits single bitmap
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage()",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestBitmap(@TypeRequest typeRequest: String, width: Int, height: Int): Bitmap {
        return requestBitmap(typeRequest, Pair(width, height))
    }

    /**
     * Request for list of bitmaps with default (1024) size
     * @return - observable tah emits list of scaled bitmaps
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage(true)",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestMultiBitmap(): List<Bitmap> {
        return requestMultiBitmap(Pair(IMAGE_SIZE, IMAGE_SIZE))
    }

    /**
     * Request for list of bitmaps with explicitly set of size
     * @param width - width of resized bitmaps
     * @param height - height of resized bitmaps
     * @return - observable that emits list of bitmaps
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage(true)",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestMultiBitmap(width: Int, height: Int): List<Bitmap> {
        return requestMultiBitmap(Pair(width, height))
    }

    /**
     * Generic request for getting bitmap observable
     * @param typeRequest - selected source for emitter
     * @param bitmapSize - requested bitmap scale size
     * @return - explicitly scaled or not (1024 by default) bitmap
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage()",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestBitmap(@TypeRequest typeRequest: String, bitmapSize: Pair<Int, Int>): Bitmap {
        response = BITMAP
        startJob(typeRequest)
        this.bitmapSizes = bitmapSize
        return CompletableDeferred<Bitmap>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for single uri
     * *NOTE* Google Photo Content Provider forbids the use of it URI in other contexts, in addition, from which the call was made
     * @param typeRequest - selected source for emitter
     * @return - observable that emits a single uri
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage()",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestUri(@TypeRequest typeRequest: String): Uri {
        response = URI
        startJob(typeRequest)
        return CompletableDeferred<Uri>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for single path of file
     * @param typeRequest - selected source for emitter
     * @return - observable that emits a single path
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage()",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestPath(@TypeRequest typeRequest: String): String {
        response = PATH
        startJob(typeRequest)
        return CompletableDeferred<String>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for list of bitmaps with explicitly set of size
     * @param bitmapSize - requested bitmap scale size
     * @return - explicitly scaled or not (1024 by default) bitmap
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage(true)",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestMultiBitmap(bitmapSize: Pair<Int, Int>): List<Bitmap> {
        response = BITMAP
        startJob(TypeRequest.COMBINE_MULTIPLE)
        this.bitmapSizes = bitmapSize
        return CompletableDeferred<List<Bitmap>>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for list of uris
     * *NOTE* Google Photo Content Provider forbids the use of it URI in other contexts, in addition, from which the call was made
     * @return - observable that emits a list of uris
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage(true)",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestMultiUri(): List<Uri> {
        response = URI
        startJob(TypeRequest.COMBINE_MULTIPLE)
        return CompletableDeferred<List<Uri>>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for list of paths
     * @return - observable that emits a list of paths
     */
    @Throws(CancelOperationException::class)
    @Deprecated(
        message = "Please use ActivityResultApi with TakeCombineImage contract",
        replaceWith = ReplaceWith(
            expression = "TakeCombineImage(true)",
            imports = ["com.genius.cphoto.TakeCombineImage"]),
    )
    suspend fun requestMultiPath(): List<String> {
        response = PATH
        startJob(TypeRequest.COMBINE_MULTIPLE)
        return CompletableDeferred<List<String>>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Adding title to intent chooser on string
     * @param title - title in string
     * @return - parent class
     */
    fun titleCombine(title: String): CRPhoto {
        this.title = title
        return this
    }

    /**
     * Excludes selected applications from picker
     * @param appPackages - packages to exclude
     * @return - parent class
     */
    fun excludedApplicationsFromCombine(vararg appPackages: String): CRPhoto {
        this.excludedPackages = appPackages.toList()
        return this
    }

    /**
     * Adding title to intent chooser on resource id
     * @param titleId - title in resources id
     * @return - parent class
     */
    fun titleCombine(@StringRes titleId: Int): CRPhoto {
        this.title = context.getString(titleId)
        return this
    }

    @SuppressLint("NewApi")
    private fun startJob(@TypeRequest typeRequest: String) {
        if (!hasPermission()) {
            caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) startJob(typeRequest)
            }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        when (typeRequest) {
            TypeRequest.CAMERA -> context.createImageUriNew().also { createdUri ->
                caller.registerForActivityResult(TakePhotoFromCamera()) { result ->
                    if (result != null) {
                        propagateResult(result)
                    } else {
                        publishSubject?.completeExceptionally(CancelOperationException(TypeRequest.CAMERA))
                    }
                }.launch(createdUri)
            }
            TypeRequest.COMBINE -> context.createImageUriNew().also {
                caller.registerForActivityResult(TakeCombineImage(false, title, excludedPackages)) { uris ->
                    uris?.let { nonNullableUris ->
                        propagateResult(nonNullableUris.first())
                    } ?: publishSubject?.completeExceptionally(CancelOperationException(TypeRequest.COMBINE))
                }.launch(it)
            }
            TypeRequest.COMBINE_MULTIPLE -> context.createImageUriNew().also {
                caller.registerForActivityResult(TakeCombineImage(true, title, excludedPackages)) { uris ->
                    uris?.let { nonNullableUris ->
                        propagateMultipleResult(nonNullableUris)
                    } ?: publishSubject?.completeExceptionally(CancelOperationException(TypeRequest.COMBINE_MULTIPLE))
                }.launch(it)
            }
            TypeRequest.GALLERY -> caller.registerForActivityResult(TakeLocalPhoto()) { uri ->
                uri?.let {
                    propagateResult(it)
                } ?: publishSubject?.completeExceptionally(CancelOperationException(TypeRequest.GALLERY))
            }.launch(null)
            TypeRequest.FROM_DOCUMENT -> caller.registerForActivityResult(TakeDocumentFromSaf()) { uri ->
                uri?.let {
                    propagateResult(it)
                } ?: publishSubject?.completeExceptionally(CancelOperationException(TypeRequest.FROM_DOCUMENT))
            }.launch(true)
        }
    }

    /**
     * Handle result from fragment
     * @param uri - uri-result
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateResult(uri: Uri) {
        try {
            when (response) {
                URI -> (publishSubject as? CompletableDeferred<Uri>)?.complete(uri)
                BITMAP -> CRUtils.getBitmap(context, uri, bitmapSizes?.first, bitmapSizes?.second).let {
                    (publishSubject as? CompletableDeferred<Bitmap>)?.complete(it)
                }
                PATH -> CRFileUtils.getPath(context, uri)?.let {
                    (publishSubject as? CompletableDeferred<String>)?.complete(it)
                } ?: (publishSubject as? CompletableDeferred<String>)?.completeExceptionally(
                    NullPointerException("Cannot recover image for URI: $uri. Please ensure that file is in local storage")
                )
            }
        } catch (e: Exception) {
            publishSubject?.completeExceptionally(e)
        }
    }

    /**
     * Handle multiple result from fragment
     * @param uris - uris items from fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateMultipleResult(uris: List<Uri>) {
        try {
            when (response) {
                URI -> (publishSubject as? CompletableDeferred<List<Uri>>)?.complete(uris)
                BITMAP -> {
                    uris.map { item -> CRUtils.getBitmap(context, item, bitmapSizes?.first, bitmapSizes?.second) }.apply {
                        (publishSubject as? CompletableDeferred<List<Bitmap>>)?.complete(this)
                    }
                }
                PATH -> {
                    (publishSubject as? CompletableDeferred<List<String>>)?.let { continuation ->
                        continuation.complete(uris.mapNotNull { uri -> CRFileUtils.getPath(context, uri) })
                    }
                }
            }
        } catch (e: Exception) {
            publishSubject?.completeExceptionally(e)
        }
    }

    private fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val BITMAP = "BITMAP"
        private const val URI = "URI"
        private const val PATH = "PATH"

        const val IMAGE_SIZE = 1024
        const val REQUEST_ATTACH_IMAGE = 9123
        const val REQUEST_TAKE_PICTURE = 9124
        const val REQUEST_COMBINE = 9125
        const val REQUEST_COMBINE_MULTIPLE = 9126
        const val REQUEST_DOCUMENT = 9127
        const val REQUEST_TYPE_EXTRA = "request_type_extra"
        const val RECEIVER_EXTRA = "receiver_extra"
        const val TITLE_EXTRA = "title_extra"
        const val EXCLUDED_PACKAGES_EXTRA = "excluded_packages_extra"
    }
}

@SuppressLint("NewApi")
@StringDef(
    TypeRequest.CAMERA,
    TypeRequest.GALLERY,
    TypeRequest.COMBINE,
    TypeRequest.COMBINE_MULTIPLE,
    TypeRequest.FROM_DOCUMENT)
@Retention(AnnotationRetention.SOURCE)
annotation class TypeRequest {
    companion object {
        const val CAMERA = "CAMERA"
        const val GALLERY = "GALLERY"
        const val COMBINE = "COMBINE"
        const val COMBINE_MULTIPLE = "COMBINE_MULTIPLE"
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        const val FROM_DOCUMENT = "FROM_DOCUMENT"
    }
}

fun Context.createImageUriNew(authority: String = "${packageName}.cphoto.fileprovider"): Uri? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(cacheDir, "photo_$timeStamp.jpg")
    return FileProvider.getUriForFile(this, authority, file)
}

/**
 * Get thumbnails bitmap for selected scale from source
 * @param resizeValues - pair values with requested size for bitmap
 * @return - scaled bitmap
 */
@Suppress("UNUSED")
infix fun Bitmap.toThumb(resizeValues: Pair<Int, Int>): Bitmap {
    return ThumbnailUtils.extractThumbnail(this, resizeValues.first, resizeValues.second)
}

@Suppress("UNUSED")
@WorkerThread
fun Uri.toAbsolutePath(context: Context): String? = CRFileUtils.getPath(context, this)

@Suppress("UNUSED")
@WorkerThread
fun Uri.toBitmap(context: Context, width: Int? = null, height: Int? = null): Bitmap = CRUtils.getBitmap(context, this, width, height)