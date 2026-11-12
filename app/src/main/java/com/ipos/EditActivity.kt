package com.ipos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.ipos.screens.SelectImage
import com.ipos.ui.theme.LearnCompose303Theme

class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearnCompose303Theme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var uriString: String? = null
                    uriString = intent.extras?.getString("imageUri")

                    if (uriString == null)
                        uriString = intent.extras?.get(Intent.EXTRA_STREAM).toString()
                    val uri = Uri.parse(uriString)
                    EditImage(uri)
                }
            }
        }
    }
}

@Composable
fun EditImage(uri: Uri) {

    val painter = rememberAsyncImagePainter(uri)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

//pickedImage