package com.suraksha.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About Suraksha",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SectionHeader(title = "What is Suraksha?")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Suraksha means 'Protection' in Hindi, and that's exactly what we deliver - your personal safety companion that's always watching over you.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "In emergency situations, every second counts. Suraksha uses cutting-edge artificial intelligence and advanced motion sensors to automatically detect dangerous situations like falls, accidents, or attacks. When danger is detected, the app instantly alerts your emergency contacts with your precise GPS location, ensuring help arrives as quickly as possible.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Our Mission")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "To make personal safety accessible to everyone through smart technology. We believe that no one should feel unsafe, whether walking alone at night, living independently as a senior, or working in potentially dangerous environments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Key Features")
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸ¤–",
                    title = "AI-Powered Fall Detection",
                    description = "Our advanced machine learning model has been trained on thousands of fall patterns to distinguish between real emergencies and everyday movements.",
                    details = listOf(
                        "Continuously monitors accelerometer and gyroscope data at 50Hz",
                        "Detects free-fall followed by impact patterns",
                        "Filters out phone drops and false alarms using ML classification",
                        "Works silently in the background without draining battery",
                        "Automatically sends SOS with your location when real fall detected",
                        "No user intervention needed - perfect if you're unconscious"
                    )
                )
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸ“±",
                    title = "Shake Detection",
                    description = "A quick and discreet way to trigger emergency alerts when you need help but can't speak or access your phone screen.",
                    details = listOf(
                        "Shake your phone vigorously 3 times back-and-forth",
                        "Must be deliberate shaking - normal walking won't trigger it",
                        "Works even with phone in pocket or bag",
                        "Complete the gesture within 1.5 seconds",
                        "Instant SOS sent to all emergency contacts",
                        "Useful in threatening situations where you can't call"
                    )
                )
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸŽ¤",
                    title = "Hotword Detection",
                    description = "Voice-activated emergency trigger that works even when your phone is locked or in your pocket.",
                    details = listOf(
                        "Set your own custom hotword (e.g., 'Help me', 'Emergency')",
                        "Always listening in background when enabled",
                        "Works with screen off - no need to unlock phone",
                        "Low power consumption - won't drain battery",
                        "Say your hotword clearly to trigger instant SOS",
                        "Perfect for situations where you can't reach your phone"
                    )
                )
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸ“",
                    title = "Real-Time Location Sharing",
                    description = "Every emergency alert includes your precise GPS coordinates so help can find you quickly.",
                    details = listOf(
                        "Uses GPS, Wi-Fi, and cell tower triangulation",
                        "Accurate location within 5-50 meters",
                        "Includes clickable Google Maps link in SMS",
                        "Shows address and coordinates",
                        "Works indoors and outdoors",
                        "Location updated in real-time if you move"
                    )
                )
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸ—ºï¸",
                    title = "Safe Haven Map",
                    description = "Interactive map showing nearby police stations, hospitals, and safe locations when you need immediate help.",
                    details = listOf(
                        "Find nearest police stations in emergency",
                        "Locate closest hospitals and clinics",
                        "Get directions to safe locations",
                        "Shows distance and estimated time",
                        "Updates based on your current location",
                        "One-tap navigation to any safe haven"
                    )
                )
            }

            item {
                DetailedFeatureCard(
                    icon = "ðŸ‘¥",
                    title = "Emergency Contacts",
                    description = "Add trusted family members, friends, or neighbors who will receive instant alerts when you trigger SOS.",
                    details = listOf(
                        "Add unlimited emergency contacts",
                        "Each contact receives SMS with your location",
                        "Messages include emergency type (fall, shake, voice)",
                        "Contacts can click map link to navigate to you",
                        "Edit or remove contacts anytime",
                        "Test feature to ensure SMS delivery works"
                    )
                )
            }

            item {
                SectionHeader(title = "Complete Setup Guide")
            }

            item {
                DetailedUsageStep(
                    step = "1",
                    title = "Initial Setup",
                    description = "First-time configuration to get started with Suraksha.",
                    instructions = listOf(
                        "Download and install the app from the store",
                        "Create your account with email and password",
                        "Grant all required permissions when prompted:",
                        "  â€¢ Location - for GPS tracking",
                        "  â€¢ SMS - for sending emergency messages",
                        "  â€¢ Microphone - for hotword detection",
                        "  â€¢ Physical activity - for fall detection",
                        "  â€¢ Notifications - for alerts",
                        "Complete your profile with name and basic info"
                    )
                )
            }

            item {
                DetailedUsageStep(
                    step = "2",
                    title = "Add Emergency Contacts",
                    description = "Set up your safety network of trusted people.",
                    instructions = listOf(
                        "Tap on 'Contacts' tab at the bottom",
                        "Click the '+' button to add new contact",
                        "Enter contact name and phone number",
                        "Add relationship (Family, Friend, Neighbor, etc.)",
                        "Repeat for all people you want to receive alerts",
                        "Recommended: Add at least 3-5 trusted contacts",
                        "Inform your contacts that they'll receive emergency alerts",
                        "Test by sending a test message to verify it works"
                    )
                )
            }

            item {
                DetailedUsageStep(
                    step = "3",
                    title = "Enable Detection Features",
                    description = "Turn on the safety features you want to use.",
                    instructions = listOf(
                        "Go to 'Settings' tab at the bottom",
                        "Scroll to 'TRIGGERS' section",
                        "Toggle ON the features you want:",
                        "  â€¢ AI Fall Detection - automatic fall monitoring",
                        "  â€¢ Shake Detection - manual trigger by shaking",
                        "  â€¢ Hotword Detection - voice-activated trigger",
                        "Each toggle starts the respective monitoring service",
                        "You can enable all three for maximum protection",
                        "Services run in background even when app is closed"
                    )
                )
            }

            item {
                DetailedUsageStep(
                    step = "4",
                    title = "Configure Your Hotword",
                    description = "Set up your personal voice trigger phrase.",
                    instructions = listOf(
                        "In Settings, find 'Hotword Detection' section",
                        "Think of a unique phrase (2-3 words work best)",
                        "Good examples: 'Help me now', 'Emergency alert', 'Call help'",
                        "Avoid common phrases you say often",
                        "Say your chosen hotword clearly 3-5 times to train",
                        "Test by saying the hotword and checking if SOS triggers",
                        "Speak clearly and at normal volume",
                        "Works even with phone in pocket or screen off"
                    )
                )
            }

            item {
                DetailedUsageStep(
                    step = "5",
                    title = "Test Your Setup",
                    description = "Verify everything works before relying on it in emergency.",
                    instructions = listOf(
                        "IMPORTANT: Always test in a safe environment",
                        "Test Shake Detection:",
                        "  â€¢ Hold phone firmly",
                        "  â€¢ Shake vigorously 3 times back-and-forth",
                        "  â€¢ Should trigger within 1.5 seconds",
                        "Test Hotword Detection:",
                        "  â€¢ Say your hotword clearly",
                        "  â€¢ Check if app responds instantly",
                        "Test Fall Detection:",
                        "  â€¢ Drop phone onto soft surface from ~1 meter",
                        "  â€¢ Verify if detection occurs",
                        "Check SMS delivery:",
                        "  â€¢ Ask your emergency contacts if they received messages",
                        "  â€¢ Verify location link works in the SMS",
                        "Make adjustments if needed and retest"
                    )
                )
            }

            item {
                DetailedUsageStep(
                    step = "6",
                    title = "Daily Usage",
                    description = "How to use Suraksha in your everyday life.",
                    instructions = listOf(
                        "Keep app running in background at all times",
                        "Don't force-close the app from recent apps",
                        "Ensure phone has sufficient battery",
                        "Keep location services enabled always",
                        "Check settings occasionally to ensure features are on",
                        "Update emergency contacts if phone numbers change",
                        "The app works silently - you won't notice it",
                        "Battery usage is optimized (typically <5% per day)",
                        "For best results: Enable all three detection methods"
                    )
                )
            }

            item {
                SectionHeader(title = "How to Trigger SOS")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TriggerMethod(
                            emoji = "ðŸ†˜",
                            method = "SOS Button",
                            description = "Tap the large red SOS button on home screen. Instant and obvious method when you have time to access your phone."
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        TriggerMethod(
                            emoji = "ðŸ“±",
                            method = "Shake Detection",
                            description = "Shake your phone vigorously 3 times back-and-forth within 1.5 seconds. Discreet method that works in threatening situations."
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        TriggerMethod(
                            emoji = "ðŸŽ¤",
                            method = "Voice Activation",
                            description = "Say your configured hotword clearly. Works with screen off. Perfect when phone is out of reach."
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        TriggerMethod(
                            emoji = "ðŸ¤–",
                            method = "Automatic Fall Detection",
                            description = "No action needed! AI automatically detects real falls and sends SOS. Life-saving if you're unconscious."
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Important Safety Information")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = UrgentRed.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Critical Requirements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UrgentRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        SafetyNote("âœ“ All permissions must be granted for features to work")
                        SafetyNote("âœ“ Keep location services enabled 24/7 for accurate alerts")
                        SafetyNote("âœ“ Ensure emergency contacts have working phone numbers")
                        SafetyNote("âœ“ Keep your phone charged above 20% when possible")
                        SafetyNote("âœ“ Test the system regularly (monthly recommended)")
                        SafetyNote("âœ“ Update emergency contacts if numbers change")
                        SafetyNote("âœ“ Don't rely solely on app - call 911 in emergencies")
                        SafetyNote("âœ“ Inform contacts they may receive emergency alerts")
                        SafetyNote("âœ“ App requires internet for map features")
                        SafetyNote("âœ“ SMS sending works even without internet")
                    }
                }
            }

            item {
                SectionHeader(title = "Technical Specifications")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TechSpec("Sensor Sampling Rate", "50Hz (20ms intervals)")
                        TechSpec("Fall Detection Accuracy", "~95% with ML model")
                        TechSpec("Location Accuracy", "5-50 meters (GPS dependent)")
                        TechSpec("Response Time", "< 2 seconds from trigger to SMS")
                        TechSpec("Battery Usage", "< 5% per day (optimized)")
                        TechSpec("Android Version", "8.0 (Oreo) and above")
                        TechSpec("Internet Required", "Only for map features")
                        TechSpec("Data Storage", "Local - your data stays on device")
                    }
                }
            }

            item {
                SectionHeader(title = "Support & Feedback")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentBlue.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "We're here to help! Your safety is our priority.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "â€¢ Experiencing issues? Check Settings â†’ Sensor Logger to view sensor data and debug detection problems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "â€¢ Have suggestions? We continuously improve based on user feedback to make Suraksha better for everyone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "â€¢ Found a bug? Please report it so we can fix it quickly and improve safety for all users.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentBlue
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Stay Safe, Stay Protected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Suraksha is always watching over you. Remember: your safety matters, and help is just a shake, voice command, or automatic detection away.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = AccentBlue,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun DetailedFeatureCard(
    icon: String,
    title: String,
    description: String,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            details.forEach { detail ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "â€¢ ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentBlue
                    )
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedUsageStep(
    step: String,
    title: String,
    description: String,
    instructions: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier.size(40.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = step,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            instructions.forEach { instruction ->
                Text(
                    text = if (instruction.startsWith("  ")) instruction else "â€¢ $instruction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TriggerMethod(emoji: String, method: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = method,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SafetyNote(note: String) {
    Text(
        text = note,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 3.dp)
    )
}

@Composable
fun TechSpec(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

