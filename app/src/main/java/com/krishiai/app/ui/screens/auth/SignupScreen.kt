package com.krishiai.app.ui.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.ui.components.PrimaryButton
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.utils.KarnatakaDataHelper
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val selectedRole by authViewModel.selectedRole.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Location dropdown states
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedTaluk by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") } // Buyers
    var businessName by remember { mutableStateOf("") } // Optional Farm Name/Shop Name

    var districtExpanded by remember { mutableStateOf(false) }
    var talukExpanded by remember { mutableStateOf(false) }

    // Image upload states
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedBytes by remember { mutableStateOf<ByteArray?>(null) }

    var validationError by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBytes = inputStream?.readBytes()
                inputStream?.close()

                if (originalBytes != null) {
                    // Compress image bytes to JPEG under 200KB before network transfer
                    val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                    profileBitmap = bitmap
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    compressedBytes = outputStream.toByteArray()
                }
            } catch (e: Exception) {
                validationError = "Error parsing selected image: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.uid.isNotEmpty() && user.role.isNotEmpty()) {
                val route = if (user.role.equals("Farmer", ignoreCase = true)) {
                    Screen.FarmerMain.route
                } else {
                    Screen.BuyerMain.route
                }
                navController.navigate(route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                    popUpTo(Screen.WelcomeRoleSelection.route) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                validationError = (authState as AuthState.Error).message
            }
            else -> {
                validationError = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Back Navigation Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Header Title
        Text(
            text = "Create $selectedRole Account",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Join the KrishiAI agricultural network",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Profile Photo selector layout
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap!!.asImageBitmap(),
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        Text(
            text = "Add Profile Photo (Optional)",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Mandatories
        OutlinedTextField(
            value = fullName,
            onValueChange = { 
                fullName = it
                authViewModel.clearError()
            },
            label = { Text("Full Name *") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { 
                if (it.length <= 10) {
                    mobileNumber = it
                    authViewModel.clearError()
                }
            },
            label = { Text("Mobile Number *") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                authViewModel.clearError()
            },
            label = { Text("Email Address *") },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                authViewModel.clearError()
            },
            label = { Text("Password * (Min 6 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Karnataka District Selection Dropdown (Shared)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedDistrict,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Karnataka District *") },
                leadingIcon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    IconButton(onClick = { districtExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { districtExpanded = true },
                shape = RoundedCornerShape(12.dp)
            )
            DropdownMenu(
                expanded = districtExpanded,
                onDismissRequest = { districtExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                KarnatakaDataHelper.getDistricts().forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district) },
                        onClick = {
                            selectedDistrict = district
                            selectedTaluk = "" // Reset taluk upon district shift
                            districtExpanded = false
                        }
                    )
                }
            }
        }

        // Role-Specific Section
        if (selectedRole == "Farmer") {
            Spacer(modifier = Modifier.height(16.dp))

            // Taluk Selection Dropdown (Farmers only)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedTaluk,
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedDistrict.isNotEmpty(),
                    label = { Text(if (selectedDistrict.isEmpty()) "Select District First *" else "Select Taluk *") },
                    leadingIcon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        IconButton(onClick = { if (selectedDistrict.isNotEmpty()) talukExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (selectedDistrict.isNotEmpty()) talukExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = talukExpanded,
                    onDismissRequest = { talukExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    KarnatakaDataHelper.getTaluksForDistrict(selectedDistrict).forEach { taluk ->
                        DropdownMenuItem(
                            text = { Text(taluk) },
                            onClick = {
                                selectedTaluk = taluk
                                talukExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Farm Name (Optional)") },
                leadingIcon = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        } else {
            // Buyer Section: City and Shop/Business Name
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City / Town Name *") },
                leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business / Shop Name (Optional)") },
                leadingIcon = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Validation Error Text
        if (validationError.isNotEmpty()) {
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Action Trigger
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            PrimaryButton(
                text = "Register",
                onClick = {
                    val error = validateInputs(
                        role = selectedRole,
                        fullName = fullName,
                        mobileNumber = mobileNumber,
                        email = email,
                        password = password,
                        district = selectedDistrict,
                        taluk = selectedTaluk,
                        city = city
                    )
                    if (error != null) {
                        validationError = error
                    } else {
                        validationError = ""
                        authViewModel.signup(
                            fullName = fullName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            email = email.trim(),
                            password = password,
                            district = selectedDistrict,
                            taluk = selectedTaluk,
                            city = city.trim(),
                            businessName = businessName.trim(),
                            profilePhotoBytes = compressedBytes
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle to Login Page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
            )
        }
    }
}

// Client Side Form Validation Function
private fun validateInputs(
    role: String,
    fullName: String,
    mobileNumber: String,
    email: String,
    password: String,
    district: String,
    taluk: String,
    city: String
): String? {
    if (fullName.isBlank()) return "Full name is required"
    if (mobileNumber.isBlank() || mobileNumber.length != 10) return "Please enter a valid 10-digit mobile number"
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email address"
    if (password.length < 6) return "Password must contain at least 6 characters"
    if (district.isBlank()) return "Please select your district"
    if (role == "Farmer" && taluk.isBlank()) return "Please select your taluk"
    if (role == "Buyer" && city.isBlank()) return "City/Town name is required"
    return null
}
