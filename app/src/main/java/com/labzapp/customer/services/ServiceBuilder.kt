package com.labzapp.customer.services

import android.os.Build
import com.labzapp.customer.utilities.API_BASEURL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object ServiceBuilder {


    private const val URL = API_BASEURL

    // Create Logger
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    // Create a Custom Interceptor to apply Headers application wide
    val headerInterceptor = object: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()

            request = request.newBuilder()
                    .addHeader("x-device-type", Build.DEVICE)
                    .addHeader("Accept-Language", Locale.getDefault().language)
                    .build()

            val response = chain.proceed(request)
            return response
        }
    }

    // Create OkHttp Client
    private val okHttp = OkHttpClient.Builder()
            .callTimeout(0, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(logger)

    // Create Retrofit Builder
    private val builder = Retrofit.Builder().baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp.build())

    // Create Retrofit Instance
    private val retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }
}