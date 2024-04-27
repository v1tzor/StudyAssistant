package ru.aleshin.studyassistant.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.aleshin.studyassistant.android.R
import ru.aleshin.studyassistant.presentation.ui.AppScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_StudyAssistant)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContent { AppScreen() }
    }
}
