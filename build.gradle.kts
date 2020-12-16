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
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.dokka") version "1.4.20"
    id("nebula.dependency-lock") version "11.1.1"
    id("nebula.nebula-bintray") version "8.3.0"
    id("nebula.maven-publish") version "17.3.2"
    id("nebula.source-jar") version "17.3.2"
    id("nebula.javadoc-jar") version "17.3.2"
    id("nebula.release") version "15.3.0"
}

repositories {
    jcenter()
}

group = DefaultValues.groupId
description = "JSON RPC 2.0 Client Library"


tasks {
    dokkaJavadoc {
        enabled = true
    }
    dokkaHtml {
        enabled = true
    }
//    dokka {
//        outputFormat = "javadoc"
//    }
    test {
        useJUnitPlatform()
    }
}

kotlin {
    explicitApi = ExplicitApiMode.Warning
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
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
    testImplementation(Dependencies.mockWebServer)
}

bintray {
    user.set(findLocalProperty("jcenter.user"))
    apiKey.set(findLocalProperty("jcenter.apiKey"))

    licenses.add("Apache-2.0")

    syncToMavenCentral.set(false)

    userOrg.set("retrograd")
    pkgName.set("retrograd")
    repo.set("Retrograd")

    vcsUrl.set("https://github.com/Mikhail57/Retrograd")
    labels.addAll("kotlin", "json rpc 2.0", "client")
}


fun findLocalProperty(s: String): String? {
    val properties: Properties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }
    return properties.getProperty(s)
}
