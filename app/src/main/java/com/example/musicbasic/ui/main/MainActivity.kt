package com.example.musicbasic.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicbasic.R
import com.example.musicbasic.extensions.repeatIcon
import com.example.musicbasic.extensions.repeatTint
import com.example.musicbasic.extensions.timeText
import com.example.musicbasic.model.Music
import com.example.musicbasic.ui.theme.MusicBasicTheme
import com.example.musicbasic.ui.theme.PlayerAccentPurple
import com.example.musicbasic.ui.theme.PlayerAccentPurpleLight
import com.example.musicbasic.ui.theme.PlayerPrimaryText
import com.example.musicbasic.ui.theme.PlayerProgressTrack
import com.example.musicbasic.ui.theme.PlayerSecondaryText
import com.example.musicbasic.ui.theme.PlayerShadowPurple
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.isActive

private val PlayerControlIconSize = 38.dp
private val PlayerPrimaryControlSize = 76.dp
private val MainScreenPreviewMusic = Music(
    id = 1,
    title = "Ophelia",
    author = "Steven Price",
    thumbnail = R.drawable.ic_background,
    musicResId = R.raw.mot_ngay_nao_do,
)
private val MainScreenPreviewState = MainState(
    currentMusic = MainScreenPreviewMusic,
    currentMusicIndex = 0,
    isPlaying = true,
    progress = 85_000f / 195_000f,
    currentPositionMs = 85_000L,
    durationMs = 195_000L,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        setContent {
            MusicBasicTheme {
                Scaffold(
                    modifier = Modifier,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                ) { innerPadding ->
                    MainRoute(
                        modifier = Modifier.padding(innerPadding),
                    )
                }

            }
        }
    }
}

@Composable
fun MainRoute(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreen(
        modifier = modifier,
        mainState = mainState,
        onEvent = viewModel::onEvent,
    )

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is MainEffect.MusicMessage -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainState: MainState,
    onEvent: (MainEvent) -> Unit,
) {
    val playPauseIcon = if (mainState.isPlaying) {
        R.drawable.ic_pause
    } else {
        R.drawable.ic_resume
    }
    val playPauseContentDescription = if (mainState.isPlaying) {
        R.string.content_description_pause
    } else {
        R.string.content_description_resume
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(PlayerShadowPurple)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RotateImage(
            musicResId = mainState.currentMusic?.thumbnail ?: R.drawable.ic_background,
            isPlaying = mainState.isPlaying
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = mainState.currentMusic?.title ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            color = PlayerPrimaryText,
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = mainState.currentMusic?.author ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = PlayerSecondaryText,
        )

        Spacer(modifier = Modifier.height(45.dp))

        PlaybackProgressSlider(
            progress = mainState.progress,
            durationMs = mainState.durationMs,
            onSeek = {
                onEvent(MainEvent.SeekChanged(it))
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mainState.currentPositionMs.timeText(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = PlayerPrimaryText,
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mainState.durationMs.timeText(),
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
                onClick = { onEvent(MainEvent.ShuffleClicked) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = stringResource(R.string.content_description_shuffle),
                    modifier = Modifier.size(PlayerControlIconSize),
                    tint = if (mainState.isShuffleEnabled) {
                        PlayerAccentPurpleLight
                    } else {
                        PlayerPrimaryText
                    },
                )
            }

            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(MainEvent.PreviousClicked) },
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
                    modifier = Modifier
                        .size(PlayerPrimaryControlSize)
                        .clip(CircleShape)
                        .background(PlayerAccentPurple),
                    onClick = {
                        onEvent(MainEvent.PlayPauseClicked)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = playPauseIcon),
                        contentDescription = stringResource(playPauseContentDescription),
                        modifier = Modifier.size(PlayerControlIconSize),
                        tint = PlayerPrimaryText,
                    )
                }
            }

            IconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    onEvent(MainEvent.NextClicked)
                },
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
                onClick = {
                    onEvent(MainEvent.RepeatClicked)
                },
            ) {
                Icon(
                    painter = painterResource(id = mainState.repeatMode.repeatIcon()),
                    contentDescription = stringResource(R.string.content_description_repeat),
                    modifier = Modifier.size(PlayerControlIconSize),
                    tint = mainState.repeatMode.repeatTint(),
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        enabled = durationMs > 0,
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor =  PlayerAccentPurpleLight,
            inactiveTrackColor = PlayerProgressTrack
        )
    )
}

@Composable
fun RotateImage(
    musicResId: Int,
    isPlaying: Boolean,
) {
    val rotation = remember {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        while (isActive) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(
                    durationMillis = 10_000,
                    easing = LinearEasing,
                ),
            )

            rotation.snapTo(rotation.value % 360f)
        }
    }

    Image(
        painter = painterResource(
            id = musicResId
        ),
        contentDescription = "",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 20.dp)
            .clip(CircleShape)
            .aspectRatio(1f)
            .graphicsLayer(
                rotationZ = rotation.value
            ),
        contentScale = ContentScale.Crop,
    )
}

@Preview(
    name = "MainScreen",
    showBackground = true,
    backgroundColor = 0xFF24105A,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun MainScreenPreview() {
    MusicBasicTheme(dynamicColor = false) {
        MainScreen(
            mainState = MainScreenPreviewState,
            onEvent = {},
        )
    }
}
