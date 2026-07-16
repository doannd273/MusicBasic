package com.example.musicbasic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicbasic.ui.theme.MusicBasicTheme
import com.example.musicbasic.ui.theme.PlayerAccentPurple
import com.example.musicbasic.ui.theme.PlayerAccentPurpleLight
import com.example.musicbasic.ui.theme.PlayerPrimaryText
import com.example.musicbasic.ui.theme.PlayerProgressTrack
import com.example.musicbasic.ui.theme.PlayerSecondaryText
import com.example.musicbasic.ui.theme.PlayerShadowPurple
import dagger.hilt.android.AndroidEntryPoint

private val MainScreenPreviewState = MainState(
    progress = 85f / 195f,
    startTime = 85L,
    endTime = 195L,
)
private val PlayerControlIconSize = 38.dp
private val PlayerPrimaryControlSize = 76.dp

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
                when(effect) {
                    MainEffect.PlayMusicSuccess -> {

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
    val playPauseIcon = if (mainState.isPause) {
        R.drawable.ic_resume
    } else {
        R.drawable.ic_pause
    }
    val playPauseContentDescription = if (mainState.isPause) {
        R.string.content_description_resume
    } else {
        R.string.content_description_pause
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(PlayerShadowPurple).statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_background),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 20.dp)
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .aspectRatio(1f),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.track_title_ophelia),
            modifier = Modifier,
            style = MaterialTheme.typography.displaySmall,
            color = PlayerPrimaryText,
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = stringResource(R.string.track_artist_steven_price),
            modifier = Modifier,
            style = MaterialTheme.typography.titleLarge,
            color = PlayerSecondaryText,
        )

        Spacer(modifier = Modifier.height(45.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            progress = { mainState.progress },
            color = PlayerAccentPurpleLight,
            trackColor = PlayerProgressTrack,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mainState.startTime.timeText(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = PlayerPrimaryText,
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mainState.endTime.timeText(),
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
                onClick = {
                    onEvent(MainEvent.ShuffleClicked)
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = stringResource(R.string.content_description_shuffle),
                    modifier = Modifier.size(PlayerControlIconSize),
                    tint = PlayerPrimaryText,
                )
            }

            IconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    onEvent(MainEvent.PreviousClicked)
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_previous),
                    contentDescription = stringResource(R.string.content_description_previous),
                    modifier = Modifier.size(PlayerControlIconSize),
                    tint = PlayerPrimaryText,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f),
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
                    painter = painterResource(id = R.drawable.ic_repeat),
                    contentDescription = stringResource(R.string.content_description_repeat),
                    modifier = Modifier.size(PlayerControlIconSize),
                    tint = PlayerPrimaryText,
                )
            }
        }
    }
}

@Preview(
    name = "MainScreen",
    showBackground = true,
    backgroundColor = 0xFF070215,
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
