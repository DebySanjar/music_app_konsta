package com.example.muzik.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import com.example.muzik.myapplication.BuildConfig
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.models.Music
import com.example.muzik.myapplication.ui.theme.AppColors
import com.example.muzik.myapplication.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

private val whitePdf = android.graphics.PorterDuffColorFilter(
    android.graphics.Color.WHITE,
    android.graphics.PorterDuff.Mode.SRC_ATOP
)

// Telegram link — ilova bo'lsa app, bo'lmasa web
private const val TELEGRAM_LINK = "https://t.me/konsta_music_player"
private const val TELEGRAM_APP  = "tg://resolve?domain=konsta_music_player"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: MusicViewModel,
    onSongClick: (Int) -> Unit,
    onMiniPlayerExpand: (Int) -> Unit = onSongClick
) {
    val context         = LocalContext.current
    val playerState     by viewModel.playerState.collectAsState()
    val searchQuery     by viewModel.searchQuery.collectAsState()
    val favorites       by viewModel.favorites.collectAsState()
    val listState       = rememberLazyListState()
    val hasActive       = playerState.duration > 0
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope           = rememberCoroutineScope()
    var showSheet      by remember { mutableStateOf(false) }
    var showArtistRail by remember { mutableStateOf(false) }
    var selectedTab     by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) { viewModel.setActiveTab(selectedTab) }

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lotti))
    val lottieWhite = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR_FILTER, whitePdf, "**")
    )

    val displayList = remember(searchQuery, selectedTab, favorites) {
        val q = searchQuery.trim().lowercase()
        val all = viewModel.songs.mapIndexed { i, m -> i to m }
            .filter { (_, m) ->
                q.isEmpty() ||
                m.songName.lowercase().contains(q) ||
                m.artistName.lowercase().contains(q)
            }
        if (selectedTab == 1) all.filter { (i, _) -> favorites.contains(i) } else all
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.img_4),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(AppColors.BgScrim))

        Column(modifier = Modifier.fillMaxSize()) {

            // ── App Bar ────────────────────────────────────────
            AppBar(
                onMenuClick = { showArtistRail = true },
                modifier = Modifier.fillMaxWidth()
            )

            // ── Search ─────────────────────────────────────────
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // ── Tabs ───────────────────────────────────────────
            GlassTabs(
                selectedTab = selectedTab,
                favCount = favorites.size,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )

            // ── Content ────────────────────────────────────────
            if (displayList.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (selectedTab == 1) "No favorites yet" else "No songs found",
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 6.dp, end = 6.dp, top = 4.dp,
                        bottom = if (hasActive) 90.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = displayList,
                        key = { _, pair -> pair.first },
                        contentType = { _, _ -> "song" }
                    ) { _, pair ->
                        val (idx, music) = pair
                        MusicListItem(
                            music = music,
                            index = idx,
                            isPlaying = playerState.isPlaying && playerState.currentIndex == idx,
                            isFavorite = favorites.contains(idx),
                            lottieComposition = lottieComposition,
                            lottieWhite = lottieWhite,
                            onClick = { onSongClick(idx) },
                            onFavoriteToggle = { viewModel.toggleFavorite(idx) }
                        )
                    }
                }
            }
        }

        // Top fade
        Box(
            Modifier.fillMaxWidth().height(60.dp).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(AppColors.BgScrim, Color.Transparent)))
        )

        // MiniPlayer
        if (hasActive) {
            MiniPlayer(
                playerState = playerState,
                songs = viewModel.songs,
                isFavorite = favorites.contains(playerState.currentIndex),
                onExpand = { onMiniPlayerExpand(playerState.currentIndex) },
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNextSong() },
                onFavoriteToggle = { viewModel.toggleFavorite(playerState.currentIndex) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Artist Rail — chap tomondan siljib chiqadi
        ArtistRailOverlay(
            visible = showArtistRail,
            onDismiss = { showArtistRail = false },
            onArtistClick = { artist -> viewModel.setSearchQuery(artist.name) },
            onCommunityClick = { showSheet = true }
        )
    }

    // ── Artist Rail Sheet ──────────────────────────────────────


    // ── Community Bottom Sheet ─────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.BgSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x44FFFFFF))
                )
            }
        ) {
            CommunitySheetContent(
                onTelegramClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                    // Telegram ilovasi bo'lsa uni, bo'lmasa brauzerni ochadi
                    val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_APP))
                    val webIntent      = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_LINK))
                    try {
                        context.startActivity(telegramIntent)
                    } catch (e: Exception) {
                        context.startActivity(webIntent)
                    }
                }
            )
        }
    }
}

