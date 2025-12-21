//import android.content.Context
//import android.graphics.Paint
//import android.graphics.pdf.PdfDocument
//import android.os.Environment
//import com.example.yumyumrestaurant.Reservation
//import java.io.File
//import java.io.FileOutputStream
//import java.time.Month
//
//fun createYearlyReportPdf(
//    context: Context,
//    reservations: List<Reservation>,
//    year: Int
//): File {
//
//    // 1. Filter data by year
//    val yearlyData = reservations.filter {
//        it.date.year == year
//    }
//
//    // 2. Calculations
//    val totalReservations = yearlyData.size
//    val totalSales = yearlyData.sumOf { it.amount }
//
//    val monthlySales = yearlyData.groupBy { it.date.month }
//        .mapValues { entry ->
//            entry.value.sumOf { it.amount }
//        }
//
//    // 3. Create PDF
//    val pdfDocument = PdfDocument()
//    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//    val page = pdfDocument.startPage(pageInfo)
//    val canvas = page.canvas
//
//    val paint = Paint()
//    paint.textSize = 14f
//
//    // Title
//    paint.textSize = 18f
//    canvas.drawText(
//        "Yearly Reservation and Sales Report - $year",
//        50f,
//        50f,
//        paint
//    )
//
//    // Summary
//    paint.textSize = 14f
//    canvas.drawText("Total Reservations: $totalReservations", 50f, 100f, paint)
//    canvas.drawText("Total Sales: $${"%.2f".format(totalSales)}", 50f, 130f, paint)
//
//    // Monthly Section
//    var y = 180f
//    canvas.drawText("Monthly Summary", 50f, y, paint)
//    y += 30f
//
//    Month.values().forEach { month ->
//        val amount = monthlySales[month] ?: 0.0
//        canvas.drawText(
//            "${month.name.lowercase().replaceFirstChar { it.uppercase() }} : $${"%.2f".format(amount)}",
//            50f,
//            y,
//            paint
//        )
//        y += 25f
//    }
//
//    pdfDocument.finishPage(page)
//
//    // 4. Save PDF
//    val downloadsDir =
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//
//    val file = File(downloadsDir, "Yearly_Report_$year.pdf")
//
//    pdfDocument.writeTo(FileOutputStream(file))
//    pdfDocument.close()
//
//
//    return file
//}
