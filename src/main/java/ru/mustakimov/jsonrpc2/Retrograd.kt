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
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL
import java.util.Deque
import java.util.ArrayDeque
import kotlin.reflect.KClass

/**
 * Retrograd adapts a Java interface to JSON RPC 2.0 calls, by using annotations on interface and
 * declared methods to define how RPC calls are made. Create instance using [the builder][Builder]
 * and pass your interface to the [create] method to generate an implementation.
 *
 * For example,
 * ```kotlin
 * val retrograd: Retrograd = Retrograd.Builder()
 *     .baseUrl("http://rpc.example.com/")
 *     .build()
 *
 * val myApi: MyApi = retrograd.create(MyApi::class.java)
 * val response: Single<Boolean> = retrograd.isValidPhone("+7(912)123-45-67")
 * ```
 */
class Retrograd private constructor(
    val gson: Gson,
    val httpClient: HttpClient,
    val baseUrl: HttpUrl,
    val interceptors: List<Interceptor>
) {
    /**
     * Create an implementation of the JSON RPC 2.0 API services, using interface annotated by [JsonRpc].
     *
     * The relative path for the given service is obtained from the interface [annotation][JsonRpc].
     *
     * Method parameters currently can be only named.
     *
     * Named parameters should be annotated with [Param] annotation.
     *
     * By default, methods return a [Single][io.reactivex.Single] which represent JSON RPC 2.0 response body.
     */
    @Suppress("UNCHECKED_CAST") //
    fun <T> create(service: Class<T>): T {
        validateServiceInterface(service)
        val endpoint = baseUrl.resolve(getServiceEndpoint(service))
            ?: throw IllegalArgumentException("Invalid endpoint path in ${service.simpleName}")
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf(service),
            object : InvocationHandler {
                val emptyArgs: Array<Any> = emptyArray()
                val platform = Platform.INSTANCE

                override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
                    if (method.declaringClass == Any::class.java) {
                        return method.invoke(this, *(args ?: emptyArgs))
                    }
                    if (platform.isDefaultMethod(method)) {
                        return platform.invokeDefaultMethod(method, service, proxy, *(args ?: emptyArgs))
                    }

                    return loadServiceMethod(method).invoke(args ?: emptyArgs, endpoint)
                }
            }) as T
    }

    /**
     * Kotlin class variant of the [create] method
     * @see create
     */
    fun <T : Any> create(service: KClass<T>): T = create(service.java)

    private fun loadServiceMethod(method: Method): ServiceMethod<*> {
        return ServiceMethod.parseAnnotations<Any>(this, method)
    }

    @Throws(IllegalArgumentException::class)
    private fun <T> validateServiceInterface(service: Class<T>) {
        if (!service.isInterface) {
            throw IllegalArgumentException("API declaration must be interface")
        }
        if (service.getAnnotation(JsonRpcService::class.java) == null) {
            throw IllegalArgumentException("API declaration must be annotated with @${JsonRpcService::class.java.simpleName}")
        }

        val check: Deque<Class<*>> = ArrayDeque()
        check.add(service)
        while (check.isNotEmpty()) {
            val candidate = check.removeFirst()
            if (candidate.typeParameters.isNotEmpty()) {
                val builder = StringBuilder("Type parameters are not supported on ").append(candidate.name)
                if (candidate != service) {
                    builder.append(" which is an interface of ").append(service.name)
                }
                throw IllegalArgumentException(builder.toString())
            }
            check.addAll(candidate.interfaces)
        }
    }

    private fun <T> getServiceEndpoint(service: Class<T>): String {
        val annotation = service.getAnnotation(JsonRpcService::class.java)
        return annotation.value
    }


    /**
     * Build a new {@link Retrograd}.
     *
     * Calling {@link .baseUrl} is required before calling {@link .build()}. All other methods
     * are optional.
     */
    class Builder {
        private var baseUrl: HttpUrl? = null
        private var gson: Gson? = null
        private var okHttpClient: OkHttpClient? = null
        private var interceptors: MutableList<Interceptor> = mutableListOf()

        /**
         * Set the API base URL.
         *
         * @see [baseUrl][Retrograd.Builder.baseUrl]
         */
        fun baseUrl(baseUrl: URL): Builder = baseUrl(baseUrl.toString().toHttpUrl())

        /**
         * Set the API base URL.
         *
         * @see [baseUrl][Retrograd.Builder.baseUrl]
         */
        fun baseUrl(baseUrl: String): Builder = baseUrl(baseUrl.toHttpUrl())

        /**
         * Set the API base URL.
         *
         *
         * The specified endpoint values (such as with [@JsonRpc][JsonRpc]) are resolved against this
         * value using [HttpUrl.resolve]. The behavior of this matches that of an
         * `<a href="">` link on a website resolving on the current URL.
         *
         *
         * **Base URLs should always end in `/`.**
         *
         *
         * A trailing `/` ensures that endpoints values which are relative paths will correctly
         * append themselves to a base which has path components.
         *
         *
         * **Correct:**<br></br>
         * Base URL: http://example.com/api/<br></br>
         * Endpoint: foo/bar/<br></br>
         * Result: http://example.com/api/foo/bar/
         *
         *
         * **Incorrect:**<br></br>
         * Base URL: http://example.com/api<br></br>
         * Endpoint: foo/bar/<br></br>
         * Result: http://example.com/foo/bar/
         *
         *
         * This method enforces that `baseUrl` has a trailing `/`.
         *
         *
         * **Endpoint values which contain a leading `/` are absolute.**
         *
         *
         * Absolute values retain only the host from `baseUrl` and ignore any specified path
         * components.
         *
         *
         * Base URL: http://example.com/api/<br></br>
         * Endpoint: /foo/bar/<br></br>
         * Result: http://example.com/foo/bar/
         *
         *
         * Base URL: http://example.com/<br></br>
         * Endpoint: /foo/bar/<br></br>
         * Result: http://example.com/foo/bar/
         *
         *
         * **Endpoint values may be a full URL.**
         *
         *
         * Values which have a host replace the host of `baseUrl` and values also with a scheme
         * replace the scheme of `baseUrl`.
         *
         *
         * Base URL: http://example.com/<br></br>
         * Endpoint: https://github.com/square/retrofit/<br></br>
         * Result: https://github.com/square/retrofit/
         *
         *
         * Base URL: http://example.com<br></br>
         * Endpoint: //github.com/square/retrofit/<br></br>
         * Result: http://github.com/square/retrofit/ (note the scheme stays 'http')
         */
        fun baseUrl(baseUrl: HttpUrl): Builder = this.apply {
            val pathSegments = baseUrl.pathSegments
            require("" == pathSegments[pathSegments.size - 1]) { "baseUrl must end in /: $baseUrl" }
            this.baseUrl = baseUrl
        }

        fun <T : Interceptor> addInterceptor(interceptor: T) = this.apply {
            interceptors.add(interceptor)
        }

        fun gson(gson: Gson): Builder = this.apply {
            this.gson = gson
        }

        fun client(client: OkHttpClient): Builder = this.apply {
            okHttpClient = client
        }

        fun build(): Retrograd {
            val url = baseUrl ?: throw IllegalStateException("Base URL required")
            val client = okHttpClient ?: OkHttpClient()
            val gson = this.gson ?: Gson()

            return Retrograd(gson, HttpClient(OkHttp) {
                engine {
                    preconfigured = client
                }
            }, url, interceptors)
        }
    }
}