package com.example.fixit.ui.screens.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.register),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phone Number
            Text(text = stringResource(R.string.phone_number_desc), fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("") },
                    leadingIcon = { Text("+62", fontSize = 16.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = phone.isEmpty(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                    shape = RoundedCornerShape(8.dp)

                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Email
            Text(text = stringResource(R.string.email_desc), fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = email.isEmpty(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Password
            Text(text = stringResource(R.string.password_desc), fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = password.isEmpty(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Confirm Password
            Text(text = stringResource(R.string.confirm_password), fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword != password,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Sign Up Button
            Button(
                onClick = { /* Handle registration logic here */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.register), fontWeight = FontWeight.Bold)
            }

            // Or Continue with Google
            Button(
                onClick = {/* Handle Google sign-in logic here */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.continue_with_google), fontWeight = FontWeight.Bold)
            }

            // Switch to Login Screen
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.confirm_account))
                Text(
                    text = stringResource(R.string.login),
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
        }
    }
}
