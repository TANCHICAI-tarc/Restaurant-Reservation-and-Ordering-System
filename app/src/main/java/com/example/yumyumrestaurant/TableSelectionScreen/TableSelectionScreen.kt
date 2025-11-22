package com.example.yumyumrestaurant.TableSelectionScreen

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yumyumrestaurant.data.TableData.FloorItem
import com.example.yumyumrestaurant.data.TableData.TableStatus
import com.example.yumyumrestaurant.data.TableData.ZoneType
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSelectionScreen() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var selectedTable by remember { mutableStateOf<FloorItem.Table?>(null) }

    // Zoom/Pan State
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // --- 1. DATA ---
    val floorItems = remember {
        mutableStateListOf<FloorItem>(
            FloorItem.Table("t1", "T1", ZoneType.INDOOR,0.1f, 0.1f, 30f, 6),
            FloorItem.Table("t2", "T2", ZoneType.INDOOR,0.3f, 0.1f, 30f, 4),
            FloorItem.Table("t3", "T3", ZoneType.INDOOR,0.1f, 0.25f, 30f, 4),
            FloorItem.Table("t4", "T4", ZoneType.INDOOR,0.3f, 0.25f, 30f, 4),
            FloorItem.Table("t5", "T5", ZoneType.INDOOR,0.1f, 0.5f, 25f, 2),
            FloorItem.Table("t6", "T6", ZoneType.INDOOR,0.3f, 0.5f, 25f, 2),
            FloorItem.Table("t7", "T7", ZoneType.INDOOR,0.1f, 0.6f, 25f, 2),
            FloorItem.Table("t8", "T8", ZoneType.INDOOR,0.3f, 0.6f, 25f, 2),
            FloorItem.Table("t9", "T9", ZoneType.INDOOR,0.1f, 0.7f, 25f, 2),
            FloorItem.Table("t10", "T10", ZoneType.INDOOR,0.3f, 0.7f, 25f, 2),
            FloorItem.Table("t11", "T11", ZoneType.INDOOR,0.1f, 0.85f, 25f, 6),
            FloorItem.Table("t12", "T12", ZoneType.INDOOR,0.3f, 0.85f, 25f, 3),
            FloorItem.Table("t13", "T13", ZoneType.INDOOR,0.5f, 0.85f, 25f, 3),
            FloorItem.Table("t14", "T14", ZoneType.INDOOR,0.65f, 0.2f, 25f, 6),

            FloorItem.Region("Serving", ZoneType.INDOOR, x = 0.45f, y = 0.3f,  width = 0.15f, height = 0.4f, color = Color.LightGray),
            FloorItem.Region("Kitchen", ZoneType.INDOOR, x = 0.65f, y = 0.3f, width = 0.3f, height = 0.5f, color = Color.DarkGray),
            FloorItem.Region("Cooler", ZoneType.INDOOR, x = 0.85f, y = 0.05f, width = 0.1f, height = 0.15f, color = Color.Cyan),


            FloorItem.Region("Door", ZoneType.INDOOR, x = 0.01f, y = 0.35f, width = 0.1f, height = 0.1f, color = Color(0xFF795548)),


            FloorItem.Region("Toilet Male", ZoneType.INDOOR, x = 0.65f, y = 0.85f, width = 0.3f, height = 0.05f, color = Color(0xFF03A9F4)),
            FloorItem.Region("Toilet Female", ZoneType.INDOOR, x = 0.65f, y = 0.91f, width = 0.3f, height = 0.05f, color = Color(0xFFE91E63)),
            FloorItem.Region("Window", ZoneType.INDOOR, x = 0.5f, y = 0.02f, width = 0.2f, height = 0.05f, color = Color(0xFFB3E5FC)),
            FloorItem.Region("Window", ZoneType.INDOOR, x = 0.15f, y = 0.95f, width = 0.2f, height = 0.05f, color = Color(0xFFB3E5FC))

//            FloorItem.Table("t15", "T15", ZoneType.OUTDOOR, 0.6f, 0.1f, 30f, 2),
//            FloorItem.Table("t16", "T16", ZoneType.OUTDOOR, 0.75f, 0.1f, 30f, 2),
//            FloorItem.Table("t17", "T17", ZoneType.OUTDOOR, 0.6f, 0.25f, 30f, 4),
//            FloorItem.Table("t18", "T18", ZoneType.OUTDOOR, 0.75f, 0.25f, 30f, 4),
//            FloorItem.Table("t19", "T19", ZoneType.OUTDOOR, 0.6f, 0.4f, 30f, 6),
//            FloorItem.Table("t20", "T20", ZoneType.OUTDOOR, 0.75f, 0.4f, 30f, 6),
//            FloorItem.Table("t21", "T21", ZoneType.OUTDOOR, 0.6f, 0.6f, 25f, 2),
//            FloorItem.Table("t22", "T22", ZoneType.OUTDOOR, 0.75f, 0.6f, 25f, 2),
//            FloorItem.Table("t23", "T23", ZoneType.OUTDOOR, 0.6f, 0.75f, 25f, 4),
//            FloorItem.Table("t24", "T24", ZoneType.OUTDOOR, 0.75f, 0.75f, 25f, 4),
//            FloorItem.Table("t25", "T25", ZoneType.OUTDOOR, 0.6f, 0.9f, 25f, 6),
//            FloorItem.Table("t26", "T26", ZoneType.OUTDOOR, 0.75f, 0.9f, 25f, 6),
//
//            FloorItem.Region("Fountain",ZoneType.OUTDOOR,0.01f, 0.05f, 0.4f, 0.2f, color = Color(0xFF64B5F6)),
//
//            FloorItem.Region("Door", ZoneType.OUTDOOR, x = 0.01f, y = 0.35f, width = 0.1f, height = 0.1f, color = Color(0xFF795548)),

        )
    }

    // --- 2. FIXED SIZE CALCULATIONS ---
    val boxWidth = 350.dp
    val boxHeight = 500.dp
    val density = LocalDensity.current
    val boxPxWidth = with(density) { boxWidth.toPx() }
    val boxPxHeight = with(density) { boxHeight.toPx() }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column {
            Box(
                modifier = Modifier
                    .size(boxWidth, boxHeight)
                    .border(2.dp, Color.Black)
                    .clipToBounds()
                    .background(Color(0xFFF8F8F8))
                    // --- 3. GESTURE HANDLING ---
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            // A. Transform "Touch Screen Point" -> "Map Point"
                            // This math assumes the map starts at (0,0) and scales from top-left
                            val mapX = (tapOffset.x - offset.x) / scale
                            val mapY = (tapOffset.y - offset.y) / scale

                            val clickedTable =
                                floorItems.filterIsInstance<FloorItem.Table>().find { table ->
                                    val tablePixelX = table.x * boxPxWidth
                                    val tablePixelY = table.y * boxPxHeight

                                    val distance = sqrt(
                                        (mapX - tablePixelX).pow(2) +
                                                (mapY - tablePixelY).pow(2)
                                    )

                                    // Hitbox Buffer: +60px to make it easier to press
                                    distance < (table.radius + 60f)
                                }

                            if (clickedTable != null) {
                                selectedTable = clickedTable
                                scope.launch { sheetState.show() }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(1f, 4f)

                            // Standard Google Maps style zoom math
                            val tempOffset =
                                (offset - centroid) * (newScale / oldScale) + centroid + pan

                            val scaledWidth = boxPxWidth * newScale
                            val scaledHeight = boxPxHeight * newScale
                            val minX = boxPxWidth - scaledWidth
                            val minY = boxPxHeight - scaledHeight

                            offset = Offset(
                                x = tempOffset.x.coerceIn(minX, 0f),
                                y = tempOffset.y.coerceIn(minY, 0f)
                            )
                            scale = newScale
                        }
                    }
            ) {
                // --- 4. DRAWING ---
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,

                            transformOrigin = TransformOrigin(0f, 0f)
                        )
                ) {
                    floorItems.forEach { item ->
                        when (item) {
                            is FloorItem.Table -> {
                                val tablePixelX = item.x * boxPxWidth
                                val tablePixelY = item.y * boxPxHeight

                                val tableColor = when {
                                    item.id == selectedTable?.id -> Color.Blue
                                    item.status == TableStatus.Available -> Color.Green
                                    item.status == TableStatus.Reserved -> Color.Red
                                    else -> Color.Gray
                                }

                                // 1. Draw the Circle (Ring)
                                drawCircle(
                                    color = tableColor,
                                    radius = item.radius,
                                    center = Offset(tablePixelX, tablePixelY),
                                    style = Stroke(width = 5f)
                                )

                                // 2. Draw the Text (Label) inside the circle
                                val paint = Paint().apply {
                                    textSize = 20f // Adjust text size to fit inside the circle
                                    color = android.graphics.Color.BLACK
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.DEFAULT_BOLD
                                }

                                // Calculate vertical centering for the text
                                // (descent + ascent) / 2 gives the distance from center to baseline
                                val textOffset = (paint.descent() + paint.ascent()) / 2

                                drawContext.canvas.nativeCanvas.drawText(
                                    item.label,          // The text (e.g., "T1")
                                    tablePixelX,         // Center X
                                    tablePixelY - textOffset, // Center Y adjusted for text height
                                    paint
                                )

                                // 3. Draw Seats (Chairs)
                                val seatRadius = 20f
                                val padding = 15f
                                for (i in 0 until item.seatCount) {
                                    val angle = (i * (2 * PI / item.seatCount)).toFloat()
                                    val sX =
                                        tablePixelX + (item.radius + seatRadius + padding) * cos(
                                            angle
                                        )
                                    val sY =
                                        tablePixelY + (item.radius + seatRadius + padding) * sin(
                                            angle
                                        )
                                    drawCircle(Color.Gray, seatRadius, Offset(sX, sY))
                                }
                            }

                            is FloorItem.Region -> {
                                val zLeft = item.x * boxPxWidth
                                val zTop = item.y * boxPxHeight
                                val zWidth = item.width * boxPxWidth
                                val zHeight = item.height * boxPxHeight

                                drawRect(item.color, Offset(zLeft, zTop), Size(zWidth, zHeight))

                                drawContext.canvas.nativeCanvas.drawText(
                                    item.label,
                                    zLeft + zWidth / 2,
                                    zTop + zHeight / 2 + 15f,
                                    Paint().apply {
                                        textSize = 32f; textAlign = Paint.Align.CENTER
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // space between box and text
             Text(
                text = "Remark:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp) // Add spacing after the header
            )

            Column {
                remarkMessage(
                    tableColor = Color.Green,
                    tableLabel = "T0",
                    statusText = "Table Available"
                )

                // --- Use the Reusable Composable for Unavailable Tables ---
                remarkMessage(
                    tableColor = Color.Red,
                    tableLabel = "T0",
                    statusText = "Table Reserved"
                )

                // --- Use the Reusable Composable for Unavailable Tables ---
                remarkMessage(
                    tableColor = Color.Blue,
                    tableLabel = "T0",
                    statusText = "Table be Selected"
                )
            }


        }

        selectedTable?.let { table ->
            ModalBottomSheet(
                onDismissRequest = { selectedTable = null },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Table: ${table.label}", fontSize = 24.sp)
                    Button(
                        onClick = { /* Reserve */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) { Text("Reserve") }
                }
            }
        }







    }




}

@Composable
fun remarkMessage(
    tableColor: Color,
    tableLabel: String,
    statusText: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
        Canvas(
            modifier = Modifier
                .size(30.dp, 20.dp) // Maintain size consistency
                .padding(end = 4.dp)
        ) {
            val radius = size.minDimension / 2
            val centerX = size.minDimension / 2
            val centerY = size.minDimension / 2

            // Draw the colored circle ring
            drawCircle(
                color = tableColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f)
            )

            // Draw the fixed label text (T0)
            val paint = Paint().apply {
                textSize = 20f
                color = android.graphics.Color.BLACK
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val textOffset = (paint.descent() + paint.ascent()) / 2

            drawContext.canvas.nativeCanvas.drawText(
                tableLabel, // Use the provided label
                centerX,
                centerY - textOffset,
                paint
            )
        }

        Text(
            text = "-> $statusText",
            fontSize = 14.sp
        )
    }
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
}

fun findTablesNearRegion(
    tables: List<FloorItem.Table>,
    region: FloorItem.Region,
    maxDistance: Float = 0.2f
): List<FloorItem.Table> {
    val centerX = region.x + region.width / 2
    val centerY = region.y + region.height / 2

    return tables
        .map { it to distance(it.x, it.y, centerX, centerY) }
        .filter { (_, d) -> d <= maxDistance }
        .sortedBy { it.second }
        .map { it.first }
}