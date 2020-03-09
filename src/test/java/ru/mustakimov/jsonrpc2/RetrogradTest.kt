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
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class RetrogradTest {
    var server: MockWebServer = MockWebServer()

    @JsonRpc("/")
    interface Valid {
        @JsonRpcMethod("plus")
        fun validNamedPlusMethod(
            @Param("a") a: Int,
            @Param("b") b: Int
        ): Single<Int>

        @JsonRpcMethod("plus", namedParams = false)
        fun validUnnamedPlusMethod(
            @Param("a") a: Int,
            @Param("b") b: Int
        ): Single<Int>

        @JsonRpcMethod("plus")
        fun invalidParamNamedPlusMethod(
            a: Int,
            @Param("b") b: Int
        ): Single<Int>

        @JsonRpcMethod("plus", namedParams = false)
        fun invalidParamUnnamedPlusMethod(
            a: Int,
            @Param("b") b: Int
        ): Single<Int>

        @JsonRpcMethod("plus")
        fun invalidReturnType(
            @Param("a") a: Int,
            @Param("b") b: Int
        ): Int
    }

    @JsonRpc("/")
    interface Extending : Valid

    interface UnannotatedExtending : Valid

    interface Unannotated

    @JsonRpc("/")
    interface TypeParam<T>

    @JsonRpc("/")
    interface ExtendingTypeParameter : TypeParam<String>

    @JsonRpc("/")
    class InvalidClass

    @Test
    fun `Should create api with valid interface`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        retrograd.create(Valid::class)
    }

    @Test
    fun `Shouldn't create api with unannotated interface`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val e = assertThrows<IllegalArgumentException> {
            retrograd.create(Unannotated::class)
        }
        e.message!! `should contain` "must be annotated with @JsonRpc"
    }

    @Test
    fun `Should create api with interface extended from valid interface`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        retrograd.create(Extending::class)
    }

    @Test
    fun `Shouldn't create api extended from valid api, but no annotated`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        assertThrows<IllegalArgumentException> {
            retrograd.create(UnannotatedExtending::class)
        }
    }

    @Test
    fun `Should not create api with type parameter`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        assertThrows<IllegalArgumentException> {
            retrograd.create(TypeParam::class)
        }
    }

    @Test
    fun `Should not create api with extended type parameter`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        assertThrows<IllegalArgumentException> {
            retrograd.create(ExtendingTypeParameter::class)
        }
    }

    @Test
    fun `Default methods should be callable`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)
        assertDoesNotThrow {
            valid.toString()
            @Suppress("UnusedEquals")
            valid == valid
            valid.hashCode()
        }
    }

    @Test
    fun `Should call valid method with named params`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)

        server.enqueue(MockResponse().setBody("""{"id": 1, "result": 3}"""))

        val testObserver = valid.validNamedPlusMethod(1, 2).test()
        testObserver.assertComplete()
        testObserver.assertResult(3)
    }

    @Test
    fun `Should call valid method with unnamed params`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)

        server.enqueue(MockResponse().setBody("""{"id": 1, "result": 3}"""))

        val testObserver = valid.validUnnamedPlusMethod(1, 2).test()
        testObserver.assertComplete()
        testObserver.assertResult(3)
    }

    @Test
    fun `Shouldn't call invalid method with unannotated named param`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)

        assertThrows<IllegalArgumentException> {
            valid.invalidParamNamedPlusMethod(1, 2)
        }.message!! `should contain` "Argument #0 of #invalidParamNamedPlusMethod must be annotated with @Param"
    }

    @Test
    fun `Shouldn't call invalid method with unannotated unnamed param`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)

        assertThrows<IllegalArgumentException> {
            valid.invalidParamUnnamedPlusMethod(1, 2)
        }.message!! `should contain` "Argument #0 of #invalidParamUnnamedPlusMethod must be annotated with @Param"
    }

    @Test
    fun `Shouldn't call method with wrong return type`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val valid = retrograd.create(Valid::class)

        assertThrows<IllegalArgumentException> {
            valid.invalidReturnType(1, 2)
        }.message!! `should contain` "Only io.reactivex.Single<T> is supported as return type"
    }

    @Test
    fun `Should call default interface method`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        val default = retrograd.create(JavaInterfaces.DefaultJavaInterface::class)

        default.sum(1, 2) `should be` 3
    }

    @Test
    fun `Shouldn't create from class`() {
        val retrograd = Retrograd.Builder().baseUrl(server.url("/")).build()
        assertThrows<IllegalArgumentException> {
            retrograd.create(InvalidClass::class)
        }
    }

}