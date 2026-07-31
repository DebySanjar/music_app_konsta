package com.example.muzik.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.models.Music
import com.example.muzik.myapplication.ui.theme.AppColors
import com.example.muzik.myapplication.viewmodel.PlayerState

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    songs: List<Music>,
    isFavorite: Boolean,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = songs.getOrNull(playerState.currentIndex) ?: return

    val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF0161628), Color(0xF5101020))
                    ),
                    cornerRadius = CornerRadius(18.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0x22FFFFFF), Color(0x00FFFFFF))
                    ),
                    size = Size(size.width, size.height * 0.5f),
                    cornerRadius = CornerRadius(18.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0x888B5CF6), Color(0x44A78BFA), Color(0x888B5CF6))
                    ),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            .clickable { onExpand() }
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(10.dp))

            // Song info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.songName,
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName,
                    color = AppColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Like
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart
                    ),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFF6B8A) else AppColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play / Pause
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = null,
                    tint = AppColors.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Next
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skip),
                    contentDescription = null,
                    tint = AppColors.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Progress bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        ) {
            drawRect(color = Color(0x33FFFFFF), size = Size(size.width, size.height))
            val filled = size.width * playerState.progress
            if (filled > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(AppColors.Purple, AppColors.PurpleLight),
                        startX = 0f, endX = filled
                    ),
                    size = Size(filled, size.height)
                )
            }
        }
    }
}
