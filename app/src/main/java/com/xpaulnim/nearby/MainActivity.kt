package com.xpaulnim.nearby

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DrawerValue
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.search.SearchBar
import retrofit2.http.Body
import androidx.navigation.compose.rememberNavController

class MainActivity : FragmentActivity(R.layout.main_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NearByApp()
        }
        // ensure that the fragment is added only once, when the activity is first created
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                setReorderingAllowed(true)
////                add<MapsFragment>(R.id.fragment_container_view)
//                add<ListFragment>(R.id.fragment_container_view)
//            }
//        }
    }
}

@Composable
fun NearByApp() {
    val navController = rememberNavController()


    val materialBlue700 = Color(0xFF1976D2)
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text("Places", style = MaterialTheme.typography.h4)

                },
//                backgroundColor = materialBlue700
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Text("X")
            }
        },
//        drawerContent = { Text(text = "drawerContent") },
        content = {
            it.calculateBottomPadding()
            it.calculateTopPadding()

            NearByContent()
            SearchBar()
        },
//        bottomBar = { BottomAppBar(backgroundColor = materialBlue700) { Text("BottomAppBar") } }
    )
}

@Composable
fun NearByContent() {

}


@Composable
fun SearchBar() {
    Row {
        TextField(value = "",
            label = {
                Text(text = "Search Places")
            },
            onValueChange = {},
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = false,
            onCheckedChange = {  },
            )
    }
}