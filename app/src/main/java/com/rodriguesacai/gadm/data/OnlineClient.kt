package com.rodriguesacai.gadm.data

data class OnlineClient(
    val id: String,
    val name: String,
    val page: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyM: Double?,
    val locationSource: String,
    val city: String,
    val region: String,
    val country: String,
    val firstSeenAt: String,
    val lastSeenAt: String
)
