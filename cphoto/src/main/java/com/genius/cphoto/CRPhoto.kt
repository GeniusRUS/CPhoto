package com.genius.cphoto

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.annotation.StringRes
import android.util.Pair
import androidx.annotation.StringDef
import androidx.fragment.app.FragmentActivity
import com.genius.cphoto.util.CRFileUtils
import com.genius.cphoto.util.CRUtils
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.annotation.RequiresApi

/**
 * Created by Genius on 03.12.2017.
 */
@Suppress("UNUSED")
class CRPhoto(private val context: Context) {

    private var bitmapSizes: Pair<Int, Int>? = null
    private var publishSubject: CompletableDeferred<*>? = null
    private lateinit var response: String
    private var excludedPackages: List<String>? = null
    private val receiver: ResultReceiver by lazy {
        object : ResultReceiver(Handler(context.mainLooper)) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == OverlapFragment.SUCCESS_CODE) {
                    when {
                        resultData?.containsKey(OverlapFragment.SINGLE_PAYLOAD) == true -> resultData.getParcelable<Uri>(OverlapFragment.SINGLE_PAYLOAD)?.let { propagateResult(it) }
                        resultData?.containsKey(OverlapFragment.MULTI_PAYLOAD) == true -> resultData.getParcelableArrayList<Uri>(OverlapFragment.MULTI_PAYLOAD)?.let { propagateMultipleResult(it) }
                    }
                } else if (resultCode == OverlapFragment.ERROR_CODE) {
                    val exception = resultData?.getSerializable(OverlapFragment.ERROR_PAYLOAD) as? Exception? ?: return
                    publishSubject?.completeExceptionally(exception)
                }
            }
        }
    }

    var title: String? = null
        private set

    /**
     * Generic request for
     * @param typeRequest - selected source for bitmap
     * @return - observable that emits bitmaps
     */
    @Throws(CancelOperationException::class)
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
    suspend fun requestBitmap(@TypeRequest typeRequest: String, width: Int, height: Int): Bitmap {
        return requestBitmap(typeRequest, Pair(width, height))
    }

    /**
     * Request for list of bitmaps with default (1024) size
     * @return - observable tah emits list of scaled bitmaps
     */
    @Throws(CancelOperationException::class)
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
    suspend fun requestBitmap(@TypeRequest typeRequest: String, bitmapSize: Pair<Int, Int>): Bitmap {
        response = BITMAP
        startOverlapFragment(typeRequest)
        this.bitmapSizes = bitmapSize
        return CompletableDeferred<Bitmap>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for single uri
     * @param typeRequest - selected source for emitter
     * @return - observable that emits a single uri
     */
    @Throws(CancelOperationException::class)
    @Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made")
    suspend fun requestUri(@TypeRequest typeRequest: String): Uri {
        response = URI
        startOverlapFragment(typeRequest)
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
    suspend fun requestPath(@TypeRequest typeRequest: String): String {
        response = PATH
        startOverlapFragment(typeRequest)
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
    suspend fun requestMultiBitmap(bitmapSize: Pair<Int, Int>): List<Bitmap> {
        response = BITMAP
        startOverlapFragment(TypeRequest.COMBINE_MULTIPLE)
        this.bitmapSizes = bitmapSize
        return CompletableDeferred<List<Bitmap>>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for list of uris
     * @return - observable that emits a list of uris
     */
    @Throws(CancelOperationException::class)
    @Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made")
    suspend fun requestMultiUri(): List<Uri> {
        response = URI
        startOverlapFragment(TypeRequest.COMBINE_MULTIPLE)
        return CompletableDeferred<List<Uri>>().apply {
            publishSubject = this
        }.await()
    }

    /**
     * Request for list of paths
     * @return - observable that emits a list of paths
     */
    @Throws(CancelOperationException::class)
    suspend fun requestMultiPath(): List<String> {
        response = PATH
        startOverlapFragment(TypeRequest.COMBINE_MULTIPLE)
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

    /**
     * Start fragment for action
     * Calling [OverlapFragment.newInstance] with selected type request and CRPhoto instance
     * @param typeRequest - selected request
     */
    private fun startOverlapFragment(@TypeRequest typeRequest: String) {
        val activity = findActivityInContext(context)
        if (activity == null) {
            publishSubject?.completeExceptionally(ClassCastException("Couldn't find FragmentActivity in attached Context"))
            return
        }

        if (!activity.supportFragmentManager.isStateSaved) {
            activity.supportFragmentManager.findFragmentByTag(OverlapFragment.TAG)?.let { overlapFragment ->
                (overlapFragment as? OverlapFragment)?.newRequest(typeRequest, receiver, title)
            } ?: activity.supportFragmentManager.beginTransaction()
                .add(OverlapFragment.newInstance(typeRequest, receiver, title, excludedPackages), OverlapFragment.TAG)
                .commit()
        }
    }

    /**
     * Get the bitmap from the source by URI
     * @param uri - uri source
     * @return image in bitmap
     */
    @Throws(IOException::class)
    private fun getBitmapFromStream(uri: Uri): Bitmap? {
        return CRUtils.getBitmap(context, uri, bitmapSizes?.first, bitmapSizes?.second)
    }

    /**
     * Try to find the FragmentActivity inside current Context
     */
    private fun findActivityInContext(context: Context): FragmentActivity? {
        if (context is FragmentActivity) return context

        var currentStepContext = context
        while (currentStepContext is ContextWrapper) {
            if (currentStepContext is FragmentActivity) {
                return currentStepContext
            }
            currentStepContext = currentStepContext.baseContext
        }
        return null
    }

    /**
     * Handle result from fragment
     * @param uri - uri-result
     */
    private fun propagateResult(uri: Uri) {
        try {
            when (response) {
                BITMAP -> propagateBitmap(uri)
                URI -> propagateUri(uri)
                PATH -> propagatePath(uri)
            }
        } catch (e: Exception) {
            publishSubject?.completeExceptionally(e)
        }
    }

    /**
     * Handle multiple result from fragment
     * @param uris - uris items from fragment
     */
    private fun propagateMultipleResult(uris: List<Uri>) {
        try {
            when (response) {
                BITMAP -> propagateMultipleBitmap(uris)
                URI -> propagateMultipleUri(uris)
                PATH -> propagateMultiplePaths(uris)
            }
        } catch (e: Exception) {
            publishSubject?.completeExceptionally(e)
        }
    }

    /**
     * Handle single result from fragment
     * @param uri - uri item from fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateUri(uri: Uri) {
        (publishSubject as? CompletableDeferred<Uri>)?.complete(uri)
    }

    /**
     * Handle single result from fragment
     * @param uri - uri item from fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagatePath(uri: Uri) {
        CRFileUtils.getPath(context, uri)?.let {
            (publishSubject as? CompletableDeferred<String>)?.complete(it)
        } ?: (publishSubject as? CompletableDeferred<String>)?.completeExceptionally(
            NullPointerException("Cannot recover image for URI: $uri")
        )
    }

    /**
     * Handle multiple result from fragment
     * @param uris - uris items from fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateMultipleUri(uris: List<Uri>) {
        (publishSubject as? CompletableDeferred<List<Uri>>)?.complete(uris)
    }

    /**
     * Handle result list of paths from fragment
     * @param uris - uris of path image fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateMultiplePaths(uris: List<Uri>) {
        (publishSubject as? CompletableDeferred<List<String>>)?.let { continuation ->
            continuation.complete(uris.mapNotNull { uri -> CRFileUtils.getPath(context, uri) })
        }
    }

    /**
     * Handle single result bitmap from fragment
     * @param uriBitmap - uri for bitmap image fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateBitmap(uriBitmap: Uri) {
        getBitmapFromStream(uriBitmap)?.let {
            (publishSubject as? CompletableDeferred<Bitmap>)?.complete(it)
        } ?: publishSubject?.completeExceptionally(IllegalStateException("Bitmap is null"))

    }

    /**
     * Handle result list of bitmaps from fragment
     * @param uris - uris of bitmap image fragment
     */
    @Suppress("UNCHECKED_CAST")
    private fun propagateMultipleBitmap(uris: List<Uri>) {
        val images = uris.mapNotNull { item -> getBitmapFromStream(item) }
        (publishSubject as? CompletableDeferred<List<Bitmap>>)?.complete(images)
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
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        const val COMBINE_MULTIPLE = "COMBINE_MULTIPLE"
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        const val FROM_DOCUMENT = "FROM_DOCUMENT"
    }
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
suspend infix fun Context.takePhotoPath(@TypeRequest typeRequest: String): String {
    return CRPhoto(this).requestPath(typeRequest)
}
@Suppress("UNUSED")
suspend infix fun Context.takePhotoBitmap(@TypeRequest typeRequest: String): Bitmap {
    return CRPhoto(this).requestBitmap(typeRequest)
}

@Suppress("DEPRECATION")
@Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made", replaceWith = ReplaceWith("CRPhoto(this).requestPath(typeRequest)", "com.genius.cphoto.CRPhoto"))
suspend infix fun Context.takePhotoUri(@TypeRequest typeRequest: String): Uri {
    return CRPhoto(this).requestUri(typeRequest)
}