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

object Dependencies {
    const val android = "com.google.android:android:4.1.1.4"
    const val rxjava = "io.reactivex.rxjava2:rxjava:2.2.20"
    const val okhttp = "com.squareup.okhttp3:okhttp:4.9.0"
    const val gson = "com.google.code.gson:gson:2.8.6"

    // Tests
    const val junit = "org.junit.jupiter:junit-jupiter:5.7.0"
    const val mockk = "io.mockk:mockk:1.10.3"
    const val kluent = "org.amshove.kluent:kluent:1.61"
    const val mockWebServer = "com.squareup.okhttp3:mockwebserver:4.9.0"
}

object DefaultValues {
    const val groupId = "com.github.mikhail57"
}