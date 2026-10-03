package com.krishiai.app.utils

object KarnatakaDataHelper {
    private val districtTalukMap = mapOf(
        "Bengaluru Urban" to listOf("Bengaluru North", "Bengaluru South", "Bengaluru East", "Anekal"),
        "Bengaluru Rural" to listOf("Devanahalli", "Doddaballapura", "Hosakote", "Nelamangala"),
        "Belagavi" to listOf("Belagavi", "Athani", "Bailhongal", "Chikkodi", "Gokak", "Hukkeri", "Khanapur", "Raybag", "Ramdurg", "Saundatti"),
        "Mysuru" to listOf("Mysuru", "Hunsur", "H.D. Kote", "K.R. Nagar", "Nanjangud", "Piriyapatna", "T. Narasipura"),
        "Dharwad" to listOf("Dharwad", "Hubli", "Kalghatgi", "Kundgol", "Navalgund"),
        "Mandya" to listOf("Mandya", "Maddur", "Malavalli", "Pandavapura", "Srirangapatna", "Krishnarajapet", "Nagamangala"),
        "Shivamogga" to listOf("Shivamogga", "Bhadravathi", "Hosanagara", "Sagar", "Shikaripura", "Soraba", "Thirthahalli"),
        "Chikmagalur" to listOf("Chikmagalur", "Kadur", "Koppa", "Mudigere", "Narasimharajapura", "Sringeri", "Tarikere"),
        "Tumakuru" to listOf("Tumakuru", "Chiknayakanhalli", "Gubbi", "Koratagere", "Kunigal", "Madhugiri", "Pavagada", "Sira", "Tiptur", "Turuvekere"),
        "Davangere" to listOf("Davangere", "Harihar", "Channagiri", "Honnali", "Jagalur"),
        "Bagalkot" to listOf("Bagalkot", "Badami", "Bilgi", "Hunagund", "Jamkhandi", "Mudhol"),
        "Vijayapura" to listOf("Vijayapura", "Indi", "Muddebihal", "Sindgi", "Basavana Bagewadi")
    )

    fun getDistricts(): List<String> {
        return districtTalukMap.keys.sorted()
    }

    fun getTaluksForDistrict(district: String): List<String> {
        return districtTalukMap[district] ?: emptyList()
    }
}
