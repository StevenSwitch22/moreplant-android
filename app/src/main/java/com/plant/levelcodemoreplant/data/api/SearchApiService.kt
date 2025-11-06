package com.plant.levelcodemoreplant.data.api

import com.plant.levelcodemoreplant.data.model.SearchRequest
import com.plant.levelcodemoreplant.data.model.SearchResponse
import com.plant.levelcodemoreplant.data.model.SuggestionsRequest
import com.plant.levelcodemoreplant.data.model.SuggestionsResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 查询兑换码 API 接口
 */
interface SearchApiService {
    
    @POST("/api/search")
    suspend fun searchCode(@Body request: SearchRequest): SearchResponse
    
    @POST("/api/search/suggestions")
    suspend fun getSuggestions(@Body request: SuggestionsRequest): SuggestionsResponse
}
