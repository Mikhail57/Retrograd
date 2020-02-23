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

import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RetrogradTest {
    var server: MockWebServer = MockWebServer()

    @JsonRpc("/")
    interface Valid {

    }

    @JsonRpc("/")
    interface Extending : Valid

    interface UnannotatedExtending : Valid

    interface Unannotated

    @JsonRpc("/")
    interface TypeParam<T>

    @JsonRpc("/")
    interface ExtendingTypeParameter : TypeParam<String>

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

}