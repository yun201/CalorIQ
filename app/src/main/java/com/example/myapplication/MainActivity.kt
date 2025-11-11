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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

data class Ingredient(val name: String, val calories: Int)
data class NutrientInfo(val name: String, val weight: Int, val percentage: Int)
data class FoodReport(val imageResId: Int, val ingredients: List<Ingredient>, val nutrients: List<NutrientInfo>) {
    val totalCalories: Int
        get() = ingredients.sumOf { it.calories }
}

val foodReports = mapOf(
    "chicken_broccoli" to FoodReport(
        imageResId = R.drawable.chicken_and_broccoli,
        ingredients = listOf(
            Ingredient("Chicken Breast", 330),
            Ingredient("Broccoli", 35)
        ),
        nutrients = listOf(
            NutrientInfo("Protein", 22, 44),
            NutrientInfo("Fats", 16, 32),
            NutrientInfo("Carbs", 12, 24)
        )
    ),
    "banana" to FoodReport(
        imageResId = R.drawable.banana, // Using placeholder
        ingredients = listOf(Ingredient("Banana", 105)),
        nutrients = listOf(
            NutrientInfo("Protein", 1, 4),
            NutrientInfo("Fats", 0, 0),
            NutrientInfo("Carbs", 27, 96)
        )
    ),
    "apple" to FoodReport(
        imageResId = R.drawable.apple, // Using placeholder
        ingredients = listOf(Ingredient("Apple", 95)),
        nutrients = listOf(
            NutrientInfo("Protein", 0, 0),
            NutrientInfo("Fats", 0, 0),
            NutrientInfo("Carbs", 25, 100)
        )
    ),
        "salad" to FoodReport(
        imageResId = R.drawable.chicken_salad, // Using placeholder
        ingredients = listOf(
            Ingredient("Chicken", 200),
            Ingredient("Lettuce", 15),
            Ingredient("Dressing", 135)
        ),
        nutrients = listOf(
            NutrientInfo("Protein", 18, 30),
            NutrientInfo("Fats", 25, 50),
            NutrientInfo("Carbs", 10, 20)
        )
    ),
    "shake" to FoodReport(
        imageResId = R.drawable.protein_shake, // Using placeholder
        ingredients = listOf(Ingredient("Protein Shake", 180)),
        nutrients = listOf(
            NutrientInfo("Protein", 30, 70),
            NutrientInfo("Fats", 2, 10),
            NutrientInfo("Carbs", 10, 20)
        )
    )
)

