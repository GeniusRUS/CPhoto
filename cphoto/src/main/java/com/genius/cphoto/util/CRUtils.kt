package com.genius.cphoto.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import com.genius.cphoto.CRPhoto.Companion.IMAGE_SIZE
import java.io.FileDescriptor
import java.io.IOException

/**
 * Created by Genius on 03.12.2017.
 */
class CRUtils {

    companion object {

        @Throws(IOException::class)
        private fun modifyOrientation(bitmap: Bitmap?, localFilePath: String?, externalFileDescriptor: FileDescriptor? = null): Bitmap {
            val ei = when {
                bitmap == null -> throw IllegalStateException("Decoded bitmap is null")
                !localFilePath.isNullOrEmpty() -> ExifInterface(localFilePath)
                externalFileDescriptor != null -> ExifInterface(externalFileDescriptor)
                else -> throw IllegalStateException("Sources is null")
            }

            return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, horizontal = true, vertical = false)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, horizontal = false, vertical = true)
                else -> bitmap
            }
        }

        /**
         * Adding multiple intents of camera and gallery
         * @receiver current context instance
         * @param intent matching intents for request from system
         */
        @Suppress("DEPRECATION")
        fun Context.getActivitiesForIntent(intent: Intent, excludedPackages: List<String>? = null): List<Intent> {
            val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(MATCH_DEFAULT_ONLY.toLong()))
            } else {
                packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY)
            }
            return activities.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (excludedPackages?.contains(packageName) == true) return@mapNotNull null
                val targetedIntent = Intent(intent)
                targetedIntent.`package` = packageName
                targetedIntent
            }
        }

        /**
         * Is external storage available for write
         * @return is available
         */
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

        private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degrees)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
            val matrix = Matrix()
            matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        @Throws(IOException::class)
        fun getBitmap(context: Context, uri: Uri, width: Int?, height: Int?): Bitmap {
            val path: String? = try {
                CRFileUtils.getPath(context, uri) // from local storage
            } catch (e: Exception) {
                null
            }

            val iOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                inSampleSize = calculateInSampleSize(this, width ?: IMAGE_SIZE, height ?: IMAGE_SIZE)
                inJustDecodeBounds = false
            }

            val original = if (path == null) {
                context.contentResolver.openInputStream(uri).use { content ->
                    BitmapFactory.decodeStream(content, null, iOptions)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )
                    ) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    val decodedBitmap = BitmapFactory.decodeFile(path, iOptions)
                    modifyOrientation(decodedBitmap, path)
                }
            }

            return original ?: throw IllegalStateException("Decoded bitmap is null")
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {

            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {

                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }
}