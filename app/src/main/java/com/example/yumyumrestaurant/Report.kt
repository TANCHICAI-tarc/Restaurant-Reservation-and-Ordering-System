

package com.example.yumyumrestaurant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.yumyumrestaurant.ui.MonthlyPaymentData
import com.example.yumyumrestaurant.ui.MonthlyReservationData
import com.example.yumyumrestaurant.ui.ReportViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.text.Regex


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Report(navController: NavHostController, reportViewModel: ReportViewModel = viewModel()) {
    val context = LocalContext.current

    val sortedMonthlyReservations by reportViewModel.sortedMonthlyReservations.collectAsState()
    val sortedMonthlyPayments by reportViewModel.sortedMonthlyPayments.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val errorMessage by reportViewModel.errorMessage.collectAsState()
    val selectedYear by reportViewModel.selectedYear.collectAsState()
    val reportType by reportViewModel.reportType.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var yearInput by remember { mutableStateOf(selectedYear.toString()) }
    var yearError by remember { mutableStateOf<String?>(null) }

    var isGeneratingPDF by remember { mutableStateOf(false) }
    var pdfErrorMessage by remember { mutableStateOf<String?>(null) }
    var pdfSuccessMessage by remember { mutableStateOf<String?>(null) }

    val currentYear = LocalDate.now().year

    LaunchedEffect(selectedYear) {
        yearInput = selectedYear.toString()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            "Yearly Report",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val reportTypes = listOf("Reservation Report", "Sales Report")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = reportType.ifEmpty { "Select Report Type" },
                onValueChange = {},
                label = { Text("Report Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                reportTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            reportViewModel.setReportType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = yearInput,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("\\d*"))) {
                    yearInput = newValue
                    yearError = null
                }
            },
            label = { Text("Year") },
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter year (0-$currentYear)") },
            modifier = Modifier.fillMaxWidth(),
            isError = yearError != null,
            supportingText = {
                if (yearError != null) {
                    Text(yearError!!, color = Color.Red)
                } else {
                    Text("Enter year between 0 and $currentYear")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    validateAndGenerate(reportViewModel, yearInput, currentYear, ::setYearError)
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    validateAndGenerate(reportViewModel, yearInput, currentYear, ::setYearError)
                    pdfErrorMessage = null
                    pdfSuccessMessage = null
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("View Report")
                }
            }

            Button(
                onClick = {
                    val yearErrorMsg = validateYear(yearInput, currentYear)
                    if (yearErrorMsg != null) {
                        yearError = yearErrorMsg
                        return@Button
                    }
                    if (reportType.isEmpty()) {
                        yearError = "Please select a report type"
                        return@Button
                    }

                    isGeneratingPDF = true
                    pdfErrorMessage = null
                    pdfSuccessMessage = null

                    Thread {
                        try {
                            val pdfFile = when (reportType) {
                                "Reservation Report" -> {
                                    if (sortedMonthlyReservations.isEmpty()) {
                                        throw IllegalStateException("No reservation data available")
                                    }
                                    createReservationPDF(context, sortedMonthlyReservations, selectedYear)
                                }
                                "Sales Report" -> {
                                    if (sortedMonthlyPayments.isEmpty()) {
                                        throw IllegalStateException("No sales data available")
                                    }
                                    createSalesPDF(context, sortedMonthlyPayments, selectedYear)
                                }
                                else -> throw IllegalArgumentException("Invalid report type")
                            }

                            (context as android.app.Activity).runOnUiThread {
                                sharePdf(context, pdfFile, reportType)
                                pdfSuccessMessage = "PDF generated and shared!"
                                isGeneratingPDF = false
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            (context as android.app.Activity).runOnUiThread {
                                pdfErrorMessage = "Failed: ${e.localizedMessage}"
                                isGeneratingPDF = false
                            }
                        }
                    }.start()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && !isGeneratingPDF &&
                        ((reportType == "Reservation Report" && sortedMonthlyReservations.isNotEmpty()) ||
                                (reportType == "Sales Report" && sortedMonthlyPayments.isNotEmpty()))
            ) {
                if (isGeneratingPDF) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Export to PDF")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isGeneratingPDF) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Creating PDF document...",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            "Please wait",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        pdfSuccessMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = message,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose Chrome from share menu → Tap 'Save' → Select Downloads",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        pdfErrorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = Color.Red
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isLoading) {
            if (reportType == "Reservation Report" && sortedMonthlyReservations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Blue
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Reservations for $selectedYear (Sorted Highest to Lowest)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val totalReservations = sortedMonthlyReservations.sumOf { it.reservationCount }
                        val totalGuests = sortedMonthlyReservations.sumOf { it.guestCount }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Reservations: $totalReservations")
                        Text("Total Guests: $totalGuests")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.LightGray)
                                .padding(8.dp)
                        ) {
                            Text(
                                "Rank",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(50.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Month",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                "Reservations",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                "Guests",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End
                            )
                        }

                        sortedMonthlyReservations.forEachIndexed { index, monthData ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.Center,
                                    color = when (index) {
                                        0 -> Color(0xFFD4AF37)
                                        1 -> Color(0xFFC0C0C0)
                                        2 -> Color(0xFFCD7F32)
                                        else -> Color.Black
                                    }
                                )
                                Text(
                                    monthData.monthName,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    monthData.reservationCount.toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = if (monthData.reservationCount > 0) Color.Black else Color.Gray,
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    monthData.guestCount.toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = if (monthData.guestCount > 0) Color.Black else Color.Gray,
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            else if (reportType == "Sales Report" && sortedMonthlyPayments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Sales for $selectedYear (Sorted Highest to Lowest)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val totalSales = sortedMonthlyPayments.sumOf { it.totalAmount }
                        val transactionCount = sortedMonthlyPayments.sumOf { it.transactionCount }
                        val avgTransaction = if (transactionCount > 0) String.format("%.2f", totalSales / transactionCount) else "0.00"

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Sales: RM${String.format("%.2f", totalSales)}")
                        Text("Total Transactions: $transactionCount")
                        Text("Average Transaction: RM$avgTransaction")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.LightGray)
                                .padding(8.dp)
                        ) {
                            Text(
                                "Rank",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(50.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Month",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                "Transactions",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                "Total Amount",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        sortedMonthlyPayments.forEachIndexed { index, monthData ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.Center,
                                    color = when (index) {
                                        0 -> Color(0xFFD4AF37)
                                        1 -> Color(0xFFC0C0C0)
                                        2 -> Color(0xFFCD7F32)
                                        else -> Color.Black
                                    }
                                )
                                Text(
                                    monthData.monthName,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    monthData.transactionCount.toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = if (monthData.transactionCount > 0) Color.Black else Color.Gray,
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    "RM${String.format("%.2f", monthData.totalAmount)}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = if (monthData.totalAmount > 0) Color.Black else Color.Gray,
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Error: $error",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }
        if (isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading data...", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun validateAndGenerate(
    reportViewModel: ReportViewModel,
    yearInput: String,
    currentYear: Int,
    setError: (String?) -> Unit
) {
    val yearErrorMsg = validateYear(yearInput, currentYear)
    if (yearErrorMsg != null) {
        setError(yearErrorMsg)
        return
    }

    if (reportViewModel.reportType.value.isEmpty()) {
        setError("Please select a report type")
        return
    }

    reportViewModel.setYear(yearInput.toInt())
    reportViewModel.fetchDataByYear()
    setError(null)
}

private fun validateYear(yearInput: String, currentYear: Int): String? {
    return when {
        yearInput.isEmpty() -> "Please enter a year"
        yearInput.toIntOrNull() == null -> "Please enter a valid number"
        yearInput.toInt() !in 0..currentYear -> "Year must be between 0 and $currentYear"
        else -> null
    }
}

private fun setYearError(error: String?) {}

@RequiresApi(Build.VERSION_CODES.O)
fun createReservationPDF(
    context: Context,
    monthlyData: List<MonthlyReservationData>,
    year: Int
): File {
    val pdfDocument = PdfDocument()
    val pageWidth = 595f
    val pageHeight = 842f
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    var yPosition = 50f
    val leftMargin = 50f
    val rightMargin = pageWidth - 50f

    paint.textSize = 24f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText("YUM YUM RESTAURANT", leftMargin, yPosition, paint)

    paint.textSize = 18f
    yPosition += 30f
    canvas.drawText("Reservation Report - $year", leftMargin, yPosition, paint)

    paint.isFakeBoldText = false
    yPosition += 40f
    paint.textSize = 12f
    paint.color = AndroidColor.GRAY
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    canvas.drawText("Generated on: ${dateFormat.format(Date())}", leftMargin, yPosition, paint)

    val totalReservations = monthlyData.sumOf { it.reservationCount }
    val totalGuests = monthlyData.sumOf { it.guestCount }

    yPosition += 40f
    paint.textSize = 16f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText("Monthly Performance (Ranked)", leftMargin, yPosition, paint)

    yPosition += 30f
    paint.textSize = 12f
    paint.isFakeBoldText = true
    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.BLACK
    canvas.drawRect(leftMargin, yPosition - 15f, rightMargin, yPosition + 15f, paint)

    paint.color = AndroidColor.WHITE
    val colWidth = (rightMargin - leftMargin - 40f) / 4
    var currentX = leftMargin + 10f

    canvas.drawText("Rank", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Month", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Reservations", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Guests", currentX, yPosition, paint)

    paint.isFakeBoldText = false
    paint.color = AndroidColor.BLACK
    yPosition += 25f

    monthlyData.forEachIndexed { index, monthData ->
        val avg = if (monthData.reservationCount > 0)
            String.format("%.1f", monthData.guestCount.toDouble() / monthData.reservationCount)
        else "0.0"

        paint.style = Paint.Style.FILL
        paint.color = if (index % 2 == 0) AndroidColor.parseColor("#F5F5F5") else AndroidColor.WHITE
        canvas.drawRect(leftMargin, yPosition - 12f, rightMargin, yPosition + 12f, paint)

        paint.style = Paint.Style.FILL
        currentX = leftMargin + 10f

        paint.color = when (index) {
            0 -> AndroidColor.parseColor("#D4AF37")
            1 -> AndroidColor.parseColor("#C0C0C0")
            2 -> AndroidColor.parseColor("#CD7F32")
            else -> AndroidColor.BLACK
        }
        paint.isFakeBoldText = index < 3
        canvas.drawText("#${index + 1}", currentX, yPosition, paint)

        currentX += colWidth
        paint.color = AndroidColor.BLACK
        canvas.drawText(monthData.monthName, currentX, yPosition, paint)

        currentX += colWidth
        canvas.drawText(monthData.reservationCount.toString(), currentX, yPosition, paint)

        currentX += colWidth
        canvas.drawText(monthData.guestCount.toString(), currentX, yPosition, paint)

        paint.isFakeBoldText = false
        yPosition += 25f
    }

    paint.isFakeBoldText = false
    paint.textSize = 14f
    paint.color = AndroidColor.BLACK
    yPosition += 25f
    canvas.drawText("Total Reservations: $totalReservations", leftMargin, yPosition, paint)
    yPosition += 20f
    canvas.drawText("Total Guests: $totalGuests", leftMargin, yPosition, paint)


    yPosition += 20f
    paint.color = AndroidColor.LTGRAY
    paint.strokeWidth = 1f
    paint.style = Paint.Style.STROKE
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)

    yPosition += 20f
    paint.textSize = 10f
    paint.color = AndroidColor.GRAY
    paint.style = Paint.Style.FILL
    canvas.drawText("Yum Yum Restaurant • Confidential Report", leftMargin, yPosition, paint)

    pdfDocument.finishPage(page)

    val cacheDir = context.cacheDir
    val pdfFile = File(cacheDir, "Reservation_Report_$year.pdf")

    FileOutputStream(pdfFile).use { outputStream ->
        pdfDocument.writeTo(outputStream)
    }

    pdfDocument.close()
    return pdfFile
}

@RequiresApi(Build.VERSION_CODES.O)
fun createSalesPDF(
    context: Context,
    monthlyData: List<MonthlyPaymentData>,
    year: Int
): File {
    val pdfDocument = PdfDocument()
    val pageWidth = 595f
    val pageHeight = 842f
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    var yPosition = 50f
    val leftMargin = 50f
    val rightMargin = pageWidth - 50f

    paint.textSize = 24f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText("YUM YUM RESTAURANT", leftMargin, yPosition, paint)

    paint.textSize = 18f
    yPosition += 30f
    canvas.drawText("Sales Report - $year", leftMargin, yPosition, paint)

    paint.isFakeBoldText = false
    yPosition += 40f
    paint.textSize = 12f
    paint.color = AndroidColor.GRAY
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    canvas.drawText("Generated on: ${dateFormat.format(Date())}", leftMargin, yPosition, paint)

    val totalSales = monthlyData.sumOf { it.totalAmount }
    val totalTransactions = monthlyData.sumOf { it.transactionCount }

    yPosition += 40f
    paint.textSize = 16f
    paint.isFakeBoldText = true
    paint.color = AndroidColor.BLACK
    canvas.drawText("Monthly Sales Performance (Ranked)", leftMargin, yPosition, paint)

    yPosition += 30f
    paint.textSize = 12f
    paint.isFakeBoldText = true
    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.parseColor("#388E3C")
    canvas.drawRect(leftMargin, yPosition - 15f, rightMargin, yPosition + 15f, paint)

    paint.color = AndroidColor.WHITE
    val colWidth = (rightMargin - leftMargin - 40f) / 4
    var currentX = leftMargin + 10f

    canvas.drawText("Rank", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Month", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Transactions", currentX, yPosition, paint)
    currentX += colWidth
    canvas.drawText("Total Amount", currentX, yPosition, paint)

    paint.isFakeBoldText = false
    paint.color = AndroidColor.BLACK
    yPosition += 25f

    monthlyData.forEachIndexed { index, monthData ->
        val avg = if (monthData.transactionCount > 0)
            String.format("%.2f", monthData.totalAmount / monthData.transactionCount)
        else "0.00"

        paint.style = Paint.Style.FILL
        paint.color = if (index % 2 == 0) AndroidColor.parseColor("#F5F5F5") else AndroidColor.WHITE
        canvas.drawRect(leftMargin, yPosition - 12f, rightMargin, yPosition + 12f, paint)

        paint.style = Paint.Style.FILL
        currentX = leftMargin + 10f

        paint.color = when (index) {
            0 -> AndroidColor.parseColor("#D4AF37")
            1 -> AndroidColor.parseColor("#C0C0C0")
            2 -> AndroidColor.parseColor("#CD7F32")
            else -> AndroidColor.BLACK
        }
        paint.isFakeBoldText = index < 3
        canvas.drawText("#${index + 1}", currentX, yPosition, paint)

        currentX += colWidth
        paint.color = AndroidColor.BLACK
        canvas.drawText(monthData.monthName, currentX, yPosition, paint)

        currentX += colWidth
        canvas.drawText(monthData.transactionCount.toString(), currentX, yPosition, paint)

        currentX += colWidth
        canvas.drawText("RM${String.format("%,.2f", monthData.totalAmount)}", currentX, yPosition, paint)

        paint.isFakeBoldText = false
        yPosition += 25f
    }

    paint.isFakeBoldText = false
    paint.textSize = 14f
    paint.color = AndroidColor.BLACK
    yPosition += 25f
    canvas.drawText("Total Sales: RM${String.format("%,.2f", totalSales)}", leftMargin, yPosition, paint)
    yPosition += 20f
    canvas.drawText("Total Transactions: $totalTransactions", leftMargin, yPosition, paint)

    yPosition += 20f
    paint.color = AndroidColor.LTGRAY
    paint.strokeWidth = 1f
    paint.style = Paint.Style.STROKE
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)

    yPosition += 20f
    paint.textSize = 10f
    paint.color = AndroidColor.GRAY
    paint.style = Paint.Style.FILL
    canvas.drawText("Yum Yum Restaurant • Confidential Sales Report", leftMargin, yPosition, paint)

    pdfDocument.finishPage(page)

    val cacheDir = context.cacheDir
    val pdfFile = File(cacheDir, "Sales_Report_$year.pdf")

    FileOutputStream(pdfFile).use { outputStream ->
        pdfDocument.writeTo(outputStream)
    }

    pdfDocument.close()
    return pdfFile
}

@SuppressLint("QueryPermissionsNeeded")
fun sharePdf(context: Context, pdfFile: File, reportType: String) {
    try {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
        } else {
            Uri.fromFile(pdfFile)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Yum Yum Restaurant $reportType")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}