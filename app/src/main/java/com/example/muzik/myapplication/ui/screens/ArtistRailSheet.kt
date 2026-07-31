package com.example.muzik.myapplication.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.ui.theme.AppColors

// ── Data ───────────────────────────────────────────────────────────────
data class ArtistItem(
    @DrawableRes val imageRes: Int,
    val name: String
)

// ══ BU LISTNI O'ZINGIZ O'ZGARTIRING ═══════════════════════════════════
val artistItems = listOf(
    ArtistItem(R.drawable.img_1, "Konsta"),
    ArtistItem(R.drawable.axror, "Axror.A"),
    ArtistItem(R.drawable.aslwayne, "Asl Wayne")
)
// ══════════════════════════════════════════════════════════════════════

@Composable
fun ArtistRailOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onArtistClick: (ArtistItem) -> Unit,
    onCommunityClick: () -> Unit
) {
    // Scrim
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        )
    }

    // Rail panel — chapdan siljib chiqadi
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(220)) { -it },
        exit = slideOutHorizontally(tween(200)) { -it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(92.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF161628), Color(0xFD101020))),
                    shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                )
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Artists",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(12.dp))

                // Artist items — tight spacing
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    artistItems.forEach { artist ->
                        ArtistRailItem(
                            artist = artist,
                            onClick = {
                                onArtistClick(artist)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Community button — bottom of rail
                Column(
                    modifier = Modifier
                        .width(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onCommunityClick()
                            onDismiss()
                        }
                        .padding(vertical = 10.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_community),
                        contentDescription = "Community",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Community",
                        color = AppColors.TextSecondary,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ArtistRailItem(
    artist: ArtistItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle avatar — 40dp, kichikroq
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(AppColors.Purple, AppColors.PurpleDark))
                )
        ) {
            Image(
                painter = painterResource(id = artist.imageRes),
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = artist.name,
            color = AppColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
