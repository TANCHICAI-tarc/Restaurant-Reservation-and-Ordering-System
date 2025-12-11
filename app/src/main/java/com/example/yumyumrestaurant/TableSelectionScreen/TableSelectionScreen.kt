package com.example.yumyumrestaurant.TableSelectionScreen

import android.app.Application
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yumyumrestaurant.Reservation.ReservationUiState
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.Reservation.ReservationViewModelFactory
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModelFactory
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*





@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableSelectionScreen(
    onNavigateToDetails: (String) -> Unit
) {


    val context = LocalContext.current
    val application = context.applicationContext as Application

     val tableViewModel: TableViewModel = viewModel(
        factory = TableViewModelFactory(application)
    )

    val reservationViewModel: ReservationViewModel = viewModel(
        factory = ReservationViewModelFactory(application)
    )






    val reservationTableViewModelFactory = remember {
        ReservationTableViewModelFactory(tableViewModel, reservationViewModel)
    }

    val reservationTableViewModel: ReservationTableViewModel = viewModel(
        factory = reservationTableViewModelFactory
    )

    val interactionViewModel: InteractionViewModel = viewModel()





    TableSelectionScreenBody(
        reservationTableViewModel = reservationTableViewModel,

        interactionViewModel = interactionViewModel,
        onNavigateToDetails = onNavigateToDetails
    )

}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSelectionScreenBody(
    reservationTableViewModel: ReservationTableViewModel,

    interactionViewModel: InteractionViewModel,
    onNavigateToDetails: (tableId: String) -> Unit
) {


    val reservationTableUiState by reservationTableViewModel.reservationTableUiState.collectAsState()
    val reservationUiState by reservationTableViewModel.reservationUiState.collectAsState()
    val tableUiState by reservationTableViewModel.tableUiState.collectAsState()


//    val tableUiState by tableViewModel.uiState.collectAsState()
    val interactionUi by interactionViewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentTableForSheet by remember { mutableStateOf<TableEntity?>(null) }

    val boxWidth = 350.dp
    val boxHeight = 500.dp
    val density = LocalDensity.current
    val boxPxWidth = with(density) { boxWidth.toPx() }
    val boxPxHeight = with(density) { boxHeight.toPx() }

    val scrollState = rememberScrollState()



    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(reservationUiState.selectedZone, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(boxWidth, boxHeight)
                    .border(2.dp, Color.Black)
                    .clipToBounds()
                    .background(Color(0xFFF8F8F8))


                    .pointerInput(Unit) {
                        forEachGesture {
                            awaitPointerEventScope {
                                val firstDown = awaitFirstDown()
                                var pointerIds = mutableSetOf(firstDown.id)
                                var isZooming = false

                                do {
                                    val event = awaitPointerEvent()

                                    // multi-touch
                                    pointerIds = event.changes
                                        .filter { it.pressed }
                                        .map { it.id }
                                        .toMutableSet()

                                    val isMultiTouch = pointerIds.size > 1

                                    if (isMultiTouch) {
                                        isZooming = true

                                        interactionViewModel.onPinchGesture(
                                            zoomFactor = event.calculateZoom(),
                                            pan = event.calculatePan(),
                                            centroid = event.calculateCentroid(),
                                            boxWidthPx = boxPxWidth,
                                            boxHeightPx = boxPxHeight
                                        )

                                        event.changes.forEach { it.consume() }
                                    } else if (!isZooming) {
                                        val pointer = event.changes.first()

                                        val drag = pointer.positionChange()

                                        if (drag != Offset.Zero && interactionUi.scale > 1f) {
                                            interactionViewModel.onDragGesture(
                                                dragAmount = drag,
                                                boxWidthPx = boxPxWidth,
                                                boxHeightPx = boxPxHeight
                                            )
                                            interactionViewModel.addMoveDistance(drag)
                                            pointer.consume()
                                        }

                                        // tap detection
                                        if (event.type == PointerEventType.Release) {
                                            if (interactionUi.moveDistance < 5f) {
                                                // Call ViewModel tap logic (from Step 1)
                                                val tapX = (pointer.position.x - interactionUi.offset.x) / interactionUi.scale
                                                val tapY = (pointer.position.y - interactionUi.offset.y) / interactionUi.scale

                                                val tapResult = reservationTableViewModel.tableViewModel.onCanvasTap(
                                                    tapX,
                                                    tapY,
                                                    boxPxWidth,
                                                    boxPxHeight,
                                                    reservationUiState.selectedZone
                                                )

                                                when (tapResult) {
                                                    is TapResult.TableTapped -> {
                                                        currentTableForSheet = tapResult.table
                                                        scope.launch { sheetState.show() }
                                                    }
                                                    TapResult.PatioDoor -> reservationTableViewModel.reservationViewModel.toggleZone()
                                                    TapResult.None -> Unit
                                                }
                                            }

                                            interactionViewModel.resetMoveDistance()
                                        }
                                    }
                                } while (pointerIds.isNotEmpty())
                            }
                        }
                    }

            ) {

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = interactionUi.scale,
                            scaleY = interactionUi.scale,
                            translationX = interactionUi.offset.x,
                            translationY = interactionUi.offset.y,
                            transformOrigin = TransformOrigin(0f, 0f)
                        )
                ) {
                    val selectedZone = reservationUiState.selectedZone

                    // Draw tables
                    tableUiState.tables
                        .filter { it.zone.uppercase() == selectedZone }
                        .forEach { table ->
                            val px = table.xAxis * boxPxWidth
                            val py = table.yAxis * boxPxHeight

                            val status ="Available"

                            val tableColor = when {
                                tableUiState.selectedTables.contains(table) -> Color.Blue
                                status == "Available" -> Color.Green
                                status == "Reserved" -> Color.Red
                                else -> Color.Gray
                            }

                            drawCircle(tableColor, table.radius, Offset(px, py), style = Stroke(5f))


                            val paint = Paint().apply {
                                textSize = 20f
                                color =android.graphics.Color.BLACK
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                            }
                            val textOffset = (paint.descent() + paint.ascent()) / 2
                            drawContext.canvas.nativeCanvas.drawText(
                                table.label, px, py - textOffset, paint
                            )

                            // Draw seats
                            val seatR = 20f
                            val pad = 15f
                            repeat(table.seatCount) { i ->
                                val angle = (i * (2 * PI / table.seatCount)).toFloat()
                                val sx = px + (table.radius + seatR + pad) * cos(angle)
                                val sy = py + (table.radius + seatR + pad) * sin(angle)
                                drawCircle(Color.Gray, seatR, Offset(sx, sy))
                            }
                        }

                    // Draw regions
                    tableUiState.regions
                        .filter { it.zone.uppercase() == selectedZone }
                        .forEach { r ->
                            val left = r.xAxis * boxPxWidth
                            val top = r.yAxis * boxPxHeight
                            val w = r.width * boxPxWidth
                            val h = r.height * boxPxHeight

                            drawRect(Color(r.color.toInt()), Offset(left, top), Size(w, h))

                            val paint = Paint().apply {
                                textSize = 32f
                                textAlign = Paint.Align.CENTER
                                color = android.graphics.Color.BLACK
                                typeface = Typeface.DEFAULT_BOLD
                            }

                            val lines = r.label.split("\n")
                            val lineHeight = paint.fontSpacing
                            val startY = top + h / 2 - (lines.size - 1) * lineHeight / 2

                            lines.forEachIndexed { i, line ->
                                drawContext.canvas.nativeCanvas.drawText(
                                    line,
                                    left + w / 2,
                                    startY + i * lineHeight,
                                    paint
                                )
                            }
                        }
                }
            }

            // Remarks
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                Text("Remark:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                remarkMessage(Color.Green, "T0", "Table Available")
                remarkMessage(Color.Red, "T0", "Table Reserved")
                remarkMessage(Color.Blue, "T0", "Table Selected")
            }
        }

         SelectedTablesSummarySection(
            selectedTables = tableUiState.selectedTables,
            onRemove = { reservationTableViewModel.tableViewModel.removeTable(it) },
            onClear = { reservationTableViewModel.tableViewModel.clearSelection() }
        )


        Button(
            onClick = { /* Reserve */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Reserve")
        }
    }

    currentTableForSheet?.let { table ->
        ModalBottomSheet(
            onDismissRequest = { currentTableForSheet = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            TableBottomSheetContent(
                table = table,
                tableViewModel = reservationTableViewModel.tableViewModel,
                tableUiState = tableUiState,
                onSelect = {
                    reservationTableViewModel.tableViewModel.toggleTable(table)
                    currentTableForSheet = null
                },
                onNavigateToDetails = onNavigateToDetails,
                scope = scope,
                sheetState = sheetState
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableBottomSheetContent(
    table: TableEntity,
    tableViewModel: TableViewModel,
    tableUiState: TableUiState,
    onSelect: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    scope: CoroutineScope,
    sheetState: SheetState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(table.label, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        val nearRegions = tableViewModel.findNearRegions(table, tableUiState.regions)
        val price = table.seatCount * 10

        if (nearRegions.isNotEmpty()) {
            Text(
                "Near Region: ${nearRegions.joinToString()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text("Total Price: RM ${"%.2f".format(price.toFloat())}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
//        Text("Status: ${table.status}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
//        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigateToDetails(table.tableId)
                    scope.launch { sheetState.hide() }
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = "View Details", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text("View Details", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }

        Divider()
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSelect()
                scope.launch { sheetState.hide() }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select")
        }
    }
}

@Composable
fun SelectedTablesSummarySection(
    selectedTables: Set<TableEntity>,
    onRemove: (TableEntity) -> Unit,
    onClear: () -> Unit
) {
    if (selectedTables.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFEFEFEF),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                // Summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Selected: ${selectedTables.size} tables • ${selectedTables.sumOf { it.seatCount }} seats",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Total: RM ${"%.2f".format(selectedTables.sumOf { it.seatCount * 10 }.toFloat())}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Collapse arrow
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Expandable list of selected tables
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        selectedTables.forEach { table ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {

                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawCircle(
                                        color = when ("Available") {
                                            "Available"-> Color.Blue
                                            "Reserved" -> Color.Red
                                            else -> Color.Gray
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    "${table.label} (${table.seatCount} seats)",
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable { onRemove(table) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clear All
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Clear All",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onClear() }
                    )
                }
            }
        }
    }
}

@Composable
fun remarkMessage(tableColor: Color, tableLabel: String, statusText: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
        Canvas(modifier = Modifier.size(30.dp, 20.dp).padding(end = 4.dp)) {
            val radius = size.minDimension / 2
            val centerX = size.minDimension / 2
            val centerY = size.minDimension / 2

            drawCircle(color = tableColor, radius = radius, center = Offset(centerX, centerY), style = Stroke(width = 3f))

            val paint = Paint().apply {
                textSize = 20f
                color = android.graphics.Color.BLACK
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val textOffset = (paint.descent() + paint.ascent()) / 2
            drawContext.canvas.nativeCanvas.drawText(tableLabel, centerX, centerY - textOffset, paint)
        }

        Text(text = "-> $statusText", fontSize = 14.sp)
    }
}
































fun handleTap(
    event: PointerEvent,
    tableUi: TableUiState,
    reservationUi: ReservationUiState,
    onZoneChange: (String) -> Unit,
    interactionUi: InteractionUiState,
    boxPxWidth: Float,
    boxPxHeight: Float,
    onTableSelected: (TableEntity) -> Unit
) {
    val tapOffset = event.changes.first().position
    val mapX = (tapOffset.x - interactionUi.offset.x) / interactionUi.scale
    val mapY = (tapOffset.y - interactionUi.offset.y) / interactionUi.scale

    val zone = reservationUi.selectedZone

    // PATIO DOOR first
    tableUi.regions.filter { it.zone.uppercase() == zone }
        .forEach { region ->
            val left = region.xAxis * boxPxWidth
            val top = region.yAxis * boxPxHeight
            val right = left + region.width * boxPxWidth
            val bottom = top + region.height * boxPxHeight

            if (region.label == "Patio\nDoor" &&
                mapX in left..right && mapY in top..bottom
            ) {
                onZoneChange(if (zone == "INDOOR") "OUTDOOR" else "INDOOR")
                return
            }
        }

    tableUi.tables.filter { it.zone.uppercase() == zone }
        .find { table ->
            val tx = table.xAxis * boxPxWidth
            val ty = table.yAxis * boxPxHeight

            val d = hypot(mapX - tx, mapY - ty)
            d < table.radius + 60f
        }
        ?.let { onTableSelected(it) }
}