// ── App Bar ────────────────────────────────────────────────────────────

@Composable
private fun AppBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 18.dp)
            .height(58.dp)
            .padding(horizontal = 8.dp),
    ) {
        // Hamburger — left
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart).size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Artists",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Title — center
        Text(
            text = "Konsta",
            color = AppColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        // Community icon olib tashlandi — rail ichiga ko'chirildi
    }
}

// ── Community Bottom Sheet Content ────────────────────────────────────

@Composable
private fun CommunitySheetContent(onTelegramClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(AppColors.Purple, AppColors.PurpleDark))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_community),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Konsta Community",
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Join our Telegram group to get the latest updates, new tracks, and connect with other Konsta fans.",
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            color = Color(0x66FFFFFF),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Disclaimer
        Text(
            text = "⚠ We do not claim ownership of any songs in this app. All rights belong to their respective artists.",
            color = Color(0xFFFF6B6B),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(24.dp))

        // Telegram button
        Button(
            onClick = onTelegramClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2AABEE)   // Telegram blue
            )
        ) {
            Text(
                text = "Join on Telegram",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onTelegramClick) {
            Text(
                text = "t.me/konsta_music_player",
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

// ── Search Bar ─────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0x44FFFFFF), Color(0x22FFFFFF))),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0x66FFFFFF), Color(0x33FFFFFF), Color(0x66FFFFFF))
                    ),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_music),
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box {
                if (query.isEmpty()) {
                    Text("Search song or artist...", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(AppColors.Purple),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Glass Tabs ─────────────────────────────────────────────────────────

@Composable
private fun GlassTabs(
    selectedTab: Int,
    favCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = listOf("All Songs", "Favorites")
    // Badge — yashil+ko'k gradient
    val badgeGradient = Brush.horizontalGradient(listOf(Color(0xFF00C6A7), Color(0xFF0078FF)))

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x18FFFFFF))),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0x44FFFFFF),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
    ) {
        Row(Modifier.fillMaxSize()) {
            labels.forEachIndexed { index, label ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected)
                                Brush.horizontalGradient(listOf(AppColors.Purple, AppColors.PurpleLight))
                            else
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else AppColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                        if (index == 1 && favCount > 0) {
                            Spacer(Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (selected)
                                            Modifier.background(Color(0x66FFFFFF))
                                        else
                                            Modifier.background(badgeGradient, CircleShape)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$favCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── List Item ──────────────────────────────────────────────────────────

@Composable
private fun MusicListItem(
    music: Music,
    index: Int,
    isPlaying: Boolean,
    isFavorite: Boolean,
    lottieComposition: LottieComposition?,
    lottieWhite: LottieDynamicProperties,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.02f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        isPlaying = isPlaying,
        iterations = LottieConstants.IterateForever
    )

    val borderBrush = remember {
        Brush.horizontalGradient(listOf(Color(0xAA8B5CF6), Color(0x66A78BFA), Color(0xAA8B5CF6)))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(70.dp).scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0x99131326),
                        cornerRadius = CornerRadius(14.dp.toPx())
                    )
                    if (isPlaying) {
                        drawRoundRect(
                            brush = borderBrush,
                            cornerRadius = CornerRadius(14.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${index + 1}.",
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(30.dp)
            )

            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))) {
                Image(
                    painterResource(R.drawable.img_2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    music.songName,
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    music.artistName,
                    color = AppColors.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isPlaying) {
                LottieAnimation(
                    lottieComposition, { lottieProgress },
                    dynamicProperties = lottieWhite,
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart
                    ),
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFFF6B8A) else AppColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
