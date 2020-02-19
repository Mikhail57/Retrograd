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
import org.gradle.api.publish.maven.MavenPom
import java.io.FileInputStream
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
    id("com.github.dcendents.android-maven")
}

kotlinProject()

group = DefaultValues.groupId
val properties = Properties().apply {
    load(FileInputStream(project.rootProject.file("local.properties")))
}

fun findProperty(s: String): String? = properties.getProperty(s)

bintray {
    user = findProperty("jcenter.user")
    key = findProperty("jcenter.apiKey")

    override = true

    setConfigurations("archives")
    pkg.apply {
        repo = "Retrograd"
        name = "retrograd"
        userOrg = "retrograd"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/Mikhail57/Retrograd"

        version.apply {
            name = DefaultValues.targetVersion
            desc = "JSON RPC 2 library for Android ${DefaultValues.targetVersion}"
            released = Date().toString()

            vcsTag = DefaultValues.targetVersion
            vcsUrl = "https://github.com/Mikhail57/Retrograd"

            publish = true
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

val publicationName = "retrograd"
publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])
            artifactId = "retrograd"
            version = DefaultValues.targetVersion
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJar"])
            pom.addDependencies()
        }
    }
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

fun MavenPom.addDependencies() = withXml {
    asNode().appendNode("dependencies").let { depNode ->
        configurations.compile.get().allDependencies.forEach {
            depNode.appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
            }
        }
    }
}
