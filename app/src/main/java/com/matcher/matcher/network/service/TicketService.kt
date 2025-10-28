package com.matcher.matcher.network.service

import com.matcher.matcher.network.Routes
import com.matcher.matcher.models.ticket.TicketRequest
import com.matcher.matcher.models.ticket.RemoveMessage
import com.matcher.matcher.models.ticket.StartMessage
import com.matcher.matcher.models.ticket.ReportReason
import com.matcher.matcher.models.ticket.request.ReportRequest
import com.matcher.matcher.models.ticket.response.ReportResponse
import com.matcher.matcher.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TicketService {
    @POST(Routes.Companion.SEND_TICKET)
    suspend fun sendTicket(@Body request: TicketRequest): ApiResponse<Unit>
    
    @GET(Routes.Companion.REMOVE_MESSAGES)
    suspend fun getRemoveMessages(): ApiResponse<List<RemoveMessage>>
    
    @GET(Routes.Companion.START_MESSAGES)
    suspend fun getStartMessages(): ApiResponse<List<StartMessage>>
    
    @GET(Routes.Companion.REPORT_REASONS)
    suspend fun getReportReasons(): ApiResponse<List<ReportReason>>
    
    @POST(Routes.Companion.SEND_REPORT)
    suspend fun sendReport(@Body request: ReportRequest): ApiResponse<ReportResponse>
}