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

import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.mustakimov.jsonrpc2.internal.JsonRpcRequest
import java.net.URL

internal class RetrogradBuilderTest {
    @Test
    fun `Should throw an exception without baseUrl`() {
        assertThrows(IllegalStateException::class.java) {
            Retrograd.Builder().build()
        }
    }

    @Test
    fun `Should create retrograd object with correct base url builder`() {
        val retrograd = Retrograd.Builder().baseUrl(BASE_URL).build()
        retrograd.baseUrl.toString() `should be equal to` BASE_URL
        retrograd.interceptors.isEmpty() `should be` true
    }

    @Test
    fun `Should throw an exception with invalid base url`() {
        assertThrows(IllegalArgumentException::class.java) {
            Retrograd.Builder().baseUrl(BASE_URL_INCORRECT).build()
        }
    }

    @Test
    fun `Should create retrograd with URL as baseUrl`() {
        val retrograd = Retrograd.Builder().baseUrl(URL(BASE_URL)).build()
        retrograd.baseUrl.toString() `should be equal to` BASE_URL
    }

    @Test
    fun `Should create retrograd with HttpUrl as baseUrl`() {
        val retrograd = Retrograd.Builder().baseUrl(BASE_URL.toHttpUrl()).build()
        retrograd.baseUrl.toString() `should be equal to` BASE_URL
    }

    @Test
    fun `Should use provided Gson`() {
        val gson = Gson()
        val retrograd = Retrograd.Builder().baseUrl(BASE_URL).gson(gson).build()
        retrograd.gson `should be` gson
    }

    @Test
    fun `Should use provided OkHttpClient`() {
        val okHttpClient = OkHttpClient()
        val retrograd = Retrograd.Builder().baseUrl(BASE_URL).client(okHttpClient).build()
        retrograd.callFactory `should be` okHttpClient
    }

    @Test
    fun `Should use provided interceptors`() {
        val interceptor = object : RequestInterceptor {
            override fun handleRequest(request: JsonRpcRequest, annotations: Array<Annotation>) {}
        }
        val retrograd = Retrograd.Builder().baseUrl(BASE_URL).addInterceptor(interceptor).build()
        retrograd.interceptors.size `should be equal to` 1
        retrograd.interceptors[0] `should be` interceptor
    }

    private companion object {
        const val BASE_URL = "http://test.com/"
        const val BASE_URL_INCORRECT = "http2://test.+"
    }
}