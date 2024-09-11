package com.example.learncompose303.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.data.Dessert
import com.example.learncompose303.data.Fruit
import com.example.learncompose303.data.ListItem
import com.example.learncompose303.data.Person
import com.example.learncompose303.data.Shortcut
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyGray

@Composable
fun TheNavigationDrawer(
    navController: NavController,
    randomList: List<ListItem>,
    shortcuts: List<Shortcut>
) {
    val userImage = painterResource(id = R.drawable.user)

    Column(modifier = Modifier.background(MyGray)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            Image(
                painter = userImage, contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clip(CircleShape)
            )
            Text(text = "Start", modifier = Modifier.padding(8.dp))
        }


        Text(text = "Your Shortcuts", modifier = Modifier.padding(8.dp))

        LazyRow {
            items(randomList) {
                var shape = RoundedCornerShape(8.dp)
                var resId = 0
                var title: String? = null

                when (it::class) {
                    Dessert::class -> {
                        resId = (it as Dessert).resId
                        title = (it as Dessert).title
                    }

                    Fruit::class -> {
                        resId = (it as Fruit).resId
                        title = (it as Fruit).name
                    }

                    Person::class -> {
                        shape = CircleShape
                        resId = (it as Person).resId
                        title = (it as Person).name
                    }

                }
                Column(
                    modifier = Modifier.padding(9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = resId), contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape),
                        contentScale = ContentScale.Crop
                    )
                    title?.let { Text(text = it, fontSize = 12.sp) }
                }
            }
        }

        Text(text = "All Shortcuts", modifier = Modifier.padding(8.dp))

        LazyVerticalGrid(GridCells.Adaptive(150.dp)) {
            items(shortcuts) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = it.resId),
                        contentDescription = null,
                        tint = it.tint
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = it.title, fontSize = 12.sp)
                }
            }
        }
    }
}
