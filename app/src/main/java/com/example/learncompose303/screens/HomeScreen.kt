package com.example.learncompose303.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection.Companion.Content
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.data.Dessert
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.Fruit
import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.viewmodels.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun HomeScreen() {

    val viewModel: HomeViewModel = viewModel()
    Content(homeViewModel = viewModel)

}

@Composable
fun Content(
    homeViewModel: HomeViewModel,
) {


    LaunchedEffect(key1 = true, block = {
        // we will get the student details when ever the screen is created
// Launched effect is a side effect
        homeViewModel.getStudentDetails()
    })

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f), contentAlignment = Alignment.TopCenter
        ) {
            TopContent(homeViewModel = homeViewModel)
        }
        Text(
            text = "Students List",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        BottomContent(homeViewModel = homeViewModel)


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopContent(
    homeViewModel: HomeViewModel,
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val name by homeViewModel.studentName.collectAsState()
        val studentRoll by homeViewModel.studentRollNo.collectAsState()
        val checked by homeViewModel.isChecked.collectAsState()

        val onNameEntered: (value: String) -> Unit = remember {
            return@remember homeViewModel::setStudentName
        }
        val onRollNoEntered: (value: String) -> Unit = remember {
            return@remember homeViewModel::setStudentRollNo
        }
        val onCheck: (value: Boolean) -> Unit = remember {
            return@remember homeViewModel::setChecked
        }
        val onSubmit: (value: StudentEntity) -> Unit = remember {
            return@remember homeViewModel::insertStudent
        }
        OutlinedTextField(
            value = name,
            onValueChange = {
                onNameEntered(it)
            },
            placeholder = {
                Text(text = "Enter the Name of Student")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(), maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = studentRoll, onValueChange = {
                onRollNoEntered(it)
            },
            placeholder = {
                Text(text = "Enter Roll Number")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))

        Checkbox(checked = checked, onCheckedChange = {
            onCheck(it)
        })
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = {
            onSubmit(
                StudentEntity(
                    name = name,
                    studentRollNo = studentRoll,
                    passOrFail = checked
                )
            )
        }) {
            Text(text = "Submit")
        }

    }
}

@Composable
fun BottomContent(
    homeViewModel: HomeViewModel,
) {

    val contents by homeViewModel.studentDetailsList.collectAsState()

    val mod = Modifier
        .padding(15.dp)
        .fillMaxWidth()
        .height(80.dp)

    LazyColumn(
        content = {

            items(contents) {
                val item = ImmutableStudent(it)
                StudentCard(wrapper = item, homeViewModel = homeViewModel, mod = mod)
            }
        }, modifier = Modifier.fillMaxSize()
    )


}

@Immutable
data class ImmutableStudent(val studentEntity: StudentEntity)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCard(
    wrapper: ImmutableStudent,
    homeViewModel: HomeViewModel,
    mod: Modifier,
) {


    val onCheckedChange: (value: StudentEntity) -> Unit = remember {
        return@remember homeViewModel::updateStudent
    }
    Card(
        onClick = {

        }, modifier = mod
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically

        )
        {
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {


                Text(
                    text = wrapper.studentEntity.name,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = wrapper.studentEntity.studentRollNo,
                    maxLines = 1,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {

                Checkbox(checked = wrapper.studentEntity.passOrFail, onCheckedChange = {
                    onCheckedChange(
                        StudentEntity(
                            wrapper.studentEntity.id,
                            wrapper.studentEntity.name,
                            wrapper.studentEntity.studentRollNo,
                            it
                        )
                    )
                })
            }


        }
    }
}


//old Home Screen
//@Composable
//fun HomeScreen(navController: NavController) {
//    val desserts = remember { mutableStateOf(Dessert.getAllDesserts()) }
//    val fruits = remember { mutableStateOf(Fruit.getAllFruits()) }
//    val pageSize = 5
//    var currentPage = 0
//
//    LazyColumn(modifier = Modifier.background(MyGray)) {
//        val dessertSize = desserts.value.size
//        while (dessertSize > currentPage * pageSize) {
//
//            val from = currentPage * pageSize
//            var to = (currentPage + 1) * pageSize
//            if (dessertSize < to)
//                to = dessertSize
//            val nextDesserts = desserts.value.subList(from, to)
//
//            items(nextDesserts) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 8.dp)
//                        .background(Color.White)
//                        .clickable { navController.navigate(Destinations.Detail.createRoute(it.id)) }
//                ) {
//                    Text(
//                        text = it.title,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.padding(4.dp)
//                    )
//                    Text(
//                        text = it.desc,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(4.dp)
//                    )
//                    Image(
//                        painter = painterResource(id = it.resId),
//                        contentDescription = null,
//                        modifier = Modifier.fillMaxWidth(),
//                        contentScale = ContentScale.FillWidth
//                    )
//                }
//            }
//
//            val fruitFrom = currentPage * pageSize
//            var fruitTo = (currentPage + 1) * pageSize
//            if (fruits.value.size < fruitTo)
//                fruitTo = fruits.value.size
//            val nextFruits = fruits.value.subList(fruitFrom, fruitTo)
//
//            item {
//                LazyRow(
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .height(250.dp)
//                ) {
//                    items(nextFruits) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .width(150.dp)
//                                .padding(4.dp)
//                                .clip(RoundedCornerShape(16.dp))
//                                .clickable {
//                                    navController.navigate(
//                                        Destinations.Detail.createRoute(
//                                            it.id
//                                        )
//                                    )
//                                }
//                        ) {
//                            Image(
//                                painter = painterResource(id = it.resId),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.FillHeight
//                            )
//                            Text(
//                                text = it.name,
//                                fontWeight = FontWeight.Bold,
//                                modifier = Modifier
//                                    .padding(8.dp)
//                                    .clip(RoundedCornerShape(4.dp))
//                                    .background(Color(0xaaffffff))
//                                    .padding(4.dp)
//                            )
//                        }
//                    }
//                }
//            }
//
//            currentPage++
//        }
//    }
//}

@Preview
@Composable
fun HomePrev() {
    HomeScreen()
}

