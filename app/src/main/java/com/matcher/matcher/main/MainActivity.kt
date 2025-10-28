package com.matcher.matcher.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.content.ContextCompat
import android.graphics.Color as AndroidColor
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matcher.matcher.modules.splash.SplashScreen
import com.matcher.matcher.modules.auth.AccountSelectScreen
import com.matcher.matcher.modules.auth.LoginScreen
import com.matcher.matcher.modules.auth.forgot.ForgotPasswordScreen
import com.matcher.matcher.modules.auth.register.RegisterScreen
import com.matcher.matcher.utils.AppTheme
import com.matcher.matcher.modules.terms.TermsScreen
import com.matcher.matcher.modules.main.MainScreen
import com.matcher.matcher.utils.PreferencesManager
import com.matcher.matcher.network.socket.SocketManager
import com.matcher.matcher.models.user.User
import com.matcher.matcher.models.message.socket.SocketMessageEvent
import com.matcher.matcher.models.message.socket.MessageStatusUpdate
import com.matcher.matcher.models.message.socket.UserStatus
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.matcher.matcher.R

class MainActivity : ComponentActivity() {
    
    private val socketManager = SocketManager.getInstance()
    private var isSocketConnected = false
    
    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.matcher.matcher.LOGOUT") {
                navController.navigate("account_select") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    private lateinit var navController: NavHostController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primaryDarkColor)
        window.navigationBarColor = AndroidColor.TRANSPARENT
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        val filter = IntentFilter("com.matcher.matcher.LOGOUT")
        registerReceiver(logoutReceiver, filter)
        
        setupSocketListeners()
        
        setContent {
            navController = rememberNavController()
            AppTheme {
                Surface(color = MaterialTheme.colorScheme.primary) {
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("account_select") {
                            AccountSelectScreen(
                                onLoginClick = { navController.navigate("login") },
                                onRegisterClick = { navController.navigate("register") },
                                onTermsClick = { navController.navigate("terms") },
                                onGoogleSignInResult = { task ->
                                    handleGoogleSignInResult(task)
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.navigateUp() },
                                onContinue = { },
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLogin = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onForgotClick = { navController.navigate("forgot") },
                                onBack = { navController.navigateUp() }
                            )
                        }
                        composable("forgot") {
                            ForgotPasswordScreen(
                                onBack = { navController.navigateUp() },
                                onFinished = { navController.popBackStack("login", inclusive = false) }
                            )
                        }
                        composable("main") {
                            LaunchedEffect(Unit) {
                                connectSocket()
                            }
                            MainScreen()
                        }
                        composable("terms") { TermsScreen(url = "https://example.com/terms", onBack = { navController.navigateUp() }) }
                    }
                }
            }
        }
    }
    
    private fun connectSocket() {
        if (isSocketConnected) {
            return
        }
        
        lifecycleScope.launch {
            val prefsManager = PreferencesManager(this@MainActivity)
            val token = prefsManager.getAuthToken()
            val userId = User.current?._id
            
            if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                
                socketManager.connect(token, userId)
            } else {
            }
        }
    }
    
    private fun setupSocketListeners() {
        lifecycleScope.launch {
            launch {
                socketManager.isConnected.collect { isConnected ->
                    isSocketConnected = isConnected
                    if (isConnected) {
                    } else {
                    }
                }
            }
            
            launch {
                socketManager.incomingMessages.collect { event: SocketMessageEvent? ->
                    event?.let { 
                    }
                }
            }
            
            launch {
                socketManager.messageStatusUpdates.collect { update: MessageStatusUpdate? ->
                    update?.let { upd: MessageStatusUpdate ->
                    }
                }
            }
            
            launch {
                socketManager.onlineStatusUpdates.collect { status: UserStatus? ->
                    status?.let { stat: UserStatus ->
                    }
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == 1001) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        socketManager.disconnect()
        
        try {
            unregisterReceiver(logoutReceiver)
        } catch (e: Exception) {
        }
    }
}