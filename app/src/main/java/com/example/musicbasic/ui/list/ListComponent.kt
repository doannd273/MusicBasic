package com.example.musicbasic.ui.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicbasic.R
import com.example.musicbasic.ui.theme.MusicBasicTheme

@Composable
fun OptionBottomSheet(
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        OptionBottomSheetItem(
            iconRes = R.drawable.ic_download,
            labelRes = R.string.download,
            contentDescriptionRes = R.string.content_description_download,
            onClick = onDownloadClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        OptionBottomSheetItem(
            iconRes = R.drawable.ic_share,
            labelRes = R.string.share,
            contentDescriptionRes = R.string.content_description_share,
            onClick = onShareClick,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun OptionBottomSheetItem(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    @StringRes contentDescriptionRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(contentDescriptionRes),
            modifier = Modifier.size(30.dp),
            tint = Color.White,
        )

        Text(
            text = stringResource(labelRes),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(
    name = "OptionBottomSheet",
    showBackground = true,
    backgroundColor = 0xFF24105A,
)
@Composable
private fun OptionBottomSheetPreview() {
    MusicBasicTheme(dynamicColor = false) {
        OptionBottomSheet(
            onDownloadClick = {},
            onShareClick = {},
        )
    }
}
