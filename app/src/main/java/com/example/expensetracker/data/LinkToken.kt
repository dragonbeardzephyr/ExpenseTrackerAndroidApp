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


data class TokenExchangeRequest(
    val client_id: String,
    val secret: String,
    val public_token: String
)

data class TokenExchangeResponse(val access_token: String, val item_id: String)


data class AccountsGetRequest(
    val client_id: String,
    val secret: String,
    val access_token: String

)

data class PlaidAccount(
    val account_id: String,
    val name: String,
    val mask: String?,
    val type: String,
    val balances: PlaidBalances
)

data class PlaidBalances(
    val available: Double?,
    val current: Double,
    val iso_currency_code: String?
)

data class AccountsGetResponse(
    val accounts: List<PlaidAccount>
)



data class TransactionsSyncRequest(
    val client_id: String,
    val secret: String,
    val access_token: String,
    val cursor: String? = null,
    val count: Int = 100
)

data class PlaidTransaction(
    val transaction_id: String,
    val account_id: String,
    val amount: Double,
    val payment_meta: PlaidPaymentMeta?,
    val merchant_name: String?,
    val name: String,
    val date: String, // YYYY-MM-DD
    val pending: Boolean,
    val personal_finance_category: PlaidFinanceCategory?
)

data class PlaidPaymentMeta(
    val reference_number: String?
)

data class PlaidFinanceCategory(
    val primary: String,
    val detailed: String
)

data class TransactionsSyncResponse(
    val added: List<PlaidTransaction>,
    val modified: List<PlaidTransaction>,
    val removed: List<PlaidRemovedTransaction>,
    val next_cursor: String,
    val has_more: Boolean
)

data class PlaidRemovedTransaction(
    val transaction_id: String
)


data class ItemGetRequest(
    val client_id: String,
    val secret: String,
    val access_token: String
)

data class ItemGetResponse(
    val item: PlaidItemInfo
)

data class PlaidItemInfo(
    val item_id: String,
    val institution_id: String?
)

data class InstitutionGetByIdRequest(
    val client_id: String,
    val secret: String,
    val institution_id: String,
    val country_codes: List<String>
)

data class InstitutionGetByIdResponse(
    val institution: PlaidInstitutionDetails
)

data class PlaidInstitutionDetails(
    val name: String
)