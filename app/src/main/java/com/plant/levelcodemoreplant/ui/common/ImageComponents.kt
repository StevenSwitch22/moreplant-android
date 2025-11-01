package com.plant.levelcodemoreplant.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/**
 * 植物图片组件 - 优先显示图片，无图片时显示emoji
 * 图片命名格式：plant_200134.png
 */
@Composable
fun PlantImage(
    plantId: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceName = "plant_$plantId"
    val resourceId = context.resources.getIdentifier(
        resourceName,
        "drawable",
        context.packageName
    )
    
    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = "植物图片",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

/**
 * 装扮图片组件 - 优先显示图片，无图片时显示emoji
 * 图片命名格式：costume_32000832.jpg
 */
@Composable
fun CostumeImage(
    costumeId: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceName = "costume_$costumeId"
    val resourceId = context.resources.getIdentifier(
        resourceName,
        "drawable",
        context.packageName
    )
    
    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = "装扮图片",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
