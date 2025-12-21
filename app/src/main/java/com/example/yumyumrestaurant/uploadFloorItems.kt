package com.example.yumyumrestaurant



import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.room.PrimaryKey
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

enum class TableStatus { AVAILABLE, OCCUPIED, RESERVED }

// --- 1. FloorItem Classes ---
sealed class FloorItem {
    data class Table(
        val tableId: String = "",
        val label: String = "",
        val zone: ZoneType = ZoneType.INDOOR,
        val xAxis: Float = 0f,
        val yAxis: Float = 0f,
        val radius: Float = 0f,
        val seatCount: Int = 0,
        var status: TableStatus = TableStatus.AVAILABLE,
        var imageUrls: List<String> = emptyList()

    ) : FloorItem()

    data class Region(
        val regionId: String = "",
        val label: String = "Kitchen",
        val zone: ZoneType = ZoneType.INDOOR,
        val xAxis: Float = 0.0f,
        val yAxis: Float = 0.0f,
        val width: Float = 0.0f,
        val height: Float = 0.0f,
        val color: Long = 0xFFA9A9A9 // DarkGray
    ): FloorItem()
}

enum class ZoneType { INDOOR, OUTDOOR }

// --- 2. Convert FloorItem to Map for Firebase ---
fun FloorItem.toMap(): Map<String, Any> {
    return when (this) {
        is FloorItem.Table -> mapOf(
            "type" to "Table",
            "tableId" to tableId,
            "label" to label,
            "zone" to zone.name,
            "xAxis" to xAxis,
            "yAxis" to yAxis,
            "radius" to radius,
            "seatCount" to seatCount,
            "status" to status,
            "imageUrls" to imageUrls
        )
        is FloorItem.Region -> mapOf(
            "type" to "Region",
            "regionId" to regionId,
            "label" to label,
            "zone" to zone.name,
            "xAxis" to xAxis,
            "yAxis" to yAxis,
            "width" to width,
            "height" to height,
            "color" to color
        )
    }
}

// --- 3. Upload FloorItems to Firestore ---
fun uploadFloorItems(floorItems: List<FloorItem>) {
    val db = Firebase.firestore

    floorItems.forEach { item ->

        val collectionName = when (item) {
            is FloorItem.Table -> "Tables"
            is FloorItem.Region -> "Regions"
        }

        val docRef = when (item) {
            is FloorItem.Table -> db.collection(collectionName).document(item.tableId)
            is FloorItem.Region -> db.collection(collectionName).document(item.regionId)
        }

        docRef.set(item.toMap())
            .addOnSuccessListener { println("Uploaded ${item} to $collectionName") }
            .addOnFailureListener { e -> println("Failed to upload ${item}: $e") }
    }
}

