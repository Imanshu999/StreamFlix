package com.example.data.model

data class UserDeviceTelemetry(
    val deviceId: String,
    val deviceModel: String,
    val osVersion: String,
    val ipAddress: String,
    val firstLaunchTimestamp: Long,
    val lastActiveTimestamp: Long,
    val isBanned: Boolean = false,
    val banReason: String? = null
)

data class BanRecord(
    val targetKey: String, // Can be deviceId or ipAddress
    val targetType: String, // "DEVICE" or "IP"
    val bannedAt: Long,
    val reason: String
)
