package com.labzapp.customer.models
import kotlinx.serialization.Serializable

@Serializable
data class BookingDataTransfer(var patbookfor: String,var patlatitude: String,var patlongitude: String,var patprefdate: String,var pattests: MutableList<String>,var patlab: ArrayList<Labswithtest>)
