package com.krishiai.app.core.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class LocationNode(
    val id: String,
    val name: String,
    val type: LocationType,
    val parentId: String? = null,
    val children: MutableList<LocationNode> = mutableListOf()
)

enum class LocationType {
    STATE, DISTRICT, TALUK, HOBLI, VILLAGE, APMC
}

data class LocationSearchResult(
    val node: LocationNode,
    val breadcrumb: String, // e.g., "Taluk, District, State"
    val matchScore: Int // Lower is better (0 = exact match)
)

@Singleton
class LocationSearchEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationNodes = mutableListOf<LocationNode>()
    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return
        withContext(Dispatchers.IO) {
            try {
                // Initialize State
                val karnataka = LocationNode("KA", "Karnataka", LocationType.STATE)
                locationNodes.add(karnataka)

                // Read CSV
                val inputStream = context.assets.open("Karnataka_31_Districts_and_Taluks.csv")
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                // Skip header
                reader.readLine() 
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 2) {
                        val districtName = tokens[0].trim()
                        val talukName = tokens[1].trim()

                        // Find or create district
                        var district = karnataka.children.find { it.name.equals(districtName, ignoreCase = true) }
                        if (district == null) {
                            district = LocationNode("DIST_${districtName}", districtName, LocationType.DISTRICT, karnataka.id)
                            karnataka.children.add(district)
                            locationNodes.add(district)
                        }

                        // Find or create taluk
                        var taluk = district.children.find { it.name.equals(talukName, ignoreCase = true) }
                        if (taluk == null) {
                            taluk = LocationNode("TALUK_${districtName}_${talukName}", talukName, LocationType.TALUK, district.id)
                            district.children.add(taluk)
                            locationNodes.add(taluk)
                        }
                    }
                }
                reader.close()
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun searchLocations(query: String, filterType: LocationType? = null, parentId: String? = null): List<LocationSearchResult> {
        if (!isInitialized) initialize()
        
        return withContext(Dispatchers.Default) {
            val q = query.lowercase().trim()
            val results = mutableListOf<LocationSearchResult>()
            
            for (node in locationNodes) {
                // Apply filters
                if (filterType != null && node.type != filterType) continue
                if (parentId != null && node.parentId != parentId) continue
                
                val nodeName = node.name.lowercase()
                var matchScore = -1
                
                if (q.isEmpty()) {
                    matchScore = 100 // Return all if query is empty
                } else if (nodeName == q) {
                    matchScore = 0 // Exact match
                } else if (nodeName.startsWith(q)) {
                    matchScore = 1 // Prefix match
                } else if (nodeName.contains(q)) {
                    matchScore = 2 // Substring match
                } else if (isFuzzyMatch(q, nodeName)) {
                    matchScore = 3 // Typo match
                }

                if (matchScore >= 0) {
                    val breadcrumb = buildBreadcrumb(node)
                    results.add(LocationSearchResult(node, breadcrumb, matchScore))
                }
            }
            
            results.sortedBy { it.matchScore }.take(50)
        }
    }

    private fun isFuzzyMatch(query: String, target: String): Boolean {
        if (query.length < 3) return false // Too short for fuzzy
        
        // Simple subsequence matching for typo tolerance
        var i = 0
        var j = 0
        while (i < query.length && j < target.length) {
            if (query[i] == target[j]) {
                i++
            }
            j++
        }
        return i == query.length
    }

    private fun buildBreadcrumb(node: LocationNode): String {
        var current: LocationNode? = node
        val parts = mutableListOf<String>()
        while (current != null) {
            parts.add(current.name)
            current = locationNodes.find { it.id == current!!.parentId }
        }
        return parts.joinToString(", ")
    }
    
    fun getDistricts(): List<LocationNode> {
        return locationNodes.filter { it.type == LocationType.DISTRICT }.sortedBy { it.name }
    }
    
    fun getTaluks(districtId: String): List<LocationNode> {
        return locationNodes.filter { it.type == LocationType.TALUK && it.parentId == districtId }.sortedBy { it.name }
    }
}
