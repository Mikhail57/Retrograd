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
    kotlin("multiplatform") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.10.2"
    id("nebula.dependency-lock") version "9.0.0"
    id("nebula.nebula-bintray") version "8.3.0"
    id("nebula.maven-publish") version "17.2.1"
    id("nebula.source-jar") version "17.2.1"
    id("nebula.javadoc-jar") version "17.2.1"
    id("nebula.release") version "15.0.2"
}

repositories {
    jcenter()
}

group = DefaultValues.groupId
description = "JSON RPC 2.0 Client Library"


tasks {
//    test {
//        useJUnitPlatform()
//    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Dependencies.ktorClient)

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

//dependencies {
//    compileOnly(Dependencies.android)
//
//    implementation(Dependencies.okhttp)
//    implementation(Dependencies.rxjava)
//    implementation(Dependencies.gson)
//    implementation(Dependencies.ktorClient)
//    implementation(Dependencies.ktorClientOkhttp)
//
//    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.10.2")
//
//    testImplementation(Dependencies.junit)
//    testImplementation(Dependencies.mockk)
//    testImplementation(Dependencies.kluent)
//    testImplementation(Dependencies.mockWebServer)
//}

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
