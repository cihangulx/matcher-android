package com.flort.evlilik.models.gift

import com.google.gson.annotations.SerializedName

data class Gift(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("active")
    val active: Boolean? = null,
    
    @SerializedName("desc")
    val desc: String? = null,
    
    @SerializedName("coin")
    val coin: Int? = null,
    
    @SerializedName("vip")
    val vip: Boolean? = null,
    
    @SerializedName("usageCount")
    val usageCount: Int? = null,
    
    @SerializedName("totalCoinsSpent")
    val totalCoinsSpent: Int? = null,
    
    @SerializedName("image")
    val image: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)
