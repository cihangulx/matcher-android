package com.flort.evlilik.network.service

import com.flort.evlilik.models.message.*
import com.flort.evlilik.models.message.response.*
import com.flort.evlilik.models.message.request.*
import com.flort.evlilik.network.Routes
import retrofit2.http.*

interface MessageService {
    
    // Konuşmaları listele
    @GET(Routes.GET_CONVERSATIONS)
    suspend fun getConversations(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ConversationsResponse
    
    // Belirli konuşmanın mesajlarını getir
    @GET(Routes.GET_MESSAGES)
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): MessagesResponse
    
    // Konuşma başlat
    @POST(Routes.START_CONVERSATION)
    suspend fun startConversation(
        @Body request: StartConversationRequest
    ): ConversationResponse
    
    // Konuşmayı sil
    @DELETE(Routes.DELETE_CONVERSATION)
    suspend fun deleteConversation(
        @Path("conversationId") conversationId: String
    ): ApiDeleteResponse
    
    // Mesajı sil
    @DELETE(Routes.DELETE_MESSAGE)
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    ): ApiDeleteResponse
    
    // Okunmamış mesaj sayısı
    @GET(Routes.GET_UNREAD_COUNT)
    suspend fun getUnreadCount(): UnreadCountResponse
    
    // Mesaj maliyetlerini getir (public)
    @GET(Routes.GET_MESSAGE_COSTS)
    suspend fun getMessageCosts(): MessageCostsResponse
    
    // Jeton bakiyesi
    @GET(Routes.GET_WALLET)
    suspend fun getWallet(): WalletResponse
    
    // Mesaj gönderme izni kontrolü
    @GET(Routes.CHECK_MESSAGE_PERMISSION)
    suspend fun checkMessagePermission(
        @Query("messageType") messageType: String = "text"
    ): MessagePermissionResponse
}
