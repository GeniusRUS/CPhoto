package com.genius.cphoto

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.support.annotation.StringRes
import android.util.Pair
import com.genius.cphoto.exceptions.CancelOperationException
import com.genius.cphoto.shared.Constants
import com.genius.cphoto.shared.ResponseType
import com.genius.cphoto.shared.ResponseType.*
import com.genius.cphoto.shared.TypeRequest
import com.genius.cphoto.util.FileUtils
import com.genius.cphoto.util.Utils
import kotlinx.coroutines.experimental.CompletableDeferred
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

/**
 * Created by Genius on 03.12.2017.
 */
class CRPhoto(context: Context) {

    private var contextWeakReference: WeakReference<Context> = WeakReference(context)
    private var bitmapSizes: Pair<Int, Int>? = null
    private var bitmapPublishSubject: CompletableDeferred<Bitmap>? = null
    private var uriPublishSubject: CompletableDeferred<Uri>? = null
    private var pathPublishSubject: CompletableDeferred<String>? = null
    private var bitmapMultiPublishSubject: CompletableDeferred<List<Bitmap>>? = null
    private var uriMultiPublishSubject: CompletableDeferred<List<Uri>>? = null
    private var pathMultiPublishSubject: CompletableDeferred<List<String>>? = null
    private lateinit var response: ResponseType

    var title: String? = null
        private set

    /**
     * Generic request for
     * @param typeRequest - selected source for bitmap
     * @return - observable that emits bitmaps
     */
    @Throws(CancelOperationException::class)
    suspend fun requestBitmap(typeRequest: TypeRequest): Bitmap {
        return requestBitmap(typeRequest, Pair(Constants.IMAGE_SIZE, Constants.IMAGE_SIZE))
    }

    /**
     * Request for single bitmap with explicitly set of size
     * @param typeRequest - selected source for bitmap
     * @param width - width of resized bitmap
     * @param height - height of resized bitmap
     * @return - observable that emits single bitmap
     */
    @Throws(CancelOperationException::class)
    suspend fun requestBitmap(typeRequest: TypeRequest, width: Int, height: Int): Bitmap {
        return requestBitmap(typeRequest, Pair(width, height))
    }

