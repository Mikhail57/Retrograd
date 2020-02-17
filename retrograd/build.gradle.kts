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
import java.time.LocalDateTime
import java.time.ZoneId

plugins {
    `java-library`
    kotlin("jvm")
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
}

kotlinProject()

//val jcenter_user: String = rootProject.findProperty("jcenter_user") as String
//val jcenter_api_key: String by project(":")
fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = findProperty("jcenter_user")
    println(user)
//    key = jcenter_api_key

    setConfigurations("archive")
    pkg.apply {
        repo = "Retrograd"
        name = "retrograd"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/Mikhail57/Retrograd"

        version.apply {
            name = "0.1-alpha"
            desc = "JSON RPC 2 library for Android 0.1-alpha"
            released = LocalDateTime.now().atZone(ZoneId.of("UTC+3")).toString()

        }
    }
}

task("sourcesJar", type = Jar::class) {
    from(sourceSets.main.get().java.srcDirs)
    archiveClassifier.set("sources")
}

task("dokkaJar", type = Jar::class) {
    dependsOn("dokka")
    archiveClassifier.set("dokka")
    val dokkaTask = tasks["dokka"] as org.jetbrains.dokka.gradle.DokkaTask
    from(dokkaTask.outputDirectory)
}

artifacts {
    archives(tasks["dokkaJar"])
    archives(tasks["sourcesJar"])
}

dependencies {
    compileOnly(Dependencies.android)

    implementation(Dependencies.okhttp)
    implementation(Dependencies.rxjava)
    implementation(Dependencies.gson)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.kluent)
}
