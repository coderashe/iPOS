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
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.viewmodels.HomeViewModel

@Composable
fun LoginScreen(viewModel: HomeViewModel) {
    LaunchedEffect(Unit) {
        viewModel.getStudentDetails()
    }

    val studentName by viewModel.studentName.collectAsState()
    val studentRollNo by viewModel.studentRollNo.collectAsState()
    val loginStatus by viewModel.loginStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyGray),
        contentAlignment = Alignment.Center // Center vertically and horizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CustomHeadingMedium(
                heading = "Sign in"
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

            Spacer(modifier = Modifier.height(24.dp))

            CustomButton(
                text = "Login",
                onClick = { viewModel.loginStudent() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = loginStatus, color = Color.Red, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Social Media Sign-In Buttons
            Text(text = "Or sign in with", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                SocialSignInButton(
                    text = "Google",
                    icon = Icons.Default.Facebook,
                    backgroundColor = Color(0xFF4285F4),
                    onClick = { /* Handle Google Sign-In */ }
                )

                Spacer(modifier = Modifier.width(8.dp))

                SocialSignInButton(
                    text = "Facebook",
                    icon = Icons.Default.Facebook,
                    backgroundColor = Color(0xFF1877F2),
                    onClick = { /* Handle Facebook Sign-In */ }
                )
            }

        }
    }
}

//@Composable
//fun SignUpScreen(viewModel: HomeViewModel) {
//    val studentName by viewModel.studentName.collectAsState()
//    val studentRollNo by viewModel.studentRollNo.collectAsState()
//    val password by remember { mutableStateOf("") } // Add password state if needed
//    val signUpStatus by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Sign Up",
//            style = MaterialTheme.typography.h4,
//            modifier = Modifier.padding(vertical = 16.dp),
//            fontWeight = FontWeight.Bold
//        )
//
//        CustomTextField(
//            value = studentName,
//            onValueChange = { viewModel.setStudentName(it) },
//            label = "Student Name"
//        )
//
//        CustomTextField(
//            value = studentRollNo,
//            onValueChange = { viewModel.setStudentRollNo(it) },
//            label = "Student Roll Number"
//        )
//
//        CustomTextField(
//            value = password,
//            onValueChange = { /* Handle password change */ },
//            label = "Password",
//            isPassword = true
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        CustomButton(
//            text = "Sign Up",
//            onClick = { /* Handle sign-up logic */ }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(text = signUpStatus, color = Color.Green, fontSize = 14.sp)
//    }
//}


