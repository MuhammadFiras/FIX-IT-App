package com.example.fixit.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fixit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreen(
    name: String,
    phone: String,
    description: String
) {
    // State for TextFields
    var location by rememberSaveable { mutableStateOf("") }
    var customerName by rememberSaveable { mutableStateOf(name) }
    var customerPhone by rememberSaveable { mutableStateOf(phone) }
    var serviceDescription by rememberSaveable { mutableStateOf(description) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.order_detail_title),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google maps placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with google maps later
                    Image(
                        painter = painterResource(id = R.drawable.logo_google_map),
                        contentDescription = "Google Map Placeholder",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            // Lokasi Section with TextField
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.location), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.location)) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF37C8B2),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Customer Details Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.customer_detail), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Name Field with TextField
                    Text(text = stringResource(R.string.name), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.name)) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF37C8B2),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone Number Field with TextField
                    Text(text = stringResource(R.string.phone_number), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.phone_number)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF37C8B2),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Service Description Field with TextField
                    Text(text = stringResource(R.string.service_description), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextField(
                            value = serviceDescription,
                            onValueChange = { serviceDescription = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            placeholder = { Text(text = stringResource(R.string.description)) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF37C8B2),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Order Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Handle Order */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37C8B2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = stringResource(R.string.order), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
