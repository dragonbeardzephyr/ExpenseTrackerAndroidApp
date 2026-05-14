package com.example.expensetracker.data


data class LinkTokenUser(
    val client_user_id: String,
    val legal_name: String,
    val phone_number: String,
    val email_address: String
)


data class LinkTokenRequest(
    val client_id: String,
    val secret: String,
    val user: LinkTokenUser,
    val client_name: String,
    val products: List<String>,
    val country_codes: List<String>,
    val language: String,
    val android_package_name: String
)

data class LinkTokenResponse(
    val link_token: String
)

