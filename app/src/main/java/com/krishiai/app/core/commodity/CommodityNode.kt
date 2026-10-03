package com.krishiai.app.core.commodity

data class CommodityNode(
    val id: String,
    val name: String,
    val category: String
)

data class CommoditySearchResult(
    val node: CommodityNode,
    val matchScore: Int // Lower is better (0 = exact match)
)
