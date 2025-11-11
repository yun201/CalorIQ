package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                NavigationApp()
            }
        }
    }
}

@Composable
fun NavigationApp() {
    val navController = rememberNavController()
    val defaultCredentials = remember { mutableStateOf(listOf("123" to "123")) }
    // Dummy data for the dashboard
    val scanHistory = remember {
        listOf(
            ScanHistoryItem(1, "Banana", 105, System.currentTimeMillis() - 1000 * 60 * 5),
            ScanHistoryItem(2, "Apple", 95, System.currentTimeMillis() - 1000 * 60 * 60 * 2),
            ScanHistoryItem(3, "Chicken Salad", 350, System.currentTimeMillis() - 1000 * 60 * 60 * 6),
            ScanHistoryItem(4, "Protein Shake", 180, System.currentTimeMillis() - 1000 * 60 * 60 * 24)
        )
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") },
                onNavigateToRegister = { navController.navigate("register") },
                defaultCredentials = defaultCredentials.value
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                history = scanHistory,
                onScanFoodClick = { navController.navigate("scan") },
                onProfileClick = { navController.navigate("profile") }
            )
        }
        composable("scan") {
            CameraScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onChangeInfoClick = { navController.navigate("change_info") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("change_info") {
            val currentUser = defaultCredentials.value.firstOrNull()
            ChangeInfoScreen(
                currentEmail = currentUser?.first ?: "",
                onNavigateBack = { navController.popBackStack() },
                onSave = { newEmail, newPassword ->
                    if (newEmail.isNotBlank() && newPassword.isNotBlank()) {
                        defaultCredentials.value = listOf(newEmail to newPassword)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasCamPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )
    var showReportDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var foodReport by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCamPermission) {
                CameraPreview(modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(onClick = {
                        isLoading = true
                        // TODO: 1. Capture image from CameraPreview
                        // TODO: 2. Send image to your API (e.g., using Retrofit/Ktor)
                        // TODO: 3. On response, update foodReport state and set isLoading to false
                        // For now, we'll simulate a delay and use mock data
                        scope.launch {
                            delay(2000)
                            foodReport = "Apple" to "230 kcal"
                            isLoading = false
                            showReportDialog = true
                        }
                    }) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Capture")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required to use this feature.")
                }
            }
        }
    }

    if (showReportDialog) {
        foodReport?.let {
            FoodReportDialog(
                onDismiss = { showReportDialog = false },
                ingredients = it.first,
                calories = it.second
            )
        }
    }
}

@Composable
fun FoodReportDialog(onDismiss: () -> Unit, ingredients: String, calories: String) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Food Report") },
        text = {
            Column {
                Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
                Text(ingredients)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Calories:", style = MaterialTheme.typography.titleMedium)
                Text(calories)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    defaultCredentials: List<Pair<String, String>>
) {
    // State management
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Calorie Scanner", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        // Email input
        TextField(
            value = email.value,
            onValueChange = {
                email.value = it
                errorMessage.value = "" // Clear error message
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input
        TextField(
            value = password.value,
            onValueChange = {
                password.value = it
                errorMessage.value = "" // Clear error message
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        if (errorMessage.value.isNotEmpty()) {
            Text(
                text = errorMessage.value,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login button
        Button(
            onClick = {
                if (defaultCredentials.any { it.first == email.value && it.second == password.value }) {
                    onLoginSuccess()
                } else {
                    errorMessage.value = "Invalid email or password"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Create account button
        TextButton(onClick = onNavigateToRegister) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test account credentials
        Text(
            text = "Test accounts:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        defaultCredentials.forEachIndexed { index, (email, pass) ->
            Text(
                text = "${index + 1}. $email / $pass",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit = {}
) {
    // State management
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val successMessage = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Email input
        OutlinedTextField(
            value = email.value,
            onValueChange = {
                email.value = it
                errorMessage.value = ""
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
                errorMessage.value = ""
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password input
        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = {
                confirmPassword.value = it
                errorMessage.value = ""
            },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (errorMessage.value.isNotEmpty()) {
            Text(
                text = errorMessage.value,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Success message
        if (successMessage.value) {
            Text(
                text = "Registration successful!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Register button
        Button(
            onClick = {
                if (email.value.isEmpty() || password.value.isEmpty() || confirmPassword.value.isEmpty()) {
                    errorMessage.value = "Please fill all fields"
                } else if (password.value != confirmPassword.value) {
                    errorMessage.value = "Passwords don't match"
                } else {
                    // Mock registration - doesn't actually save
                    successMessage.value = true
                    errorMessage.value = ""

                    // Clear form after submit
                    /* Uncomment this to clear form:
                    email.value = ""
                    password.value = ""
                    confirmPassword.value = ""
                    */
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to login button
        TextButton(onClick = onNavigateBack) {
            Text("Back to Login")
        }
    }
}

data class ScanHistoryItem(
    val id: Long,
    val foodName: String,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit, onLogout: () -> Unit, onChangeInfoClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onChangeInfoClick) {
                Text("Change Info")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Log Out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeInfoScreen(
    currentEmail: String,
    onNavigateBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val email = remember { mutableStateOf(currentEmail) }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Change Info") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            if (errorMessage.value.isNotEmpty()) {
                Text(
                    text = errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (password.value != confirmPassword.value) {
                        errorMessage.value = "Passwords don't match"
                    } else if (email.value.isBlank() || password.value.isBlank()) {
                        errorMessage.value = "Fields cannot be empty"
                    } else {
                        onSave(email.value, password.value)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    history: List<ScanHistoryItem>,
    onScanFoodClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CalroIQ") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onScanFoodClick) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Scan Food")
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Food")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val totalCalories = history.sumOf { it.calories }
            val lastScanCalories = history.firstOrNull()?.calories ?: 0

            SummaryOverview(
                totalCalories = totalCalories,
                lastScanCalories = lastScanCalories
            )
            RecentHistoryFeed(history = history)
        }
    }
}

@Composable
fun SummaryOverview(totalCalories: Int, lastScanCalories: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Daily Summary", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Intake", style = MaterialTheme.typography.titleMedium)
                    Text("$totalCalories kcal", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Last Scan", style = MaterialTheme.typography.titleMedium)
                    Text("$lastScanCalories kcal", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun RecentHistoryFeed(history: List<ScanHistoryItem>) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(
            "Recent History",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
        )
        LazyColumn {
            items(history) { item ->
                HistoryItemCard(item)
                Divider() // Add a divider between items
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: ScanHistoryItem) {
    val formattedTimestamp = remember(item.timestamp) {
        // Simple time format, e.g., 9:30 AM
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.foodName} - ${item.calories} kcal • $formattedTimestamp",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
