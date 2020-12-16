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

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.reactivex.Single
import io.reactivex.SingleEmitter
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.mustakimov.jsonrpc2.exception.*
import ru.mustakimov.jsonrpc2.internal.JsonRpcRequest
import ru.mustakimov.jsonrpc2.internal.JsonRpcResponse
import java.lang.reflect.Method
import kotlin.random.Random

class ServiceMethod<T> private constructor(
    private val methodName: String,
    private val namedParams: Boolean,
    private val retrograd: Retrograd,
    private val method: Method
) {

    fun invoke(args: Array<out Any?>, url: HttpUrl): T? {
        val request: JsonRpcRequest = if (namedParams) {
            JsonRpcRequest.JsonRpcNamedRequest(Random.nextLong(), methodName, method.jsonRpcNamedParameters(args))
        } else {
            JsonRpcRequest.JsonRpcUnnamedRequest(Random.nextLong(), methodName, method.jsonRpcUnnamedParameters(args))
        }
        retrograd.interceptors.filterIsInstance<RequestInterceptor>().forEach {
            it.handleRequest(request, method.annotations)
        }
        for (interceptor in retrograd.interceptors) {
            when (interceptor) {
                is RequestInterceptor -> interceptor.handleRequest(request, method.annotations)
            }
        }
        val payload = retrograd.gson.toJson(request)
        val req = HttpRequestBuilder {
            takeFrom(url.toUri())
        }.apply {
            method = HttpMethod.Post
            body = OutgoingContent.
        }
            .post(payload.toRequestBody("application/json".toMediaType()))
            .url(url)
            .build()
        @Suppress("UNCHECKED_CAST")
        return Single.create { emitter: SingleEmitter<Any> ->
            val call = retrograd.callFactory.newCall(req)
            emitter.setCancellable(call::cancel)
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    if (!emitter.isDisposed)
                        emitter.onError(Exception(response.message))
                } else {
                    val responseType = method.resultGenericTypeArgument
                    val serverResponse = retrograd.gson.fromJson(
                        response.body?.charStream(),
                        JsonRpcResponse::class.java
                    )
                    val error = serverResponse.error
                    if (error != null) {
                        if (!emitter.isDisposed)
                            emitter.onError(
                                when (error.code) {
                                    -32700 -> ParseError(error.message)
                                    -32600 -> InvalidRequestError(error.message)
                                    -32601 -> MethodNotFoundError(error.message)
                                    -32602 -> InvalidParamsError(error.message)
                                    -32603 -> InternalError(error.message)
                                    in -32000 downTo -32099 -> ServerError(error.code, error.message)
                                    else -> JsonRpcError(error.code, error.message)
                                }
                            )
                    } else {
                        val result =
                            retrograd.gson.fromJson<Any>(serverResponse.result, responseType)
                        if (!emitter.isDisposed)
                            if (result != null)
                                emitter.onSuccess(result)
                            else
                                emitter.onError(EmptyResponseException())
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
            val annotation = method.getAnnotation(JsonRpcMethod::class.java)
            val methodName = annotation?.value ?: method.name
            val named = annotation.namedParams
            return ServiceMethod(methodName, named, retrograd, method)
        }
    }
}