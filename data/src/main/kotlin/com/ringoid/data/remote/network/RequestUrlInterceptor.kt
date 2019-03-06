package com.ringoid.data.remote.network

import com.ringoid.domain.log.SentryUtil
import okhttp3.Interceptor
import okhttp3.Response

class RequestUrlInterceptor : IRequestUrlInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        SentryUtil.breadcrumb("Request", "url" to "${request.url()}")
        return chain.proceed(request)
    }
}
