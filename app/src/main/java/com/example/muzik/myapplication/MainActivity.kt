package com.example.muzik.myapplication

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.muzik.myapplication.ui.screens.ListScreen
import com.example.muzik.myapplication.ui.screens.ShowScreen
import com.example.muzik.myapplication.ui.screens.SplashScreen
import com.example.muzik.myapplication.ui.theme.KonstaTheme
import com.example.muzik.myapplication.viewmodel.MusicViewModel
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private val vm: MusicViewModel by viewModels()

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Notification permission — Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // In-App Update tekshiruvi
        checkForUpdate()

        setContent {
            KonstaTheme {
                KonstaApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Rate dialog
        if (vm.reviewManager.shouldShowReview()) {
            vm.reviewManager.launchReview(this)
        }
    }

    // ── In-App Update ─────────────────────────────────────────────────

    private fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                // Yangilanish mavjud — FLEXIBLE (fon yuklab, keyinroq taklif qiladi)
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    appUpdateManager.startUpdateFlow(
                        info,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    ).addOnFailureListener { e ->
                        Log.w("AppUpdate", "Flexible update failed: ${e.message}")
                    }
                }

                // Yangilanish allaqachon yuklab olingan — restart taklif qil
                info.installStatus() == com.google.android.play.core.install.model.InstallStatus.DOWNLOADED -> {
                    appUpdateManager.completeUpdate()
                }
            }
        }.addOnFailureListener { e ->
            // Play Store yo'q (test qurilma) — xatoni yutib yuboramiz
            Log.d("AppUpdate", "Update check skipped: ${e.message}")
        }
    }
}

@Composable
fun KonstaApp() {
    val navController  = rememberNavController()
    val musicViewModel: MusicViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {

        composable(
            route = "splash",
            exitTransition = { fadeOut(tween(300)) }
        ) {
            SplashScreen(onSplashFinished = {
                navController.navigate("list") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable(
            route = "list",
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = {
                fadeOut(tween(180)) + scaleOut(animationSpec = tween(180), targetScale = 0.96f)
            },
            popEnterTransition = {
                fadeIn(tween(220)) + scaleIn(animationSpec = tween(220), initialScale = 0.96f)
            }
        ) {
            ListScreen(
                viewModel = musicViewModel,
                onSongClick = { index ->
                    musicViewModel.playSongAt(index)
                    navController.navigate("show/$index")
                },
                onMiniPlayerExpand = { index ->
                    navController.navigate("show/$index")
                }
            )
        }

        composable(
            route = "show/{songIndex}",
            arguments = listOf(navArgument("songIndex") { type = NavType.IntType }),
            enterTransition = {
                fadeIn(tween(200)) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { (it * 0.08f).toInt() }
            },
            exitTransition = {
                fadeOut(tween(180)) + slideOutVertically(tween(200)) { (it * 0.06f).toInt() }
            }
        ) { back ->
            val index = back.arguments?.getInt("songIndex") ?: 0
            ShowScreen(
                viewModel = musicViewModel,
                songIndex = index,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
