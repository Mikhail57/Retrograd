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
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

val Method.returnsSingle
    get() = this.returnType.canonicalName == Single::class.java.canonicalName

fun Method.jsonRpcNamedParameters(args: Array<out Any?>): Map<String, Any?> {
    return checkArguments()
        .mapIndexed { i, name -> name to args[i] }
        .associate { it }
}

fun Method.jsonRpcUnnamedParameters(args: Array<out Any?>): List<Any?> {
    return checkArguments()
        .mapIndexed { i, _ -> args[i] }
}

private fun Method.checkArguments(): List<String> {
    return parameterAnnotations
        .map {
            it?.firstOrNull { p -> Param::class.java.isInstance(p) }
        }
        .mapIndexed { i, annotation ->
            when (annotation) {
                is Param -> annotation.value
                else -> throw IllegalArgumentException("Argument #$i of #$name must be annotated with @${Param::class.java.simpleName}")
            }
        }
}

val Method.resultGenericTypeArgument: Type
    @Suppress("CAST_NEVER_SUCCEEDS")
    get() = (this.genericReturnType as ParameterizedType).actualTypeArguments.first()