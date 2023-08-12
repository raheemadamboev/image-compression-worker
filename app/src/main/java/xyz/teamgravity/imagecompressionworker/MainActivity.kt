package xyz.teamgravity.imagecompressionworker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import xyz.teamgravity.imagecompressionworker.ui.theme.ImageCompressionWorkerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewmodel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkImage(intent)
        setContent {
            ImageCompressionWorkerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        viewmodel.originalImage?.let { image ->
                            Text(text = stringResource(id = R.string.uncompressed_photo))
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = image,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        viewmodel.compressedImage?.let { image ->
                            Text(text = stringResource(id = R.string.compressed_photo))
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                bitmap = image.asImageBitmap(),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkImage(intent)
    }

    private fun checkImage(intent: Intent?) {
        val image =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            else intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        if (image == null) return
        viewmodel.onProcessImage(image)
    }
}