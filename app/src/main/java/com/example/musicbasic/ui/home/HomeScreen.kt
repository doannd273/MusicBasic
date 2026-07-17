package com.example.musicbasic.ui.home

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicbasic.R
import com.example.musicbasic.designsystem.HomeTopBar
import com.example.musicbasic.extensions.playPauseDescription
import com.example.musicbasic.extensions.playPauseIcon
import com.example.musicbasic.extensions.repeatIcon
import com.example.musicbasic.extensions.repeatTint
import com.example.musicbasic.extensions.timeText
import com.example.musicbasic.model.Music
import com.example.musicbasic.ui.navigation.NavigationResultKey
import com.example.musicbasic.ui.theme.MusicBasicTheme
import com.example.musicbasic.ui.theme.PlayerAccentPurple
import com.example.musicbasic.ui.theme.PlayerAccentPurpleLight
import com.example.musicbasic.ui.theme.PlayerPrimaryText
import com.example.musicbasic.ui.theme.PlayerProgressTrack
import com.example.musicbasic.ui.theme.PlayerSecondaryText
import com.example.musicbasic.ui.theme.PlayerShadowPurple
import kotlinx.coroutines.isActive

private val PlayerControlIconSize = 38.dp
private val PlayerPrimaryControlSize = 76.dp
private val HomeScreenPreviewMusic =
    Music(
        id = 1,
        title = "Ophelia",
        author = "Steven Price",
        thumbnail = R.drawable.ic_background,
        musicResId = R.raw.mot_ngay_nao_do,
    )
