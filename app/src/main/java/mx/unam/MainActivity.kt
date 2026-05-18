package mx.unam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import mx.unam.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val calendarFragment = CalendarFragment()

    private var fragmentoActivo: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, calendarFragment, "calendar")
                .hide(calendarFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    cambiarFragmento(homeFragment)
                    true
                }

                R.id.nav_calendar -> {
                    cambiarFragmento(calendarFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun cambiarFragmento(fragmentoNuevo: Fragment) {
        if (fragmentoNuevo != fragmentoActivo) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(fragmentoActivo)
                .show(fragmentoNuevo)
                .commit()

            fragmentoActivo = fragmentoNuevo
        }
    }
}