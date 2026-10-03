package com.krishiai.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userName = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "Krishi Farmer"
    val userRole = currentUser?.role ?: "Farmer"
    val userDistrict = currentUser?.district ?: ""
    val userPhone = currentUser?.mobileNumber ?: ""
    val userEmail = currentUser?.email ?: ""

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(com.krishiai.app.ui.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Section with Wavy Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Wavy Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(BrandGreen)
                        .drawWithContent {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width, size.height * 0.7f)
                                quadraticTo(
                                    size.width * 0.5f, size.height,
                                    0f, size.height * 0.7f
                                )
                                close()
                            }
                            clipPath(path) {
                                this@drawWithContent.drawContent()
                            }
                        }
                )

                // Profile Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape)
                    ) {
                        // Avatar Placeholder
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                        // Camera Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BrandGreen)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified $userRole${if (userDistrict.isNotEmpty()) " • $userDistrict" else ""}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val shareContext = androidx.compose.ui.platform.LocalContext.current
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.EditProfile.route) },
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_edit), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out my farm on KrishiAI! I am a verified $userRole in $userDistrict. Download KrishiAI now!")
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Profile")
                                shareContext.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_share), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            val farmerViewModel: com.krishiai.app.ui.screens.farmer.FarmerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val myProducts by farmerViewModel.myProducts.collectAsState()
            val revenueStats by farmerViewModel.revenueStats.collectAsState()
            
            val totalCrops = myProducts.size.toString()
            val totalRevenue = if (revenueStats.totalRevenue >= 100000) {
                "₹%.1fL".format(revenueStats.totalRevenue / 100000)
            } else if (revenueStats.totalRevenue > 0) {
                "₹%,d".format(revenueStats.totalRevenue.toInt())
            } else {
                "₹0"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Total Crops", totalCrops)
                HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                StatItem("Revenue", totalRevenue)
                HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                StatItemWithIcon("Deals", revenueStats.totalDeals.toString(), Icons.Rounded.Handshake, BrandGreen)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Settings List
            val context = androidx.compose.ui.platform.LocalContext.current
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingsListItem(icon = Icons.Rounded.PersonOutline, title = androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_personal_info), onClick = { navController.navigate(Screen.PersonalInfo.route) })
                SettingsListItem(icon = Icons.Rounded.Eco, title = androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_farm_details), onClick = { navController.navigate(Screen.MyFarmDetails.route) })
                SettingsListItem(icon = Icons.Rounded.Translate, title = androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_language), onClick = { navController.navigate(Screen.LanguageSettings.route) })
                SettingsListItem(icon = Icons.Rounded.HeadsetMic, title = androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_support), onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:support@krishiai.com")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "KrishiAI Support Request")
                    }
                    context.startActivity(intent)
                })
                SettingsListItem(icon = Icons.AutoMirrored.Rounded.Logout, title = androidx.compose.ui.res.stringResource(com.krishiai.app.R.string.profile_logout), isDestructive = true, onClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
    }
}

@Composable
fun StatItemWithIcon(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
    }
}

@Composable
fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val contentColor = if (isDestructive) Color(0xFFE53935) else Color.DarkGray
    val bgColor = if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = contentColor), modifier = Modifier.weight(1f))
        
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        if (!isDestructive) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
