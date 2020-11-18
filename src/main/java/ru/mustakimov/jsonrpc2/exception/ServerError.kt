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

package ru.mustakimov.jsonrpc2.exception

/**
 * Server-side error
 *
 * Reserved for implementation-defined server-errors.
 */
class ServerError(code: Int, message: String) : JsonRpcError(code = code, message = message) {
    init {
        if (code !in -32000 downTo -32099) {
            throw IllegalArgumentException("Error code should be in range of -32000 to -32099")
        }
    }

    override fun toString(): String {
        return "ServerError $code: $message"
    }
}