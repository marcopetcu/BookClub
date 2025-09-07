package com.example.bookclub

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.NavGraph.Companion.findStartDestination

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, 0)
            insets
        }

        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = host.navController
        val bottom = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Leagă meniul ca să păstreze state/label-uri
        bottom.setupWithNavController(navController)

        // 1) Când reSELECTEZI același tab (ex: ești pe ClubDetail sub Home și apeși iar Home),
        //    fă popBackStack până la rădăcina acelui tab (fragmentul cu același ID ca item-ul de menu)
        bottom.setOnItemReselectedListener { item ->
            // pop până la destinatia cu id-ul item-ului (ex.: R.id.homeFragment)
            navController.popBackStack(item.itemId, false)
        }

        // 2) Când schimbi tab-ul, navighează cu popUpTo(start) + restore state
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment,
                R.id.booksFragment,
                R.id.inboxFragment,
                R.id.profileFragment,
                R.id.clubsFragment -> {
                    navController.navigate(
                        item.itemId,
                        null,
                        navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                    true
                }
                else -> false
            }
        }

        // Arată/ascunde bottom bar unde vrei
        val hideOn = setOf(R.id.loginFragment, R.id.registerFragment)
        navController.addOnDestinationChangedListener { _, d, _ ->
            bottom.isVisible = d.id !in hideOn
        }
    }
}
