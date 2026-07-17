package com.example.musicbasic.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    @StringRes topBarLabel: Int,
    @DrawableRes topBarIconLeft: Int,
    @DrawableRes topBarIconRight: Int? = null,
    onIconLeftClick: () -> Unit,
    onIconRightClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                modifier = Modifier.size(48.dp),
                onClick = {
                    onIconLeftClick()
                },
            ) {
                Icon(
                    painter = painterResource(id = topBarIconLeft),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White,
                )
            }

            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = topBarLabel),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (topBarIconRight != null) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = {
                        onIconRightClick()
                    },
                ) {
                    Icon(
                        painter = painterResource(id = topBarIconRight),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White,
                    )
                }
            }
        }

        HorizontalDivider()
    }
}