// --- 4. Compose Screen Example ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableSelectionScreen() {
    val tableImages = listOf(
        "https://images.pexels.com/photos/2451264/pexels-photo-2451264.jpeg",
        "https://images.pexels.com/photos/827528/pexels-photo-827528.jpeg",
        "https://images.pexels.com/photos/914388/pexels-photo-914388.jpeg",
        "https://images.pexels.com/photos/460537/pexels-photo-460537.jpeg"
    )
    val outdoorImages = listOf(
        "https://images.pexels.com/photos/18252321/pexels-photo-18252321.jpeg",
        "https://images.pexels.com/photos/3201920/pexels-photo-3201920.jpeg",
        "https://images.pexels.com/photos/2956952/pexels-photo-2956952.jpeg"
    )
    val floorItems = remember {
        mutableStateListOf<FloorItem>(
            // --- Outdoor Tables ---
            FloorItem.Table("T0001", "T1", ZoneType.INDOOR, 0.1f, 0.1f, 30f, 6, imageUrls = tableImages),
            FloorItem.Table("T0002", "T2", ZoneType.INDOOR, 0.3f, 0.1f, 30f, 4, imageUrls = tableImages),
            FloorItem.Table("T0003", "T3", ZoneType.INDOOR, 0.1f, 0.25f, 30f, 4, imageUrls = tableImages),
            FloorItem.Table("T0004", "T4", ZoneType.INDOOR, 0.3f, 0.25f, 30f, 4, imageUrls = tableImages),
            FloorItem.Table("T0005", "T5", ZoneType.INDOOR, 0.1f, 0.5f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0006", "T6", ZoneType.INDOOR, 0.3f, 0.5f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0007", "T7", ZoneType.INDOOR, 0.1f, 0.6f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0008", "T8", ZoneType.INDOOR, 0.3f, 0.6f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0009", "T9", ZoneType.INDOOR, 0.1f, 0.7f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0010", "T10", ZoneType.INDOOR, 0.3f, 0.7f, 25f, 2, imageUrls = tableImages),
            FloorItem.Table("T0011", "T11", ZoneType.INDOOR, 0.1f, 0.85f, 25f, 6, imageUrls = tableImages),
            FloorItem.Table("T0012", "T12", ZoneType.INDOOR, 0.3f, 0.85f, 25f, 3, imageUrls = tableImages),
            FloorItem.Table("T0013", "T13", ZoneType.INDOOR, 0.5f, 0.85f, 25f, 3, imageUrls = tableImages),
            FloorItem.Table("T0014", "T14", ZoneType.INDOOR, 0.65f, 0.2f, 25f, 6, imageUrls = tableImages),



            // ------------------- OUTDOOR TABLES -------------------
            FloorItem.Table("T0015", "T15", ZoneType.OUTDOOR, 0.5f, 0.1f, 30f, 2, imageUrls = outdoorImages),
            FloorItem.Table("T0016", "T16", ZoneType.OUTDOOR, 0.75f, 0.1f, 30f, 2, imageUrls = outdoorImages),
            FloorItem.Table("T0017", "T17", ZoneType.OUTDOOR, 0.5f, 0.25f, 30f, 4, imageUrls = outdoorImages),
            FloorItem.Table("T0018", "T18", ZoneType.OUTDOOR, 0.75f, 0.25f, 30f, 4, imageUrls = outdoorImages),
            FloorItem.Table("T0019", "T19", ZoneType.OUTDOOR, 0.5f, 0.4f, 30f, 6, imageUrls = outdoorImages),
            FloorItem.Table("T0020", "T20", ZoneType.OUTDOOR, 0.75f, 0.4f, 30f, 6, imageUrls = outdoorImages),
            FloorItem.Table("T0021", "T21", ZoneType.OUTDOOR, 0.5f, 0.6f, 25f, 2, imageUrls = outdoorImages),
            FloorItem.Table("T0022", "T22", ZoneType.OUTDOOR, 0.75f, 0.6f, 25f, 2, imageUrls = outdoorImages),
            FloorItem.Table("T0023", "T23", ZoneType.OUTDOOR, 0.5f, 0.75f, 25f, 4, imageUrls = outdoorImages),
            FloorItem.Table("T0024", "T24", ZoneType.OUTDOOR, 0.75f, 0.75f, 25f, 4, imageUrls = outdoorImages),
            FloorItem.Table("T0025", "T25", ZoneType.OUTDOOR, 0.5f, 0.9f, 25f, 6, imageUrls = outdoorImages),
            FloorItem.Table("T0026", "T26", ZoneType.OUTDOOR, 0.75f, 0.9f, 25f, 6, imageUrls = outdoorImages),




            FloorItem.Region("RG0001", "Serving", ZoneType.INDOOR, 0.45f, 0.3f, 0.15f, 0.4f, color = 0xFFD3D3D3),
            FloorItem.Region("RG0002", "Kitchen", ZoneType.INDOOR, 0.65f, 0.3f, 0.3f, 0.5f, color = 0xFF555555),
            FloorItem.Region("RG0003", "Cooler", ZoneType.INDOOR, 0.85f, 0.05f, 0.1f, 0.15f, color = 0xFF00BCD4),

            FloorItem.Region("RG0004", "Patio\nDoor", ZoneType.INDOOR, 0.01f, 0.35f, 0.1f, 0.2f, color = 0xFF795548),

            FloorItem.Region("RG0005", "Toilet Male", ZoneType.INDOOR, 0.65f, 0.85f, 0.3f, 0.05f, color = 0xFF03A9F4),
            FloorItem.Region("RG0006", "Toilet Female", ZoneType.INDOOR, 0.65f, 0.91f, 0.3f, 0.05f, color = 0xFFE91E63),

            FloorItem.Region("RG0007", "Window", ZoneType.INDOOR, 0.5f, 0.02f, 0.2f, 0.05f, color = 0xFFB3E5FC),
            FloorItem.Region("RG0008", "Window", ZoneType.INDOOR, 0.15f, 0.95f, 0.2f, 0.05f, color = 0xFFB3E5FC),

// ------------------- OUTDOOR REGIONS -------------------
            FloorItem.Region("RG0009", "Fountain", ZoneType.OUTDOOR, 0.01f, 0.05f, 0.4f, 0.2f, color = 0xFF64B5F6),
            FloorItem.Region("RG0010", "Plants", ZoneType.OUTDOOR, 0.01f, 0.7f, 0.4f, 0.2f, color = 0xFF4CAF50),

            FloorItem.Region("RG0011", "Entrance\nDoor", ZoneType.OUTDOOR, 0.01f, 0.35f, 0.2f, 0.2f, color = 0xFF795548),
            FloorItem.Region("RG0012", "Patio\nDoor", ZoneType.OUTDOOR, 0.9f, 0.35f, 0.1f, 0.2f, color = 0xFF795548),

            FloorItem.Region("RG0013", "Fan", ZoneType.OUTDOOR, 0.9f, 0.13f, 0.1f, 0.1f, color = 0xFFB3E5FC),
            FloorItem.Region("RG0014", "Fan", ZoneType.OUTDOOR, 0.9f, 0.77f, 0.1f, 0.1f, color = 0xFFB3E5FC)

        )



    }

    // --- Upload Data Once on First Compose ---
    LaunchedEffect(Unit) {
        uploadFloorItems(floorItems)
    }

    // TODO: Add your TableSelection UI drawing here
}
