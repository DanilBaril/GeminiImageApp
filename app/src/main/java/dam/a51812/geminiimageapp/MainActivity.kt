package dam.a51812.geminiimageapp

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dam.a51812.geminiimageapp.ui.theme.GeminiImageAppTheme
import kotlinx.coroutines.launch

val images = listOf(
    R.drawable.cookie,
    R.drawable.smilebolo,
    R.drawable.ursobolo
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeminiImageAppTheme {
                GeminiImageScreen()
            }
        }
    }
}

@Composable
fun GeminiImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImage by remember { mutableIntStateOf(images[0]) }
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Response will appear here...") }
    var isLoading by remember { mutableStateOf(false) }

    val model = remember {
        GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = BuildConfig.apiKey
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("Select an image:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            images.forEach { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            width = if (selectedImage == imageRes) 3.dp else 1.dp,
                            color = if (selectedImage == imageRes) Color.Blue else Color.Gray
                        )
                        .clickable { selectedImage = imageRes }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Enter your prompt") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        try {
                            val bitmap = BitmapFactory.decodeResource(context.resources, selectedImage)
                            val inputContent = content {
                                image(bitmap)
                                text(prompt)
                            }
                            val result = model.generateContent(inputContent)
                            response = result.text ?: "No response"
                        } catch (e: Exception) {
                            response = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "Send to Gemini")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Response:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(response)
    }
}