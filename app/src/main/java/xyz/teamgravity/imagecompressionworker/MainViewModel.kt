package xyz.teamgravity.imagecompressionworker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workmanager: WorkManager
) : ViewModel() {

    var originalImage: Uri? by mutableStateOf(null)
        private set

    var compressedImage: Bitmap? by mutableStateOf(null)
        private set

    private var workInfoLiveData: LiveData<WorkInfo>? = null

    private fun observeWorkInfo(id: UUID) {
        workInfoLiveData?.removeObserver(workInfoObserver)
        workInfoLiveData = workmanager.getWorkInfoByIdLiveData(id)
        workInfoLiveData?.observeForever(workInfoObserver)
    }

    private val workInfoObserver = Observer<WorkInfo?> { info ->
        val path = info?.outputData?.getString(ImageCompressionWorker.Key.PATH) ?: return@Observer
        compressedImage = BitmapFactory.decodeFile(path)
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun onProcessImage(image: Uri) {
        originalImage = image
        val request = OneTimeWorkRequestBuilder<ImageCompressionWorker>()
            .setInputData(
                workDataOf(
                    ImageCompressionWorker.Key.IMAGE to image.toString(),
                    ImageCompressionWorker.Key.COMPRESSION_THRESHOLD to 1024 * 20L
                )
            ).build()
        workmanager.enqueue(request)
        observeWorkInfo(request.id)
    }
}