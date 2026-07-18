package com.example.recipe_book.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.recipe_book.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host. Every screen in the app is a Fragment inside the
 * NavHostFragment declared in activity_main.xml — this Activity itself has
 * no business logic (matches the Clean Code guideline in Section 12).
 *
 * @AndroidEntryPoint lets this Activity (and every Fragment/ViewModel it hosts)
 * receive Hilt-injected dependencies.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // NavHostFragment (declared in XML) wires itself up automatically via
        // the nav_graph.xml navigation resource — no manual FragmentTransaction code needed.
    }
}
