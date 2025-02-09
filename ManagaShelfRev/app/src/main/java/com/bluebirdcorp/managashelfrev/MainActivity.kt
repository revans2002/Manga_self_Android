package com.bluebirdcorp.managashelfrev

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bluebirdcorp.managashelfrev.ui.view.MangaListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        createFragment(MangaListFragment())
    }

    fun createFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.frameLayoutForHoldingFragments, fragment).addToBackStack(null).commit()
    }

}