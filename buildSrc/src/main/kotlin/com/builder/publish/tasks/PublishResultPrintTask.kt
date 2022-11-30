package com.builder.publish.tasks

import com.builder.utils.green
import com.builder.utils.red
import org.gradle.api.DefaultTask
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 *
 * Created by wswenyue on 2021/12/14.
 */
open class PublishResultPrintTask : DefaultTask() {
    @Input
    var publisherTask: AbstractPublishToMaven? = null

    @TaskAction
    fun doRun() {
        if (publisherTask == null) {
            println("publisherTask is null!!!".red())
            return
        }
        if (publisherTask?.state?.failure != null) {
            //failure
            println("publisherTask run failure skip!!!".red())
            return
        }

        val publisher = publisherTask as AbstractPublishToMaven
        val groupId = publisher.publication.groupId
        val artifactId = publisher.publication.artifactId
        val version = publisher.publication.version
        val name = createToken(groupId, artifactId, version)
        when (publisher) {
            is PublishToMavenLocal -> {
                println("[MavenLocal] publish succeed==>${name}".green())
                val url = "${System.getProperty("user.home")}/.m2/repository/${
                    groupId.replace(
                        ".",
                        "/"
                    )
                }/${artifactId}"

                println("[MavenLocal]   :${url}".green())
            }
            is PublishToMavenRepository -> {
                println("[MavenRemote] publish succeed==>${name}".green())
                val metadataUrl = publisher.repository?.url.toString() + "${
                    groupId.replace(
                        ".",
                        "/"
                    )
                }/${artifactId}/maven-metadata.xml"
                println("[MavenRemote]  :$metadataUrl".green())
            }
            else -> {
                println("unknown publish type==>$name".red())
            }
        }
    }

    private fun createToken(groupId: String, artifactId: String, version: String): String {
        return "$groupId:$artifactId:$version"
    }

}