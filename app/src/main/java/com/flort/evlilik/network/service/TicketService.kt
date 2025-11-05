package com.flort.evlilik.network.service

import com.flort.evlilik.network.Routes
import com.flort.evlilik.models.ticket.TicketRequest
import com.flort.evlilik.models.ticket.RemoveMessage
import com.flort.evlilik.models.ticket.StartMessage
import com.flort.evlilik.models.ticket.ReportReason
import com.flort.evlilik.models.ticket.request.ReportRequest
import com.flort.evlilik.models.ticket.response.ReportResponse
import com.flort.evlilik.network.model.ApiResponse
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