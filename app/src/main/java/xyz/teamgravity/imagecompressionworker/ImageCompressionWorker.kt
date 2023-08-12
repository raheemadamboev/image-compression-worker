package xyz.teamgravity.imagecompressionworker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

class ImageCompressionWorker(
    private val context: Context,
    private val parameters: WorkerParameters,
) : CoroutineWorker(
    appContext = context,
    params = parameters
) {

    private companion object {
        const val MAX_QUALITY = 100
        const val MIN_QUALITY = 5
    }

    override suspend fun doWork(): Result {
        val image = parameters.inputData.getString(Key.IMAGE) ?: return Result.failure()
        val compression = parameters.inputData.getLong(Key.COMPRESSION_THRESHOLD, 0L)
        val uri = Uri.parse(image)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return Result.failure()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        var output: ByteArray
        var quality = MAX_QUALITY
        do {
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                output = stream.toByteArray()
                quality -= (quality * 0.1).roundToInt()
            }
        } while (output.size > compression && quality > MIN_QUALITY)

        val file = File(context.cacheDir, "${parameters.id}.jpg")
        file.writeBytes(output)

        return Result.success(workDataOf(Key.PATH to file.absolutePath))
    }

    object Key {
        const val IMAGE = "ImageCompressionWorker_keyImage"
        const val COMPRESSION_THRESHOLD = "ImageCompressionWorker_keyCompressionThreshold"
        const val PATH = "ImageCompressionWorker_keyPath"
    }
}