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
import java.io.FileInputStream
import java.util.*

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.jetbrains.dokka") version "0.10.1"
    id("nebula.dependency-lock") version "8.8.0"
    id("nebula.nebula-bintray") version "8.3.0"
//    id("nebula.release") version "13.0.0"
}

repositories {
    jcenter()
}

group = DefaultValues.groupId

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

dependencies {
    compileOnly(Dependencies.android)

    implementation(Dependencies.okhttp)
    implementation(Dependencies.rxjava)
    implementation(Dependencies.gson)

    implementation(kotlin("stdlib"))

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.kluent)
}

bintray {
    user.set(findLocalProperty("jcenter.user"))
    apiKey.set(findLocalProperty("jcenter.apiKey"))

    licenses.add("Apache-2.0")

    userOrg.set("retrograd")
    vcsUrl.set("https://github.com/Mikhail57/Retrograd")
}


fun findLocalProperty(s: String): String? {
    val properties: Properties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }
    return properties.getProperty(s)
}
