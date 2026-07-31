package com.example.muzik.myapplication.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzik.myapplication.BuildConfig
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val imageAlpha   = remember { Animatable(0f) }
    val imageScale   = remember { Animatable(0.75f) }
    val titleAlpha   = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(40f) }
    val versionAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Logo fade-in + scale-up
        launch { imageAlpha.animateTo(1f,  tween(700, easing = FastOutSlowInEasing)) }
        launch { imageScale.animateTo(1f,  tween(700, easing = FastOutSlowInEasing)) }

        // 2. Title slides up
        delay(400)
        launch { titleAlpha.animateTo(1f,  tween(600)) }
        launch { titleOffsetY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }

        // 3. Version fades in
        delay(700)
        versionAlpha.animateTo(1f, tween(500))

        // 4. Navigate
        delay(600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.BgDeep,
                        AppColors.BgSurface,
                        AppColors.BgDeep
                    )
                )
            )
    ) {
        // ── Centre: logo + title ──────────────────────────────────
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Circular logo with gradient border
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(imageScale.value)
                    .alpha(imageAlpha.value)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                AppColors.Purple,
                                AppColors.PurpleLight,
                                AppColors.PurpleDark,
                                AppColors.Purple
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(2.5.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_1),
                    contentDescription = "Konsta logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "KONSTA" — slide-up + fade-in
            Text(
                text = "KONSTA",
                color = AppColors.TextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .graphicsLayer { translationY = titleOffsetY.value }
            )
        }

        // ── Bottom: version ───────────────────────────────────────
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .alpha(versionAlpha.value)
        )
    }
}
