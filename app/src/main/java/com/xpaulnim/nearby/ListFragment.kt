package com.xpaulnim.nearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import java.util.AbstractMap.SimpleEntry
import androidx.compose.foundation.lazy.items


data class SimpleResult(
    val title: String,
    val description: String?,
    val imageUrl: String? = "",
    val distance: String = "0.37 miles"
)

val results = listOf(
    SimpleResult("New York City draft riots", description = "", ""),
    SimpleResult(
        title = "Metropolitan New York Library Council",
        description = "Consortium of libraries in the New York Metropolitan area"
    ),
    SimpleResult(
        title = "Brooklyn",
        description = "Bourough in New York City and county in New York State, Unite...",
    ),
    SimpleResult(
        title = "Columbus Circle",
        description = "Monument, circle and neighbourhood in Manhattan, Ne..."
    ),
    SimpleResult(
        title = "Occupy Wall Street",
        description = "2011 protest movement"
    )
)


class ListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                LazyColumn(
                    modifier = Modifier
                        .border(width = 5.dp, color = Color.Green)

                ) {
                    items(results) { result ->
                        ResultRow(result)
                    }
                }
            }
        }
    }
}


@Composable
fun ResultRow(result: SimpleResult) {
    Row(
        modifier = Modifier
//            .border(width = 2.dp, color = Color.Red)
            .fillMaxWidth()
//            .padding(vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ab1_inversions),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .border(width = 2.dp, color = Color.Red)
                .padding(8.dp)
                .size(88.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
                .border(width = 2.dp, color = Color.Red)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.h5,
//                modifier = Modifier.
            )

            Text(
                text = result.description.orEmpty(),
                style = MaterialTheme.typography.body1,
            )

            Text(
                text = result.distance,
                modifier = Modifier
                    .border(width = 1.dp, color = Color.DarkGray)
                    .padding(4.dp)
            )
        }

    }

}
