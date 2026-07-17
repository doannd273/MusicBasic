package com.example.musicbasic.ui.list

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicbasic.R
import com.example.musicbasic.designsystem.HomeTopBar
import com.example.musicbasic.model.Music
import com.example.musicbasic.ui.theme.MusicBasicTheme
import com.example.musicbasic.ui.theme.PlayerShadowPurple

private val ListScreenPreviewState =
    ListState(
        musicList =
            listOf(
                Music(
                    id = 1,
                    title = "Space Makes Me Sad",
                    author = "Fiji Blue",
                    musicResId = R.raw.mot_ngay_nao_do,
                    thumbnail = R.drawable.ic_background,
                    isLike = true,
                ),
                Music(
                    id = 2,
                    title = "Anymore",
                    author = "Dylan Rockoff",
                    musicResId = R.raw.tam_su,
                    thumbnail = R.drawable.ic_background_2,
                ),
                Music(
                    id = 3,
                    title = "Halo",
                    author = "fishkid",
                    musicResId = R.raw.vuon_may_vua_hay,
                    thumbnail = R.drawable.ic_background_3,
                ),
            ),
    )

@Composable
fun ListRoute(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
    onMusicClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    ListScreen(
        modifier = modifier,
        state = state,
        onBackClick = onBackClick,
        onMusicClick = onMusicClick,
        onPlayClick = onPlayClick,
        onDownloadClick = { musicId ->
            viewModel.onEvent(ListEvent.DownloadMusic(id = musicId))
        },
        onShareClick = { musicId ->
            viewModel.onEvent(ListEvent.ShareMusic(id = musicId))
        },
        onLikeClick = { musicId ->
            viewModel.onEvent(ListEvent.ToggleLikeMusic(id = musicId))
        },
    )

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is ListEffect.Success -> {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    state: ListState,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onMusicClick: (musicId: Int) -> Unit,
    onDownloadClick: (musicId: Int) -> Unit,
    onShareClick: (musicId: Int) -> Unit,
    onLikeClick: (musicId: Int) -> Unit,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding(),
        containerColor = PlayerShadowPurple,
        topBar = {
            HomeTopBar(
                modifier = modifier,
                topBarLabel = R.string.list_title,
                topBarIconLeft = R.drawable.ic_back,
                onIconLeftClick = onBackClick,
                topBarIconRight = R.drawable.ic_play,
                onIconRightClick = onPlayClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .background(color = PlayerShadowPurple)
                    .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = state.musicList,
                    key = { it.id },
                ) { item ->
                    MusicItem(
                        music = item,
                        isLiked = item.isLike,
                        onMusicClick = {
                            onMusicClick(item.id)
                        },
                        onDownloadClick = {
                            onDownloadClick(item.id)
                        },
                        onShareClick = {
                            onShareClick(item.id)
                        },
                        onLikeClick = {
                            onLikeClick(item.id)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicItem(
    music: Music,
    isLiked: Boolean,
    onMusicClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onLikeClick: () -> Unit,
) {
    var showOptionBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onMusicClick()
                },
        horizontalArrangement = Arrangement.Start,
    ) {
        Image(
            painter = painterResource(id = music.thumbnail),
            contentDescription = "",
            modifier =
                Modifier
                    .clip(
                        RoundedCornerShape(5.dp),
                    ).size(80.dp)
                    .aspectRatio(1f),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = music.title,
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp
                ),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = music.author,
                modifier = Modifier,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
            )
        }

        IconButton(
            modifier = Modifier.size(48.dp),
            onClick = onLikeClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_like),
                contentDescription =
                    stringResource(
                        if (isLiked) {
                            R.string.content_description_unlike
                        } else {
                            R.string.content_description_like
                        },
                    ),
                modifier = Modifier.size(24.dp),
                tint =
                    if (isLiked) {
                        Color(0xFFFF4D7D)
                    } else {
                        Color.White
                    },
            )
        }

        IconButton(
            modifier = Modifier.size(48.dp),
            onClick = {
                showOptionBottomSheet = true
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_horizontal),
                contentDescription = "",
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }

    if (showOptionBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            onDismissRequest = {
                showOptionBottomSheet = false
            },
            sheetState = sheetState,
            containerColor = Color.DarkGray,
        ) {
            OptionBottomSheet(
                onDownloadClick = onDownloadClick,
                onShareClick = onShareClick,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListScreenPreview() {
    MusicBasicTheme(dynamicColor = false) {
        ListScreen(
            state = ListScreenPreviewState,
            onBackClick = {},
            onMusicClick = {},
            onPlayClick = {},
            onDownloadClick = {},
            onShareClick = {},
            onLikeClick = {},
        )
    }
}
