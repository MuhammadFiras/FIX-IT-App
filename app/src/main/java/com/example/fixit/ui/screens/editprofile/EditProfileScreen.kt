package com.example.fixit.ui.screens.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fixit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    var name by rememberSaveable { mutableStateOf("Muhammad Firas") }
    var email by rememberSaveable { mutableStateOf("muhammadfiras332@gmail.com") }
    var phone by rememberSaveable { mutableStateOf("+62 81915356535") }

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
                            text = stringResource(R.string.edit_profile),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
                IconButton(
                    onClick = { /* TODO: Handle edit profile image */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(100.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.edit_logo), contentDescription = "Edit")
                }
            }

            // Full Name Field
            Text(text = stringResource(R.string.name), fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Email Field
            Text(text = stringResource(R.string.email_desc), fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Phone Number Field
            Text(text = stringResource(R.string.phone_number), fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF37C8B2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Save Button
            Button(
                onClick = { /* TODO: Handle save logic */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37C8B2))
            ) {
                Text(text = stringResource(R.string.save), color = Color.White)
            }
        }
    }
}
