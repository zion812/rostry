package com.rio.rostry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rio.rostry.ui.theme.RostryTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rostry.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RostryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    Column(modifier = modifier) {
        Text(
            text = "Hello $name!",
        )
        Button(onClick = { viewModel.crash() }) {
            Text("Test Crash")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RostryTheme {
        Greeting("Android")
    }
}