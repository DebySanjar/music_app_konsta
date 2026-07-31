package com.example.muzik.myapplication.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.ui.theme.AppColors
import com.example.muzik.myapplication.viewmodel.MusicViewModel

@Composable
fun ShowScreen(
    viewModel: MusicViewModel,
    songIndex: Int,
    onBack: () -> Unit
) {
    val state by viewModel.playerState.collectAsState()

    // Navigate oldidan play boshlangan — faqat index farq qilsa qayta boshlaydi
    LaunchedEffect(songIndex) {
        if (viewModel.playerState.value.currentIndex != songIndex) {
            viewModel.playSongAt(songIndex)
        }
    }

    val song = viewModel.songs.getOrNull(state.currentIndex)

    val lottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lotti))
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottie,
        isPlaying = state.isPlaying,
        iterations = LottieConstants.IterateForever
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background: img_6 ──────────────────────────────────────
        Image(
            painter = painterResource(R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x1E000000),
                            Color(0x4B000000),
                            Color(0xB9000000)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Top bar ────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.ic_prev), "Back",
                        tint = AppColors.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Now Playing",
                    color = AppColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        painterResource(R.drawable.ic_shuffle), "Shuffle",
                        tint = if (state.isShuffle) AppColors.Purple else AppColors.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { viewModel.toggleLoop() }) {
                    Icon(
                        painterResource(R.drawable.ic_loop), "Loop",
                        tint = if (state.isLooping) AppColors.Purple else AppColors.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Album art ──────────────────────────────────────────
            Card(
                modifier = Modifier.size(260.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Image(
                    painterResource(R.drawable.img_1), "Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Song info ──────────────────────────────────────────
            Text(
                song?.songName ?: "",
                color = AppColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                song?.artistName ?: "",
                color = AppColors.TextSecondary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ── Lottie waveform ────────────────────────────────────
            LottieAnimation(
                lottie, { lottieProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Liquid Glass Progress Bar ──────────────────────────
            LiquidProgressBar(
                progress = state.progress,
                onSeek = { viewModel.seekTo((it * state.duration).toInt()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    msToTime(state.currentPosition),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    msToTime(state.duration),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                // Heart / Favorite icon
                val isFav = viewModel.favorites.collectAsState().value.contains(state.currentIndex)
                IconButton(
                    onClick = { viewModel.toggleFavorite(state.currentIndex) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart
                        ),
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFFF6B8A) else AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Liquid Glass Controls Panel ────────────────────────
            LiquidGlassPanel(
                isPlaying = state.isPlaying,
                onPrev  = { viewModel.playPreviousSong() },
                onPlay  = { viewModel.togglePlayPause() },
                onNext  = { viewModel.playNextSong() }
            )
        }
    }
}

// ── Liquid Glass Progress Bar ──────────────────────────────────────────

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LiquidProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackHeight = 6.dp
    val thumbRadius = 10.dp

    var dragProgress by remember { mutableFloatStateOf(progress) }

    // Sync external progress when not dragging
    LaunchedEffect(progress) { dragProgress = progress }

    BoxWithConstraints(
        modifier = modifier
            .height(28.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragProgress = (dragProgress + dragAmount / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    }
                )
            }
    ) {
        val totalWidth = maxWidth

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY    = size.height / 2f
            val trackW    = size.width
            val trackH    = trackHeight.toPx()
            val thumbR    = thumbRadius.toPx()
            val filledW   = trackW * dragProgress

            // Glass track background
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))
                ),
                topLeft     = Offset(0f, trackY - trackH / 2),
                size        = Size(trackW, trackH),
                cornerRadius = CornerRadius(trackH / 2)
            )

            // Glass track border
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0x66FFFFFF), Color(0x22FFFFFF))
                ),
                topLeft      = Offset(0f, trackY - trackH / 2),
                size         = Size(trackW, trackH),
                cornerRadius = CornerRadius(trackH / 2),
                style        = Stroke(width = 1.dp.toPx())
            )

            // Filled portion — purple gradient
            if (filledW > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
                        startX = 0f,
                        endX   = filledW
                    ),
                    topLeft      = Offset(0f, trackY - trackH / 2),
                    size         = Size(filledW, trackH),
                    cornerRadius = CornerRadius(trackH / 2)
                )

                // Shimmer highlight on filled
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0x55FFFFFF), Color(0x00FFFFFF)),
                        startY = trackY - trackH / 2,
                        endY   = trackY
                    ),
                    topLeft      = Offset(0f, trackY - trackH / 2),
                    size         = Size(filledW, trackH / 2),
                    cornerRadius = CornerRadius(trackH / 2)
                )
            }

            // Thumb outer glow
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color(0x448B5CF6), Color(0x00000000)),
                    radius = thumbR * 2
                ),
                radius = thumbR * 2,
                center = Offset(filledW.coerceIn(thumbR, trackW - thumbR), trackY)
            )

            // Thumb glass circle
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xCCA78BFA))
                ),
                radius = thumbR,
                center = Offset(filledW.coerceIn(thumbR, trackW - thumbR), trackY)
            )

            // Thumb inner shimmer
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x88FFFFFF), Color(0x00FFFFFF))
                ),
                radius = thumbR * 0.5f,
                center = Offset(
                    filledW.coerceIn(thumbR, trackW - thumbR) - thumbR * 0.2f,
                    trackY - thumbR * 0.3f
                )
            )
        }
    }
}

// ── Liquid Glass Controls Panel ────────────────────────────────────────

@Composable
private fun LiquidGlassPanel(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(40.dp))
            // Frosted glass layers
            .drawBehind {
                // Base glass fill
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0x44FFFFFF), Color(0x18FFFFFF))
                    ),
                    cornerRadius = CornerRadius(40.dp.toPx())
                )
                // Top highlight border
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0x88FFFFFF), Color(0x11FFFFFF))
                    ),
                    cornerRadius = CornerRadius(40.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Previous
            IconButton(onClick = onPrev, modifier = Modifier.size(44.dp)) {
                Icon(
                    painterResource(R.drawable.ic_prev), "Prev",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Play/Pause — liquid glass circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        // Outer glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0x558B5CF6), Color(0x00000000))
                            )
                        )
                        // Glass fill
                        drawCircle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95))
                            )
                        )
                        // Shimmer top
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0x55FFFFFF), Color(0x00FFFFFF)),
                                center = Offset(size.width * 0.35f, size.height * 0.2f),
                                radius = size.width * 0.4f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onPlay, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Next
            IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
                Icon(
                    painterResource(R.drawable.ic_skip), "Next",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

private fun msToTime(ms: Int): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
