package com.example.yumyumrestaurant.data

import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import com.example.yumyumrestaurant.data.TableData.TableStatus
import com.example.yumyumrestaurant.data.TableData.ZoneType
import java.time.LocalDate
import java.time.LocalTime

class Converters {


    @TypeConverter
    fun fromZoneType(zone: ZoneType): String = zone.name

    @TypeConverter
    fun toZoneType(name: String): ZoneType = ZoneType.valueOf(name)

    @TypeConverter
    fun fromTableStatus(status: TableStatus): String = status.name

    @TypeConverter
    fun toTableStatus(name: String): TableStatus = TableStatus.valueOf(name)


    @TypeConverter
    fun fromColor(color: Color): Long = color.value.toLong()

    @TypeConverter
    fun toColor(value: Long): Color = Color(value)


    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

}








//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TableSelectionScreenBody(
//    reservationViewModel: ReservationFormScreenViewModel,
//    tableViewModel: TableViewModel,
//    onNavigateToDetails: (tableId: String) -> Unit
//) {
//    val reservationUiState by reservationViewModel.uiState.collectAsState()
//    val tableUiState by tableViewModel.uiState.collectAsState()
//    val scope = rememberCoroutineScope()
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//
//    var selectedTables by remember { mutableStateOf(mutableSetOf<TableEntity>()) }
//    var currentTableForSheet by remember { mutableStateOf<TableEntity?>(null) }
//
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    val boxWidth = 350.dp
//    val boxHeight = 500.dp
//    val density = LocalDensity.current
//    val boxPxWidth = with(density) { boxWidth.toPx() }
//    val boxPxHeight = with(density) { boxHeight.toPx() }
//    var moveDistance by remember { mutableStateOf(0f) }
//
//    val scrollState = rememberScrollState()
//
//    Column(modifier = Modifier.fillMaxSize()) {
//
//        // Scrollable area
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .verticalScroll(scrollState)
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(reservationUiState.selectedZone, fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Box(
//                modifier = Modifier
//                    .size(boxWidth, boxHeight)
//                    .border(2.dp, Color.Black)
//                    .clipToBounds()
//                    .background(Color(0xFFF8F8F8))
//
//
//                    .pointerInput(Unit) {
//                        awaitPointerEventScope {
//                            while (true) {
//                                val event = awaitPointerEvent()
//                                val change = event.changes.first()
//
//                                val isMultiTouch = event.changes.size > 1
//                                val dragAmount = change.positionChange()
//                                moveDistance += dragAmount.getDistance()
//                                Log.d("Debug Move",event.changes.size.toString())
//
//                                if (isMultiTouch) {
//                                    val zoom = event.calculateZoom()
//                                    val pan = event.calculatePan()
//                                    val centroid = event.calculateCentroid()
//
//                                    val newScale = (scale * zoom).coerceIn(1f, 4f)
//                                    val newOffset = (offset - centroid) * (newScale / scale) + centroid + pan
//
//                                    val minX = boxPxWidth - boxPxWidth * newScale
//                                    val minY = boxPxHeight - boxPxHeight * newScale
//                                    offset = Offset(newOffset.x.coerceIn(minX, 0f), newOffset.y.coerceIn(minY, 0f))
//                                    scale = newScale
//
//                                    event.changes.forEach { it.consume() }
//                                    continue
//                                }
//
//                                // --- DRAG when zoomed ---
//                                if (scale > 1f && dragAmount != Offset.Zero) {
//                                    val minX = boxPxWidth - boxPxWidth * scale
//                                    val minY = boxPxHeight - boxPxHeight * scale
//                                    offset = Offset(
//                                        (offset.x + dragAmount.x).coerceIn(minX, 0f),
//                                        (offset.y + dragAmount.y).coerceIn(minY, 0f)
//                                    )
//                                    change.consume()
//                                    continue
//                                }
//
//                                // --- TAP (single-finger + minimal movement) ---
//                                if (!isMultiTouch && moveDistance < 5f && event.type == PointerEventType.Release) {
//                                    val tapOffset = change.position
//                                    val mapX = (tapOffset.x - offset.x) / scale
//                                    val mapY = (tapOffset.y - offset.y) / scale
//
//                                    // Handle table selection
//                                    val clickedTable = tableUiState.tables
//                                        .filter { it.zone.uppercase() == reservationUiState.selectedZone }
//                                        .find { table ->
//                                            val tablePixelX = table.xAxis * boxPxWidth
//                                            val tablePixelY = table.yAxis * boxPxHeight
//                                            val distance = sqrt((mapX - tablePixelX).pow(2) + (mapY - tablePixelY).pow(2))
//                                            distance < (table.radius + 60f)
//                                        }
//
//                                    clickedTable?.let { table ->
//                                        currentTableForSheet = table
//                                        scope.launch { sheetState.show() }
//                                    }
//
//                                    tableUiState.regions.filter { it.zone.uppercase() == reservationUiState.selectedZone }
//                                        .forEach { region ->
//                                            val zLeft = region.xAxis * boxPxWidth
//                                            val zTop = region.yAxis * boxPxHeight
//                                            val zWidth = region.width * boxPxWidth
//                                            val zHeight = region.height * boxPxHeight
//
//                                            if (region.label == "Patio\nDoor" &&
//                                                mapX in zLeft..(zLeft + zWidth) &&
//                                                mapY in zTop..(zTop + zHeight)
//                                            ) {
//                                                val newZone =
//                                                    if (reservationUiState.selectedZone == "INDOOR") "OUTDOOR" else "INDOOR"
//                                                reservationViewModel.updateSelectedZone(newZone)
//
//
//                                            }
//                                        }
//
//                                    moveDistance = 0f
//                                }
//
//                                // Reset movement when finger is lifted
//                                if (event.type == PointerEventType.Release) {
//                                    moveDistance = 0f
//                                }
//                            }
//                        }
//                    }
//            ) {
//                Canvas(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .graphicsLayer(
//                            scaleX = scale,
//                            scaleY = scale,
//                            translationX = offset.x,
//                            translationY = offset.y,
//                            transformOrigin = TransformOrigin(0f, 0f)
//                        )
//                ) {
//                    val selectedZone = reservationUiState.selectedZone
//
//                    // Draw tables
//                    tableUiState.tables
//                        .filter { it.zone.uppercase() == selectedZone }
//                        .forEach { table ->
//                            val tablePixelX = table.xAxis * boxPxWidth
//                            val tablePixelY = table.yAxis * boxPxHeight
//
//
//                            val tableStatus = TableStatus.valueOf(table.status)
//                            val tableColor = when {
//                                selectedTables.contains(table) -> Color.Blue
//                                tableStatus == TableStatus.AVAILABLE -> Color.Green
//                                tableStatus == TableStatus.RESERVED -> Color.Red
//                                else -> Color.Gray
//                            }
//
//                            drawCircle(
//                                color = tableColor,
//                                radius = table.radius,
//                                center = Offset(tablePixelX, tablePixelY),
//                                style = Stroke(width = 5f)
//                            )
//
//                            val paint = Paint().apply {
//                                textSize = 20f
//                                color = android.graphics.Color.BLACK
//                                textAlign = Paint.Align.CENTER
//                                typeface = Typeface.DEFAULT_BOLD
//                            }
//                            val textOffset = (paint.descent() + paint.ascent()) / 2
//                            drawContext.canvas.nativeCanvas.drawText(
//                                table.label,
//                                tablePixelX,
//                                tablePixelY - textOffset,
//                                paint
//                            )
//
//                            // Draw seats
//                            val seatRadius = 20f
//                            val padding = 15f
//                            for (i in 0 until table.seatCount) {
//                                val angle = (i * (2 * PI / table.seatCount)).toFloat()
//                                val sX =
//                                    tablePixelX + (table.radius + seatRadius + padding) * cos(angle)
//                                val sY =
//                                    tablePixelY + (table.radius + seatRadius + padding) * sin(angle)
//                                drawCircle(Color.Gray, seatRadius, Offset(sX, sY))
//                            }
//                        }
//
//                    // Draw regions
//                    tableUiState.regions
//                        .filter { it.zone.uppercase() == selectedZone }
//                        .forEach { region ->
//                            val zLeft = region.xAxis * boxPxWidth
//                            val zTop = region.yAxis * boxPxHeight
//                            val zWidth = region.width * boxPxWidth
//                            val zHeight = region.height * boxPxHeight
//
//                            drawRect(
//                                color = Color(region.color.toInt()),
//                                topLeft = Offset(zLeft, zTop),
//                                size = Size(zWidth, zHeight)
//                            )
//
//                            val lines = region.label.split("\n")
//                            val paint = Paint().apply {
//                                textSize = 32f
//                                textAlign = Paint.Align.CENTER
//                                color = android.graphics.Color.BLACK
//                                typeface = Typeface.DEFAULT_BOLD
//                            }
//
//                            val lineHeight = paint.fontSpacing
//                            val startY = zTop + zHeight / 2 - (lines.size - 1) * lineHeight / 2
//
//                            lines.forEachIndexed { index, line ->
//                                drawContext.canvas.nativeCanvas.drawText(
//                                    line,
//                                    zLeft + zWidth / 2,
//                                    startY + index * lineHeight,
//                                    paint
//                                )
//                            }
//                        }
//                }
//            }
//
//            // Remarks
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 16.dp, top = 12.dp, bottom = 16.dp)
//            ) {
//                Text("Remark:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                remarkMessage(Color.Green, "T0", "Table Available")
//                remarkMessage(Color.Red, "T0", "Table Reserved")
//                remarkMessage(Color.Blue, "T0", "Table Selected")
//            }
//        }
//
//
//        if (selectedTables.isNotEmpty()) {
//            Surface(
//                modifier = Modifier.fillMaxWidth(),
//                color = Color(0xFFEFEFEF),
//                shadowElevation = 4.dp
//            ) {
//
//                var expanded by remember { mutableStateOf(false) }
//
//                Column(modifier = Modifier.padding(12.dp)) {
//
//                    // Summary: ONE line, clean and non-redundant
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { expanded = !expanded },
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                "Selected: ${selectedTables.size} tables • ${selectedTables.sumOf { it.seatCount }} seats",
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 15.sp
//                            )
//                            Text(
//                                "Total: RM ${"%.2f".format(selectedTables.sumOf { it.seatCount * 10 }.toFloat())}",
//                                fontSize = 14.sp,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//
//                        // Collapse arrow
//                        Icon(
//                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp
//                            else Icons.Default.KeyboardArrowDown,
//                            contentDescription = "Toggle",
//                            tint = Color.Black
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    // Expandable list of selected tables
//                    AnimatedVisibility(visible = expanded) {
//                        Column(modifier = Modifier.padding(top = 6.dp)) {
//                            selectedTables.forEach { table ->
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 6.dp)
//                                ) {
//
//                                    Canvas(modifier = Modifier.size(20.dp)) {
//                                        drawCircle(
//                                            color = when (TableStatus.valueOf(table.status)) {
//                                                TableStatus.AVAILABLE -> Color.Blue
//                                                TableStatus.RESERVED -> Color.Red
//                                                else -> Color.Gray
//                                            }
//                                        )
//                                    }
//
//                                    Spacer(modifier = Modifier.width(10.dp))
//
//                                    Text(
//                                        "${table.label} (${table.seatCount} seats)",
//                                        fontSize = 14.sp,
//                                        modifier = Modifier.weight(1f)
//                                    )
//
//                                    Icon(
//                                        imageVector = Icons.Default.Close,
//                                        contentDescription = "Remove",
//                                        tint = Color.Red,
//                                        modifier = Modifier
//                                            .size(22.dp)
//                                            .clickable {
//                                                selectedTables = selectedTables.toMutableSet().apply {
//                                                    remove(table)
//                                                }
//                                            }
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Clear All
//                    Row(
//                        horizontalArrangement = Arrangement.End,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            "Clear All",
//                            color = Color.Red,
//                            fontSize = 14.sp,
//                            modifier = Modifier.clickable {
//                                selectedTables = mutableSetOf()
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//
//        // Reserve button
//        Button(
//            onClick = { /* Reserve logic */ },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp)
//        ) {
//            Text("Reserve")
//        }
//    }
//
//    // Modal Bottom Sheet for table info
//    currentTableForSheet?.let { table ->
//        ModalBottomSheet(
//            onDismissRequest = { currentTableForSheet = null },
//            sheetState = sheetState,
//            dragHandle = { BottomSheetDefaults.DragHandle() }
//        ) {
//            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
//                Text(table.label, fontSize = 26.sp, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(12.dp))
//
//                val nearRegions = tableViewModel.findNearRegions(table, tableUiState.regions)
//                val price = table.seatCount * 10
//
//                if (nearRegions.isNotEmpty()) {
//                    Text("Near Region: $nearRegions", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//
//                Text("Total Price: RM ${"%.2f".format(price.toFloat())}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//                Spacer(modifier = Modifier.height(12.dp))
//                Text("Status: ${table.status}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            onNavigateToDetails(table.tableId)
//                            scope.launch { sheetState.hide() }
//                            currentTableForSheet = null
//                        }
//                        .padding(vertical = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(Icons.Default.Info, contentDescription = "View Details", tint = MaterialTheme.colorScheme.primary)
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text("View Details", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
//                }
//
//                Divider()
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Button(
//                    onClick = {
//                        selectedTables = selectedTables.toMutableSet().apply {
//                            if (contains(table)) remove(table) else add(table)
//                        }
//
//
//                        scope.launch {
//                            sheetState.hide()
//                            currentTableForSheet = null
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Select")
//                }
//
//
//            }
//        }
//    }
//}