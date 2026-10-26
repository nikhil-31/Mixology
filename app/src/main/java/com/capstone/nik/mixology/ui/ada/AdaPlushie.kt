package com.capstone.nik.mixology.ui.ada

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.unit.dp

private val PlushInk = SolidColor(Color.Black)

val AdaPlushieFilled: ImageVector by lazy {
    ImageVector.Builder(
        name = "AdaPlushieFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = plushPath {
                circle(8f, 6.05f, 3.15f)
                circle(8f, 5.55f, 1.35f)
            },
            pathFillType = PathFillType.EvenOdd,
            fill = PlushInk,
        )
        addPath(
            pathData = plushPath {
                circle(16f, 6.05f, 3.15f)
                circle(16f, 5.55f, 1.35f)
            },
            pathFillType = PathFillType.EvenOdd,
            fill = PlushInk,
        )
        addPath(plushPath { circle(4.35f, 15.35f, 2.05f) }, fill = PlushInk)
        addPath(plushPath { circle(19.65f, 15.35f, 2.05f) }, fill = PlushInk)
        addPath(
            pathData = plushPath {
                circle(12f, 14.55f, 7.15f)
                circle(9.55f, 13.35f, 0.95f)
                circle(14.45f, 13.35f, 0.95f)
                circle(12f, 15.05f, 0.48f)
                moveTo(9.35f, 16.35f)
                curveTo(10.45f, 18.05f, 13.55f, 18.05f, 14.65f, 16.35f)
                curveTo(13.5f, 17.05f, 10.5f, 17.05f, 9.35f, 16.35f)
                close()
            },
            pathFillType = PathFillType.EvenOdd,
            fill = PlushInk,
        )
    }.build()
}

val AdaPlushieOutlined: ImageVector by lazy {
    ImageVector.Builder(
        name = "AdaPlushieOutlined",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        listOf(
            Triple(8f, 6.05f, 3.15f),
            Triple(16f, 6.05f, 3.15f),
            Triple(12f, 14.55f, 7.15f),
            Triple(4.35f, 15.35f, 2.05f),
            Triple(19.65f, 15.35f, 2.05f),
        ).forEach { (cx, cy, radius) ->
            addPath(
                pathData = plushPath { circle(cx, cy, radius) },
                stroke = PlushInk,
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        addPath(
            pathData = plushPath {
                circle(9.55f, 13.35f, 0.7f)
                circle(14.45f, 13.35f, 0.7f)
            },
            fill = PlushInk,
        )
        addPath(
            pathData = plushPath {
                moveTo(9.5f, 16.4f)
                curveTo(10.6f, 17.9f, 13.4f, 17.9f, 14.5f, 16.4f)
            },
            stroke = PlushInk,
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
        )
    }.build()
}

private fun plushPath(block: PathBuilder.() -> Unit): List<PathNode> =
    PathBuilder().apply(block).nodes

private fun PathBuilder.circle(cx: Float, cy: Float, radius: Float) {
    val control = radius * 0.55228475f
    moveTo(cx, cy - radius)
    curveTo(cx + control, cy - radius, cx + radius, cy - control, cx + radius, cy)
    curveTo(cx + radius, cy + control, cx + control, cy + radius, cx, cy + radius)
    curveTo(cx - control, cy + radius, cx - radius, cy + control, cx - radius, cy)
    curveTo(cx - radius, cy - control, cx - control, cy - radius, cx, cy - radius)
    close()
}
