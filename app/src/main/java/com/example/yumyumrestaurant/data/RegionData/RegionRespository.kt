package com.example.yumyumrestaurant.data.RegionData

import com.example.yumyumrestaurant.data.TableData.TableDao
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await


class RegionRepository(private val regionDao: RegionDao) {

    private val firestore = FirebaseFirestore.getInstance()

    val allRegions: Flow<List<RegionEntity>> = regionDao.getAllRegions()

    // Insert locally and in Firestore
    suspend fun insertRegions(region: RegionEntity) {
        regionDao.insertRegion(region)
        firestore.collection("Regions").document(region.regionId).set(region).await()
    }

    // Fetch all from Firestore and save to Room
    suspend fun syncRegionsFromFirebase() {
        val snapshot = firestore.collection("Regions").get().await()
        println("Fetched ${snapshot.size()} documents from Firestore")

        val regions = snapshot.documents.mapNotNull {
            val region = it.toObject(RegionEntity::class.java)
            println("Doc ${it.id} → $region")
            region?.copy(regionId = it.id)
        }

        println("Mapped ${regions.size} regions")
        regionDao.clearAllRegion()
        regions.forEach { region ->
            regionDao.insertRegion(region)
        }
    }


}