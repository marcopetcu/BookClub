package com.example.bookclub

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = host.navController
        val bottom = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottom?.setupWithNavController(navController)
        bottom?.setOnItemReselectedListener { }

        val hideOn = setOf(R.id.loginFragment, R.id.registerFragment)
        navController.addOnDestinationChangedListener { _, d, _ ->
            bottom?.isVisible = d.id !in hideOn
        }

        Log.e("TAG", "onCreate")
    }

    override fun onStart() {
        super.onStart()

        Log.e("TAG", "onStart")
    }

    override fun onResume() {
        super.onResume()

        Log.e("TAG", "onResume")
    }

    override fun onPause() {
        super.onPause()

        Log.e("TAG", "onPause")
    }

    override fun onStop() {
        super.onStop()

        Log.e("TAG", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("TAG", "onDestroy")
    }
}