package com.builder.publish.tasks

import com.builder.utils.CommUtils
import com.builder.utils.getEnvBoolean
import com.builder.utils.getEnvMapWarp
import com.builder.utils.getEnvStr
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

/**
 *
 * Created by wswenyue on 2022/9/15.
 */
open class GeneratedJarCodeTask : DefaultTask() {
    @TaskAction
    fun doRun() {
        println("======GeneratedJarCodeTask===begin==")
        println("name:${project.name}")

        project.getEnvMapWarp("easy.config")?.let { cfg ->
            val dir = cfg["dir"] as String?
            val packageName = cfg["package"] as String?
            val name = cfg["name"] as String?
            if (CommUtils.isNotEmpty(dir)
                && CommUtils.isNotEmpty(packageName)
                && CommUtils.isNotEmpty(name)
            ) {

                val build = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

                project.rootProject.getEnvStr("version")?.let {
                    if (project.rootProject.getEnvBoolean("isSnapshot", false)) {
                        "${it}-SNAPSHOT"
                    } else {
                        it
                    }
                }?.let {
                    build.addField(
                        FieldSpec.builder(String::class.java, "VERSION")
                            .addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
                            .initializer("\$S", it)
                            .build()
                    )
                }

                project.getEnvMapWarp("easy.config.field")
                    ?.forEach { (fKey, fValue) ->
                        build.addField(
                            FieldSpec.builder(String::class.java, fKey)
                                .addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
                                .initializer("\$S", fValue.toString())
                                .build()
                        )
                    }

                File(project.projectDir, dir!!).run {
                    mkdirs()
                    JavaFile.builder(packageName, build.build())
                        .build().writeTo(this)
                }

            }
        }

        println("======GeneratedJarCodeTask===end==")
    }
}