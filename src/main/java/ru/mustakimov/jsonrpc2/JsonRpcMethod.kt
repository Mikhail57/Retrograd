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

annotation class JsonRpcMethod(
    /**
     * Json RPC method name
     */
    val value: String,

    /**
     * Use named or unnamed params.
     *
     * When `true`, uses [Param]'s [value][Param.value] as notation to the param name, otherwise pass params name from code.
     *
     * When `false`, uses [Param] as notation to param, passes in defined order.
     */
    val namedParams: Boolean = true
)