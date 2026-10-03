package com.krishiai.app.ui.screens.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import com.krishiai.app.R
import com.krishiai.app.domain.ai.ChatMessage
import com.krishiai.app.domain.ai.VoiceAssistantState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(
    navController: NavController,
    viewModel: VoiceAssistantViewModel
) {
    val aiState by viewModel.aiState.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_voice_assistant), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.ai_back))
                    }
                },
                actions = {
                    LanguageSelector(currentLanguage) { viewModel.setLanguage(it) }
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.ai_clear_chat))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FBE7)
                )
            )
        },
        containerColor = Color(0xFFF9FBE7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Chat Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                if (aiState == VoiceAssistantState.LISTENING || aiState == VoiceAssistantState.PROCESSING) {
                    item {
                        TypingIndicator(state = aiState, partialText = partialText)
                    }
                }
                items(messages.reversed()) { message ->
                    ChatBubble(message = message) { text ->
                        clipboardManager.setText(AnnotatedString(text))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SuggestedQuestions { viewModel.sendQuery(it) }
                }
            }

            // Controls & Mic Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White)
                        )
                    )
                    .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (messages.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        ActionChip(Icons.Rounded.Replay, stringResource(R.string.ai_repeat_answer)) {
                            viewModel.repeatLastAnswer()
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.ai_type_question)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color.LightGray
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (textInput.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .clickable {
                                    viewModel.sendQuery(textInput)
                                    textInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = stringResource(R.string.ai_send), tint = Color.White)
                        }
                    } else {
                        // Smaller Microphone Button when text is empty
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (aiState == VoiceAssistantState.LISTENING) Color(0xFFE91E63) else Color(0xFF4CAF50))
                                .clickable { 
                                    if (recordAudioPermission.status.isGranted) {
                                        viewModel.toggleListening() 
                                    } else {
                                        recordAudioPermission.launchPermissionRequest()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (aiState == VoiceAssistantState.LISTENING) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = stringResource(R.string.ai_microphone),
                                tint = Color.White
                            )
                        }
                    }
                }
                
                if (aiState != VoiceAssistantState.IDLE && aiState != VoiceAssistantState.LISTENING) {
                    Text(
                        text = when (aiState) {
                            VoiceAssistantState.PROCESSING -> stringResource(R.string.ai_thinking)
                            VoiceAssistantState.SPEAKING -> stringResource(R.string.ai_speaking)
                            VoiceAssistantState.ERROR -> stringResource(R.string.ai_try_again)
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(currentLang: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8F5E9)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable { onSelect("en") }
                .background(if (currentLang == "en") Color(0xFF2E7D32) else Color.Transparent, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("EN", color = if (currentLang == "en") Color.White else Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .clickable { onSelect("kn") }
                .background(if (currentLang == "kn") Color(0xFF2E7D32) else Color.Transparent, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("KN", color = if (currentLang == "kn") Color.White else Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun SuggestedQuestions(onQuestionClick: (String) -> Unit) {
    val questions = stringArrayResource(R.array.ai_suggested_questions).toList()
    Column {
        Text(stringResource(R.string.ai_try_asking), style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp, start = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(questions) { q ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { onQuestionClick(q) }
                ) {
                    Text(q, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1B5E20)))
                }
            }
        }
    }
}

@Composable
fun AnimatedMicrophoneOrb(state: VoiceAssistantState, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == VoiceAssistantState.LISTENING || state == VoiceAssistantState.SPEAKING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color = when (state) {
        VoiceAssistantState.IDLE -> Color(0xFF4CAF50)
        VoiceAssistantState.LISTENING -> Color(0xFFE91E63)
        VoiceAssistantState.PROCESSING -> Color(0xFF2196F3)
        VoiceAssistantState.SPEAKING -> Color(0xFF9C27B0)
        VoiceAssistantState.ERROR -> Color.Red
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        if (state == VoiceAssistantState.LISTENING || state == VoiceAssistantState.SPEAKING) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .scale(scale * 1.1f)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.4f))
            )
        }
        
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color.copy(alpha = 0.8f), color)))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state == VoiceAssistantState.LISTENING || state == VoiceAssistantState.SPEAKING) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = stringResource(R.string.ai_microphone),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onCopy: (String) -> Unit) {
    val align = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isFromUser) Color(0xFF1B5E20) else Color.White
    val textColor = if (message.isFromUser) Color.White else Color(0xFF333333)
    val shape = if (message.isFromUser) {
        RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
    } else {
        RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp)
    }
    
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(message.timestamp))

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        contentAlignment = align
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!message.isFromUser) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF81C784)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.SmartToy, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start) {
                Surface(
                    shape = shape,
                    color = bgColor,
                    shadowElevation = if (message.isFromUser) 0.dp else 2.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = message.text, style = MaterialTheme.typography.bodyLarge, color = textColor)
                        if (!message.isFromUser) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.clickable { onCopy(message.text) }, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ContentCopy, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.ai_copy), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
                Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun TypingIndicator(state: VoiceAssistantState, partialText: String) {
    if (state == VoiceAssistantState.LISTENING) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp),
                color = Color(0xFF1B5E20).copy(alpha = 0.5f)
            ) {
                Text(
                    text = partialText.ifBlank { stringResource(R.string.ai_listening) },
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }
        }
    } else if (state == VoiceAssistantState.PROCESSING) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Start) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF81C784)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SmartToy, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Text(text = stringResource(R.string.ai_processing), modifier = Modifier.padding(16.dp), color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun ActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE8F5E9),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Icon(icon, null, tint = Color(0xFF1B5E20), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold))
        }
    }
}
