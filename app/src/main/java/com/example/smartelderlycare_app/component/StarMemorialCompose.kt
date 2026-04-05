package com.example.smartelderlycare_app.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarMemorialScreen(
    stars: List<MemorialStar>,
    onStarClicked: (MemorialStar) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = androidx.compose.material3.SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()
    var selectedStar by remember { mutableStateOf<MemorialStar?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            selectedStar?.let {star ->
                StarDetailBottomSheet(star)
            }
        },
        sheetPeekHeight = 0.dp,
        sheetShape = BottomSheetDefaults.ExpandedShape,
        sheetContainerColor = Color.Black.copy(alpha = 0.9f),
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(Color.Black)
        ) {
            StarField(
                stars = stars,
                onStarClicked = {
                    selectedStar = it
                    onStarClicked(it)
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            )
        }
    }
}

@Composable
fun StarField(
    stars: List<MemorialStar>,
    onStarClicked: (MemorialStar) -> Unit
) {
    val virtualWidth = 3000f
    val virtualHeight = 3000f
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val starsState = remember { stars.map { it.copy() }.toMutableList() }

    // 动画循环
    LaunchedEffect(Unit) {
        while (true) {
            starsState.forEach {star ->
                // 更新位置
                star.x += star.speedX
                star.y += star.speedY

                // 边界检测
                if (star.x < 0 || star.x > virtualWidth) {
                    star.speedX = -star.speedX
                }
                if (star.y < 0 || star.y > virtualHeight) {
                    star.speedY = -star.speedY
                }

                // 呼吸效果
                star.alpha = 0.5f + 0.5f * kotlin.math.sin(System.currentTimeMillis() / 1000f + star.id.hashCode())
            }
            delay(16) // 约60fps
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures {change, dragAmount ->
                    change.consume()
                    // 更新偏移量
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y

                    // 边界限制
                    offsetX = offsetX.coerceIn(-(virtualWidth - size.width), 0f)
                    offsetY = offsetY.coerceIn(-(virtualHeight - size.height), 0f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {tapOffset ->
                    // 计算虚拟坐标
                    val virtualX = tapOffset.x - offsetX
                    val virtualY = tapOffset.y - offsetY

                    // 检测点击的星星
                    starsState.forEach { star ->
                        val distance = sqrt(
                            (star.x - virtualX).pow(2) + (star.y - virtualY).pow(2)
                        )
                        if (distance < star.radius + 20f) {
                            onStarClicked(star)
                        }
                    }
                }
            }
    ) {
        // 绘制星星
        starsState.forEach { star ->
            val starOffset = Offset(star.x + offsetX, star.y + offsetY)
            
            // 绘制星星辉光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = star.alpha * 0.8f),
                        Color.White.copy(alpha = 0f)
                    ),
                    center = starOffset,
                    radius = star.radius * 3
                ),
                radius = star.radius * 3,
                center = starOffset
            )
            
            // 绘制星星主体
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius,
                center = starOffset
            )
        }
    }
}

@Composable
fun StarDetailBottomSheet(star: MemorialStar) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize(0.5f)
            )
        }
        Text(
            text = star.name,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
        Text(
            text = star.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .padding(horizontal = 32.dp)
        )
    }
}

// 生成示例星星数据
fun generateSampleStars(count: Int): List<MemorialStar> {
    val stars = mutableListOf<MemorialStar>()
    for (i in 1..count) {
        stars.add(
            MemorialStar(
                id = "star_$i",
                name = "逝者 $i",
                description = "这里是逝者 $i 的生前事迹和寄语...",
                profileImageUrl = null,
                x = Random.nextFloat() * 3000f,
                y = Random.nextFloat() * 3000f,
                radius = Random.nextFloat() * 10f + 5f,
                speedX = Random.nextFloat() * 0.5f - 0.25f,
                speedY = Random.nextFloat() * 0.5f - 0.25f,
                alpha = 1f
            )
        )
    }
    return stars
}
