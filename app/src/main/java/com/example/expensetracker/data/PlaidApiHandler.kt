package com.example.expensetracker.data

import retrofit2.http.Body
import retrofit2.http.POST

interface PlaidApiHandler {
    @POST("link/token/create")
    suspend fun createLinkToken(@Body request: LinkTokenRequest): LinkTokenResponse

    // Add this new endpoint
    @POST("item/public_token/exchange")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): TokenExchangeResponse


    @POST("accounts/get")
    suspend fun getAccounts(@Body request: AccountsGetRequest): AccountsGetResponse

    @POST("transactions/sync")
    suspend fun syncTransactions(@Body request: TransactionsSyncRequest): TransactionsSyncResponse


    @POST("item/get")
    suspend fun getItemDetails(@Body request: ItemGetRequest): ItemGetResponse

    @POST("institutions/get_by_id")
    suspend fun getInstitutionById(@Body request: InstitutionGetByIdRequest): InstitutionGetByIdResponse




}