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

import android.os.Build
import java.lang.invoke.MethodHandles.Lookup
import java.lang.reflect.Constructor
import java.lang.reflect.Method

open class Platform internal constructor(
    private val hasJava8Types: Boolean
) {

    fun isDefaultMethod(method: Method): Boolean = hasJava8Types && method.isDefault

    @Throws(Throwable::class)
    fun invokeDefaultMethod(method: Method, declaringClass: Class<*>, obj: Any, vararg args: Any?): Any? {
        // Because the service interface might not be public, we need to use a MethodHandle lookup
        // that ignores the visibility of the declaringClass.
        val constructor: Constructor<Lookup> =
            Lookup::class.java.getDeclaredConstructor(Class::class.java, Int::class.javaPrimitiveType)
        constructor.isAccessible = true
        return constructor.newInstance(declaringClass, -1)
            .unreflectSpecial(method, declaringClass)
            .bindTo(obj)
            .invokeWithArguments(*args)
    }

    companion object {
        val INSTANCE: Platform = findPlatform()

        private fun findPlatform(): Platform {
            try {
                Class.forName("android.os.Build")
                return Android()
            } catch (e: ClassNotFoundException) {
            }
            return Platform(true)
        }
    }

}
class Android: Platform(Build.VERSION.SDK_INT >= 24) {

}
