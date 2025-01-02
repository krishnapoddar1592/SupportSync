package com.chatSDK.SupportSync.data.models

data class SessionRequest(
    val user:AppUser,
    val category: IssueCategory?
) {
}