package com.xpaulnim.nearby

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit

class MainActivity : FragmentActivity(R.layout.main_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {  // ensure that the fragment is added only once, when the activity is first created
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<MapsFragment>(R.id.fragment_container_view)
            }
        }
    }
}
