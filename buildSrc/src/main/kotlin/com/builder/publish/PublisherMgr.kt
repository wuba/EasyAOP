package com.builder.publish

import com.builder.publish.tasks.GeneratedJarCodeTask
import com.builder.publish.tasks.PublishResultPrintTask
import com.builder.utils.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import java.util.*


/**
 *
 * Created by wswenyue on 2022/2/9.
 */
object PublisherMgr {

    private var localRootProp: Properties? = null

    @JvmStatic
    fun config(project: Project) {
        if (project.isApplication()) {
            return
        }
        if (!project.getEnvBoolean("useMavenUpload", false)) {
            return
        }
        project.run {
            println("===========${name}==============")
            pluginManager.apply("maven-publish")
            if (localRootProp == null) {
                localRootProp = localProp(true)
            }
            configResultTask(this)
            afterEvaluate {
                it.extensions.getByType(PublishingExtension::class.java).let { publishing ->
                    configMavenRepo(publishing)
                    configUpload(project, publishing)
                }
            }

        }
    }


    private val pubMavenLocalRegex by lazy { """.*publish(.+)PublicationToMaven\S*""".toRegex() }
    private fun configResultTask(target: Project) {
        target.tasks.whenTaskAdded { theTask: Task ->
            if (theTask is AbstractPublishToMaven) {
                val mavenLocalPubName =
                    pubMavenLocalRegex.find(theTask.name)?.destructured?.toList()?.first()
                if (CommUtils.isNotEmpty(mavenLocalPubName)) {
                    println("task->${theTask.name}")
                    println("mavenLocalPubName->${mavenLocalPubName}")
                    val resultTask = target.buildTask<PublishResultPrintTask>(
                        name = "${theTask.name}Result",
                        group = "easy publisher"
                    )
                    resultTask.configure { pubTask ->
                        (pubTask as PublishResultPrintTask).apply {
                            publisherTask = theTask
                        }
                    }
                    theTask.finalizedBy(resultTask)
                }
            } else if (theTask is JavaCompile) {
                if (theTask.name == "compileJava") {
                    println("JavaCompile==>${target.name}#${theTask.name}")
                    val generatedJarCodeTask = target.buildTask<GeneratedJarCodeTask>(
                        name = "GeneratedJarCode",
                        group = "easy publisher"
                    )

                    theTask.dependsOn(generatedJarCodeTask)
                }
            }
        }
    }

    private fun configMavenRepo(publishing: PublishingExtension) {
        localRootProp?.let {
            val mavenUrl = it.getProperty("mavenUrl")
            val mavenUserName = it.getProperty("mavenUserName")
            val mavenPassword = it.getProperty("mavenPassword")
            if (CommUtils.isNotEmpty(mavenUrl)) {
                publishing.repositories.run {
                    maven { mvn ->
                        mvn.url = URI.create(mavenUrl)
                        if (CommUtils.isNotEmpty(mavenUserName) && CommUtils.isNotEmpty(
                                mavenPassword
                            )
                        ) {
                            mvn.credentials.run {
                                username = mavenUserName
                                password = mavenPassword
                            }
                        }
                    }
                }
            }
        }
    }


    private fun configUpload(project: Project, publishing: PublishingExtension) {
        val mGroupId = project.getEnvStr("groupId")
        val mArtifactId = project.getEnvStr("artifactId")
        val mVersion = project.getEnvStr("version")?.let {
            if (project.getEnvBoolean("isSnapshot", false)) {
                "${it}-SNAPSHOT"
            } else {
                it
            }
        }
        if (CommUtils.isEmpty(mGroupId) ||
            CommUtils.isEmpty(mArtifactId) ||
            CommUtils.isEmpty(mVersion)
        ) {
            throw RuntimeException("==${project.name}== groupId or artifactId or version is Empty!!!")
        }

        val sourcesJar = project.tasks.run {
            var sourceTask = findByName("sourcesJar")
            if (sourceTask == null) {
                val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
                val main = sourceSets.named("main").get()
                sourceTask = create("sourcesJar", Jar::class.java).run {
                    archiveClassifier.set("sources")
                    from(main.allSource)
                }
            }
            sourceTask
        }

//        project.tasks.withType(Jar::class.java).configureEach { jar ->
//            jar.manifest {
//                it.attributes(
//                    mapOf<String, String>(
//                        "EasyAOP-Version" to mVersion.toString()
//                    )
//                )
//            }
//        }

//        val javadocJar = project.tasks.let {
//            it.create("javadocJar", Jar::class.java).run {
//                val javadoc = it.named("javadoc")
//                dependsOn.add(javadoc)
//                archiveClassifier.set("javadoc")
//                from(javadoc)
//            }
//        }

        publishing.publications.run {
            if (project.pluginManager.hasPlugin("java-gradle-plugin")) {
                withType(MavenPublication::class.java).configureEach { pub: MavenPublication ->
                    pub.run {
                        groupId = mGroupId
                        artifactId = mArtifactId
                        version = mVersion
                        artifact(sourcesJar)
                        pom.withXml {
                            PomSetting(it, project).doSetting()
                        }
                    }
                }
            } else {
                create("mavenJava", MavenPublication::class.java) { pub: MavenPublication ->
                    pub.run {
                        from(project.components.getByName("java"))
                        groupId = mGroupId
                        artifactId = mArtifactId
                        version = mVersion
                        artifact(sourcesJar)
                        pom.withXml {
                            PomSetting(it, project, packaging = "jar").doSetting()
                        }
                    }
                }
            }
        }
    }

}