private val HomeScreenPreviewState =
    HomeState(
        currentMusic = HomeScreenPreviewMusic,
        currentMusicIndex = 0,
        isPlaying = true,
        progress = 85_000f / 195_000f,
        currentPositionMs = 85_000L,
        durationMs = 195_000L,
    )

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    savedStateHandle: SavedStateHandle,
    viewModel: HomeViewModel = hiltViewModel(),
    onMusicListClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainState by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedMusicId by savedStateHandle
        .getStateFlow(
            NavigationResultKey.SELECTED_MUSIC_ID,
            NavigationResultKey.NO_SELECTED_MUSIC_ID,
        ).collectAsStateWithLifecycle()

    val playShuffle by savedStateHandle
        .getStateFlow(
            NavigationResultKey.PLAY_SHUFFLE,
            false,
        ).collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        homeState = mainState,
        onMusicListClick = onMusicListClick,
        onShuffleClick = {
            viewModel.onEvent(HomeEvent.ShuffleClicked)
        },
        onPreviousClick = {
            viewModel.onEvent(HomeEvent.PreviousClicked)
        },
        onPlayPauseClick = {
            viewModel.onEvent(HomeEvent.PlayPauseClicked)
        },
        onNextClick = {
            viewModel.onEvent(HomeEvent.NextClicked)
        },
        onRepeatClick = {
            viewModel.onEvent(HomeEvent.RepeatClicked)
        },
        onSeekChange = { progress ->
            viewModel.onEvent(HomeEvent.SeekChanged(progress))
        },
    )

    LaunchedEffect(selectedMusicId, lifecycleOwner) {
        if (selectedMusicId != NavigationResultKey.NO_SELECTED_MUSIC_ID) {
            viewModel.onEvent(HomeEvent.MusicSelected(musicId = selectedMusicId))
            savedStateHandle[NavigationResultKey.SELECTED_MUSIC_ID] = NavigationResultKey.NO_SELECTED_MUSIC_ID
        }
    }

    LaunchedEffect(playShuffle, lifecycleOwner) {
        if (playShuffle) {
            viewModel.onEvent(HomeEvent.PlayShuffleClicked)
            savedStateHandle[NavigationResultKey.PLAY_SHUFFLE] = false
        }
    }

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is HomeEffect.MusicMessage -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeState: HomeState,
    onMusicListClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeekChange: (progress: Float) -> Unit,
) {
    Scaffold(
        containerColor = PlayerShadowPurple,
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        topBar = {
            HomeTopBar(
                modifier = modifier,
                topBarLabel = R.string.home_title,
                topBarIconLeft = R.drawable.ic_list,
                onIconLeftClick = onMusicListClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(PlayerShadowPurple)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RotateImage(
                musicResId = homeState.currentMusic?.thumbnail ?: R.drawable.ic_background,
                isPlaying = homeState.isPlaying,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = homeState.currentMusic?.title ?: "",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                color = PlayerPrimaryText,
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = homeState.currentMusic?.author ?: "",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = PlayerSecondaryText,
            )

            Spacer(modifier = Modifier.height(45.dp))

            PlaybackProgressSlider(
                progress = homeState.progress,
                durationMs = homeState.durationMs,
                onSeek = onSeekChange,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = homeState.currentPositionMs.timeText(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = PlayerPrimaryText,
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = homeState.durationMs.timeText(),
                        modifier = Modifier.align(Alignment.CenterEnd),
                        style = MaterialTheme.typography.titleMedium,
                        color = PlayerPrimaryText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = onShuffleClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuffle),
                        contentDescription = stringResource(R.string.content_description_shuffle),
                        modifier = Modifier.size(PlayerControlIconSize),
                        tint =
                            if (homeState.isShuffleEnabled) {
                                PlayerAccentPurpleLight
                            } else {
                                PlayerPrimaryText
                            },
                    )
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = onPreviousClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_previous),
                        contentDescription = stringResource(R.string.content_description_previous),
                        modifier = Modifier.size(PlayerControlIconSize),
                        tint = PlayerPrimaryText,
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        modifier =
                            Modifier
                                .size(PlayerPrimaryControlSize)
                                .clip(CircleShape)
                                .background(PlayerAccentPurple),
                        onClick = onPlayPauseClick,
                    ) {
                        Icon(
                            painter = painterResource(id = homeState.isPlaying.playPauseIcon()),
                            contentDescription = stringResource(homeState.isPlaying.playPauseDescription()),
                            modifier = Modifier.size(PlayerControlIconSize),
                            tint = PlayerPrimaryText,
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = onNextClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        contentDescription = stringResource(R.string.content_description_next),
                        modifier = Modifier.size(PlayerControlIconSize),
                        tint = PlayerPrimaryText,
                    )
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = onRepeatClick,
                ) {
                    Icon(
                        painter = painterResource(id = homeState.repeatMode.repeatIcon()),
                        contentDescription = stringResource(R.string.content_description_repeat),
                        modifier = Modifier.size(PlayerControlIconSize),
                        tint = homeState.repeatMode.repeatTint(),
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybackProgressSlider(
    progress: Float,
    durationMs: Long,
    onSeek: (progress: Float) -> Unit,
) {
    var sliderProgress by remember {
        mutableFloatStateOf(progress)
    }

    var isSeeking by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(progress, isSeeking) {
        if (!isSeeking) {
            sliderProgress = progress
        }
    }

    Slider(
        value = sliderProgress,
        onValueChange = { newProgress ->
            isSeeking = true
            sliderProgress = newProgress
        },
        onValueChangeFinished = {
            onSeek(sliderProgress)
            isSeeking = false
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
        enabled = durationMs > 0,
        valueRange = 0f..1f,
        colors =
            SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = PlayerAccentPurpleLight,
                inactiveTrackColor = PlayerProgressTrack,
            ),
    )
}

@Composable
fun RotateImage(
    musicResId: Int,
    isPlaying: Boolean,
) {
    val rotation =
        remember {
            Animatable(initialValue = 0f)
        }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        while (isActive) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec =
                    tween(
                        durationMillis = 10_000,
                        easing = LinearEasing,
                    ),
            )

            rotation.snapTo(rotation.value % 360f)
        }
    }

    Image(
        painter =
            painterResource(
                id = musicResId,
            ),
        contentDescription = "",
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 20.dp)
                .clip(CircleShape)
                .aspectRatio(1f)
                .graphicsLayer(
                    rotationZ = rotation.value,
                ),
        contentScale = ContentScale.Crop,
    )
}

@Preview(
    name = "HomeScreen",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun HomeScreenPreview() {
    MusicBasicTheme(dynamicColor = false) {
        HomeScreen(
            homeState = HomeScreenPreviewState,
            onMusicListClick = {},
            onShuffleClick = {},
            onPreviousClick = {},
            onPlayPauseClick = {},
            onNextClick = {},
            onRepeatClick = {},
            onSeekChange = {},
        )
    }
}