@Composable
fun NavigationApp() {
    val navController = rememberNavController()
    val defaultCredentials = remember { mutableStateOf(listOf("123" to "123")) }
    val scanHistory = remember {
        listOf(
            ScanHistoryItem(1, "Banana", 105, System.currentTimeMillis() - 1000 * 60 * 5, "banana"),
            ScanHistoryItem(2, "Apple", 95, System.currentTimeMillis() - 1000 * 60 * 60 * 2, "apple"),
            ScanHistoryItem(3, "Chicken Salad", 350, System.currentTimeMillis() - 1000 * 60 * 60 * 6, "salad"),
            ScanHistoryItem(4, "Protein Shake", 180, System.currentTimeMillis() - 1000 * 60 * 60 * 24, "shake")
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
            RegisterScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("dashboard") {
            DashboardScreen(
                history = scanHistory,
                onScanFoodClick = { navController.navigate("scan") },
                onProfileClick = { navController.navigate("profile") },
                onHistoryItemClick = { item ->
                    navController.navigate("food_report/${item.foodReportId}")
                }
            )
        }
        composable("scan") {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onCaptureSuccess = {
                    // Simulate capturing the main food item
                    navController.navigate("food_report/chicken_broccoli")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onChangeInfoClick = { navController.navigate("change_info") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
        composable("food_report/{reportId}") { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")
            val report = foodReports[reportId]
            if (report != null) {
                FoodReportScreen(report = report, onNavigateBack = { navController.popBackStack() })
            } else {
                // Handle error: report not found
                Text("Error: Food report not found.", modifier = Modifier.padding(16.dp))
            }
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
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
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
fun CameraScreen(onNavigateBack: () -> Unit, onCaptureSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasCamPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )
    var isLoading by remember { mutableStateOf(false) }

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
                        scope.launch {
                            delay(1000) // Simulate capture and API call
                            isLoading = false
                            onCaptureSuccess()
                        }
                    }) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Capture")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission is required to use this feature.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodReportScreen(report: FoodReport, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Food Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(color = Color(0xFFFDE4B7), shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Calories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${report.totalCalories} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.8f),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Image(
                    painter = painterResource(id = report.imageResId),
                    contentDescription = "Scanned food image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Ingredients
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Calories", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.End, modifier = Modifier.weight(0.5f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            report.ingredients.forEach {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(it.name, modifier = Modifier.weight(1f))
                    Text("${it.calories} kcal", textAlign = TextAlign.End, modifier = Modifier.weight(0.5f))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Nutrients
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Nutrients", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("weight/g", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                Text("Content", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.End, modifier = Modifier.weight(0.5f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            report.nutrients.forEach {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(it.name, modifier = Modifier.weight(1f))
                    Text("${it.weight}g", textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                    Text("${it.percentage}%", textAlign = TextAlign.End, modifier = Modifier.weight(0.5f))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    defaultCredentials: List<Pair<String, String>>
) {
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
        Text(text = "CalorIQ", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = email.value,
            onValueChange = { email.value = it; errorMessage.value = "" },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password.value,
            onValueChange = { password.value = it; errorMessage.value = "" },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage.value.isNotEmpty()) {
            Text(text = errorMessage.value, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }
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
        TextButton(onClick = onNavigateToRegister) { Text("Create Account") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Test accounts:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        defaultCredentials.forEachIndexed { index, (email, pass) ->
            Text("${index + 1}. $email / $pass", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit = {}
) {
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
        OutlinedTextField(value = email.value, onValueChange = { email.value = it; errorMessage.value = "" }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password.value, onValueChange = { password.value = it; errorMessage.value = "" }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = confirmPassword.value, onValueChange = { confirmPassword.value = it; errorMessage.value = "" }, label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(24.dp))
        if (errorMessage.value.isNotEmpty()) {
            Text(text = errorMessage.value, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (successMessage.value) {
            Text(text = "Registration successful!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        }
        Button(
            onClick = {
                if (email.value.isEmpty() || password.value.isEmpty() || confirmPassword.value.isEmpty()) {
                    errorMessage.value = "Please fill all fields"
                } else if (password.value != confirmPassword.value) {
                    errorMessage.value = "Passwords don't match"
                } else {
                    successMessage.value = true
                    errorMessage.value = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Register") }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateBack) { Text("Back to Login") }
    }
}

data class ScanHistoryItem(
    val id: Long,
    val foodName: String,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val foodReportId: String
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
            Button(onClick = onChangeInfoClick) { Text("Change Info") }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) { Text("Log Out") }
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
            OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = confirmPassword.value, onValueChange = { confirmPassword.value = it }, label = { Text("Confirm New Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
            if (errorMessage.value.isNotEmpty()) {
                Text(text = errorMessage.value, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
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
            ) { Text("Save") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    history: List<ScanHistoryItem>,
    onScanFoodClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryItemClick: (ScanHistoryItem) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CalorIQ") },
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

            SummaryOverview(totalCalories = totalCalories, lastScanCalories = lastScanCalories)
            RecentHistoryFeed(history = history, onItemClick = onHistoryItemClick)
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
fun RecentHistoryFeed(history: List<ScanHistoryItem>, onItemClick: (ScanHistoryItem) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text("Recent History", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp))
        LazyColumn {
            items(history) { item ->
                HistoryItemCard(item = item, onClick = { onItemClick(item) })
                Divider() // Add a divider between items
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: ScanHistoryItem, onClick: () -> Unit) {
    val formattedTimestamp = remember(item.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${item.foodName} - ${item.calories} kcal • $formattedTimestamp", style = MaterialTheme.typography.bodyLarge)
    }
}
