package com.krishiai.app.core.commodity

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommoditySearchEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val commodities = mutableListOf<CommodityNode>()
    private val categoryMap = mutableMapOf<String, MutableList<CommodityNode>>()
    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("KrishiAI_Master_Commodity_List_1000_Items.csv")
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                // Skip header
                reader.readLine() 
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // ID,Category,Commodity
                    // Example: 1,Vegetable,Tomato
                    val tokens = line!!.split(",")
                    if (tokens.size >= 3) {
                        val id = tokens[0].trim()
                        val category = tokens[1].trim()
                        val name = tokens[2].trim()

                        val node = CommodityNode(id, name, category)
                        commodities.add(node)
                        
                        if (!categoryMap.containsKey(category)) {
                            categoryMap[category] = mutableListOf()
                        }
                        categoryMap[category]?.add(node)
                    }
                }
                reader.close()
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun searchCommodities(query: String, categoryFilter: String? = null): List<CommoditySearchResult> {
        if (!isInitialized) initialize()
        
        return withContext(Dispatchers.Default) {
            val q = query.lowercase().trim()
            val results = mutableListOf<CommoditySearchResult>()
            
            val listToSearch = if (categoryFilter != null && categoryMap.containsKey(categoryFilter)) {
                categoryMap[categoryFilter]!!
            } else {
                commodities
            }
            
            for (node in listToSearch) {
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
                    results.add(CommoditySearchResult(node, matchScore))
                }
            }
            
            results.sortedBy { it.matchScore }.take(50)
        }
    }
    
    fun getCategories(): List<String> {
        return categoryMap.keys.sorted()
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
}
