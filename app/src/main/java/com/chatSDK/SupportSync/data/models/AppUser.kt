package com.chatSDK.SupportSync.data.models

data class AppUser(
    val id: Long? = null,
    var username: String,
    var role: UserRole
)
enum class UserRole {
    CUSTOMER,
    AGENT
}