    /**
     * Request for list of bitmaps with default (1024) size
     * @return - observable tah emits list of scaled bitmaps
     */
    @Throws(CancelOperationException::class)
    suspend fun requestMultiBitmap(): List<Bitmap> {
        return requestMultiBitmap(Pair(Constants.IMAGE_SIZE, Constants.IMAGE_SIZE))
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
    suspend fun requestBitmap(typeRequest: TypeRequest, bitmapSize: Pair<Int, Int>): Bitmap {
        response = BITMAP
        startOverlapActivity(typeRequest)
        this.bitmapSizes = bitmapSize
        bitmapPublishSubject = CompletableDeferred()
        return (bitmapPublishSubject as CompletableDeferred<Bitmap>).await()
    }

    /**
     * Request for single uri
     * @param typeRequest - selected source for emitter
     * @return - observable that emits a single uri
     */
    @Throws(CancelOperationException::class)
    @Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made")
    suspend fun requestUri(typeRequest: TypeRequest): Uri {
        response = URI
        startOverlapActivity(typeRequest)
        uriPublishSubject = CompletableDeferred()
        return (uriPublishSubject as CompletableDeferred<Uri>).await()
    }

    /**
     * Request for single path of file
     * @param typeRequest - selected source for emitter
     * @return - observable that emits a single path
     */
    @Throws(CancelOperationException::class)
    suspend fun requestPath(typeRequest: TypeRequest): String {
        response = PATH
        startOverlapActivity(typeRequest)
        pathPublishSubject = CompletableDeferred()
        return (pathPublishSubject as CompletableDeferred<String>).await()
    }

    /**
     * Request for list of bitmaps with explicitly set of size
     * @param bitmapSize - requested bitmap scale size
     * @return - explicitly scaled or not (1024 by default) bitmap
     */
    @Throws(CancelOperationException::class)
    suspend fun requestMultiBitmap(bitmapSize: Pair<Int, Int>): List<Bitmap> {
        response = BITMAP
        startOverlapActivity(TypeRequest.COMBINE_MULTIPLE)
        this.bitmapSizes = bitmapSize
        bitmapMultiPublishSubject = CompletableDeferred()
        return (bitmapMultiPublishSubject as CompletableDeferred<List<Bitmap>>).await()
    }

    /**
     * Request for list of uris
     * @return - observable that emits a list of uris
     */
    @Throws(CancelOperationException::class)
    @Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made")
    suspend fun requestMultiUri(): List<Uri> {
        response = URI
        startOverlapActivity(TypeRequest.COMBINE_MULTIPLE)
        uriMultiPublishSubject = CompletableDeferred()
        return (uriMultiPublishSubject as CompletableDeferred<List<Uri>>).await()
    }

    /**
     * Request for list of paths
     * @return - observable that emits a list of paths
     */
    @Throws(CancelOperationException::class)
    suspend fun requestMultiPath(): List<String> {
        response = PATH
        startOverlapActivity(TypeRequest.COMBINE_MULTIPLE)
        pathMultiPublishSubject = CompletableDeferred()
        return (pathMultiPublishSubject as CompletableDeferred<List<String>>).await()
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
     * Adding title to intent chooser on resource id
     * @param titleId - title in resources id
     * @return - parent class
     */
    fun titleCombine(@StringRes titleId: Int): CRPhoto {
        this.title = contextWeakReference.get()?.getString(titleId)
        return this
    }

    /**
     * Start activity for action
     * Calling {@link OverlapActivity#newIntent} with selected type request and Rx2Photo instance
     * @param typeRequest - selected request
     */
    private fun startOverlapActivity(typeRequest: TypeRequest) {
        val context = contextWeakReference.get() ?: return
        context.startActivity(OverlapActivity.newIntent(context, typeRequest, this))
    }

    /**
     * Get the bitmap from the source by URI
     * @param uri - uri source
     * @return image in bitmap
     */
    @Throws(IOException::class)
    private fun getBitmapFromStream(uri: Uri): Bitmap? {
        val context = contextWeakReference.get() ?: return null
        return Utils.getBitmap(context, uri, bitmapSizes?.first, bitmapSizes?.second)
    }

    /**
     * Processing the result of selecting images by the user
     * @param uri - single uri of selected image
     */
    internal fun onActivityResult(uri: Uri?) {
        uri?.let {
            propagateResult(it)
        }
    }

    /**
     *Processing the results of selecting images by the user
     * @param uri - list of uris of selected images
     */
    internal fun onActivityResult(uri: List<Uri>) {
        propagateMultipleResult(uri)
    }

    /**
     * Handle throwable from activity
     * @param error - throwable
     */
    internal fun propagateThrowable(error: Throwable) {
        when (response) {
            BITMAP -> {
                bitmapMultiPublishSubject?.completeExceptionally(error)
                bitmapPublishSubject?.completeExceptionally(error)
            }
            URI -> {
                uriMultiPublishSubject?.completeExceptionally(error)
                uriPublishSubject?.completeExceptionally(error)
            }
            PATH -> {
                pathMultiPublishSubject?.completeExceptionally(error)
                pathPublishSubject?.completeExceptionally(error)
            }
        }
    }

    /**
     * Handle result from activity
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
            uriPublishSubject?.completeExceptionally(e)
            bitmapPublishSubject?.completeExceptionally(e)
            pathPublishSubject?.completeExceptionally(e)
        }
    }

    /**
     * Handle multiple result from activity
     * @param uris - uris items from activity
     */
    private fun propagateMultipleResult(uris: List<Uri>) {
        try {
            when (response) {
                BITMAP -> propagateMultipleBitmap(uris)
                URI -> propagateMultipleUri(uris)
                PATH -> propagateMultiplePaths(uris)
            }
        } catch (e: Exception) {
            uriPublishSubject?.completeExceptionally(e)
            bitmapPublishSubject?.completeExceptionally(e)
            pathPublishSubject?.completeExceptionally(e)
        }
    }

    /**
     * Handle single result from activity
     * @param uri - uri item from activity
     */
    private fun propagateUri(uri: Uri) {
        uriPublishSubject?.complete(uri)
    }

    /**
     * Handle single result from activity
     * @param uri - uri item from activity
     */
    private fun propagatePath(uri: Uri) {
        pathPublishSubject?.complete(FileUtils.getPath(contextWeakReference.get(), uri))
    }

    /**
     * Handle multiple result from activity
     * @param uris - uris items from activity
     */
    private fun propagateMultipleUri(uris: List<Uri>) {
        uriMultiPublishSubject?.complete(uris)
    }

    /**
     * Handle result list of paths from activity
     * @param uris - uris of path image activity
     */
    private fun propagateMultiplePaths(uris: List<Uri>) {
        pathMultiPublishSubject?.let {
            it.complete(uris.map { FileUtils.getPath(contextWeakReference.get(), it) } )
        }
    }

    /**
     * Handle single result bitmap from activity
     * @param uriBitmap - uri for bitmap image activity
     */
    private fun propagateBitmap(uriBitmap: Uri) {
        getBitmapFromStream(uriBitmap)?.let {
            bitmapPublishSubject?.complete(it)
        } ?: bitmapMultiPublishSubject?.cancel(IllegalStateException("Bitmap is null"))

    }

    /**
     * Handle result list of bitmaps from activity
     * @param uris - uris of bitmap image activity
     */
    private fun propagateMultipleBitmap(uris: List<Uri>) {
        val list = ArrayList<Bitmap>()

        for (item in uris) {
            val tmp = getBitmapFromStream(item)
            if (tmp != null) {
                list.add(tmp)
            }
        }

        bitmapMultiPublishSubject?.complete(list)
    }
}

/**
 * Get thumbnails bitmap for selected scale from source
 * @param bitmap - source bitmap for scale
 * @param resizeValues - pair values with requested size for bitmap
 * @return - scaled bitmap
 */
infix fun Bitmap.toThumb(resizeValues: Pair<Int, Int>): Bitmap {
    return ThumbnailUtils.extractThumbnail(this, resizeValues.first, resizeValues.second)
}

suspend infix fun Context.takePhotoPath(typeRequest: TypeRequest): String {
    return CRPhoto(this).requestPath(typeRequest)
}

suspend infix fun Context.takePhotoBitmap(typeRequest: TypeRequest): Bitmap {
    return CRPhoto(this).requestBitmap(typeRequest)
}

@Deprecated(message = "Because Google Photo Content Provider forbids the use of it ury in other contexts, in addition, from which the call was made")
suspend infix fun Context.takePhotoUri(typeRequest: TypeRequest): Uri {
    return CRPhoto(this).requestUri(typeRequest)
}