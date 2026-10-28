package com.example.romposter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProfileStore(this)
        setContent {
            MaterialTheme {
                var profile by remember { mutableStateOf(store.load()) }
                var tab by remember { mutableIntStateOf(if (profile.token.isBlank()) 1 else 0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0, onClick = { tab = 0 },
                                icon = { Text("📝") }, label = { Text("Đăng bài") }
                            )
                            NavigationBarItem(
                                selected = tab == 1, onClick = { tab = 1 },
                                icon = { Text("⚙️") }, label = { Text("Cài đặt") }
                            )
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad)) {
                        if (tab == 0) PostScreen(profile)
                        else SettingsScreen(profile) {
                            profile = it
                            store.save(it)
                            tab = 0
                        }
                    }
                }
            }
        }
    }
}
