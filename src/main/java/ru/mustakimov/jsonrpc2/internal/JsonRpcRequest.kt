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

package ru.mustakimov.jsonrpc2.internal

import com.google.gson.annotations.SerializedName

sealed class JsonRpcRequest(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("method")
    val method: String
) {
    @SerializedName("jsonrpc")
    val jsonrpc: String = "2.0"

    class JsonRpcNamedRequest(
        id: Long?,
        method: String,
        @SerializedName("params")
        val params: Map<String, Any?> = emptyMap()
    ) : JsonRpcRequest(id, method) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (!super.equals(other)) return false
            if (javaClass != other.javaClass) return false

            other as JsonRpcNamedRequest

            if (params != other.params) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + params.hashCode()
            return result
        }

        override fun toString(): String {
            return "JsonRpcNamedRequest(id=$id, method='$method', params=$params)"
        }
    }

    class JsonRpcUnnamedRequest(
        id: Long?,
        method: String,
        @SerializedName("params")
        val params: List<Any?> = emptyList()
    ) : JsonRpcRequest(id, method) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (!super.equals(other)) return false
            if (javaClass != other.javaClass) return false

            other as JsonRpcUnnamedRequest

            if (params != other.params) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + params.hashCode()
            return result
        }

        override fun toString(): String {
            return "JsonRpcUnnamedRequest(id=$id, method='$method', params=$params)"
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonRpcRequest) return false

        if (id != other.id) return false
        if (method != other.method) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + jsonrpc.hashCode()
        return result
    }
}
