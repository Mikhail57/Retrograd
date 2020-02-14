/*
 * Copyright 2020 Mikhail Mustakimov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.mustakimov.jsonrpc2

import io.reactivex.Single
import io.reactivex.SingleEmitter
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.mustakimov.jsonrpc2.exception.BusinessException
import ru.mustakimov.jsonrpc2.exception.SystemException
import ru.mustakimov.jsonrpc2.internal.JsonRpcRequest
import ru.mustakimov.jsonrpc2.internal.JsonRpcResponse
import java.lang.reflect.Method
import kotlin.random.Random

class ServiceMethod<T>(
    val methodName: String,
    val retrograd: Retrograd,
    val method: Method
) {

    fun invoke(args: Array<out Any?>, url: HttpUrl): T? {
        val params = method.jsonRpcParameters(args)
        val request = JsonRpcRequest(Random.nextLong(), methodName, params)
        retrograd.interceptors.filterIsInstance<RequestInterceptor>().forEach {
            it.handleRequest(request, method.annotations)
        }
        for (interceptor in retrograd.interceptors) {
            when (interceptor) {
                is RequestInterceptor -> interceptor.handleRequest(request, method.annotations)
            }
        }
        val payload = retrograd.gson.toJson(request)
        val req = Request.Builder()
            .post(payload.toRequestBody("application/json".toMediaType()))
            .url(url)
            .build()
        @Suppress("UNCHECKED_CAST")
        return Single.create<Any> { emitter: SingleEmitter<Any> ->
            val call = retrograd.callFactory.newCall(req)
            emitter.setCancellable(call::cancel)
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    if (!emitter.isDisposed)
                        emitter.onError(Exception(response.message))
                } else {
                    val responseType = method.resultGenericTypeArgument
                    val serverResponse = retrograd.gson.fromJson<JsonRpcResponse>(
                        response.body?.charStream(),
                        JsonRpcResponse::class.java
                    )
                    if (serverResponse?.error?.code != null) {
                        if (serverResponse.error.code > 0) {
                            if (!emitter.isDisposed)
                                emitter.onError(BusinessException(request, serverResponse.error))
                        } else {
                            if (!emitter.isDisposed)
                                emitter.onError(SystemException(request, serverResponse.error))
                        }
                    } else {
                        val result =
                            retrograd.gson.fromJson<Any>(serverResponse.result, responseType)
                        if (!emitter.isDisposed)
                            emitter.onSuccess(result)
                    }
                }
            } catch (t: Throwable) {
                if (!emitter.isDisposed)
                    emitter.onError(t)
            }
        } as T
    }

    companion object {
        fun <T> parseAnnotations(retrograd: Retrograd, method: Method): ServiceMethod<T> {
            if (!method.returnsSingle) {
                throw IllegalArgumentException("Only io.reactivex.Single<T> is supported as return type")
            }
            val methodName = method.getAnnotation(JsonRpcMethod::class.java)?.value ?: method.name
            return ServiceMethod(methodName, retrograd, method)
        }
    }
}