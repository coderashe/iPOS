package com.example.learncompose303.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.app.CustomButton
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.app.CustomTextField
import com.example.learncompose303.app.SocialSignInButton
import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.viewmodels.HomeViewModel

@Composable
fun SignUpScreen(viewModel: HomeViewModel) {

    val studentName by viewModel.studentName.collectAsState()
    val studentRollNo by viewModel.studentRollNo.collectAsState()
    var password by remember { mutableStateOf("") } // Add password state if needed
    val signUpStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
            fontWeight = FontWeight.Bold
        )

        CustomTextField(
            value = studentName,
            onValueChange = { viewModel.setStudentName(it) },
            label = "Student Name"
        )

        CustomTextField(
            value = studentRollNo,
            onValueChange = { viewModel.setStudentRollNo(it) },
            label = "Student Roll Number"
        )

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = "Sign Up",
            onClick = {
                viewModel.insertStudent(
                    studentEntity = StudentEntity(
                        0,
                        studentName,
                        studentRollNo,
                        true
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = signUpStatus, color = Color.Green, fontSize = 14.sp)
    }


}