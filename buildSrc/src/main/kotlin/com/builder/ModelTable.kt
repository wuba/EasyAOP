package com.builder

import com.builder.utils.getEnvBoolean
import com.builder.utils.getEnvStr
import org.gradle.api.Project

/**
 *
 * Created by wswenyue on 2022/2/9.
 */
object ModelTable {
    lateinit var rootProject: Project
    private var globalType: ModelType? = ModelType.SOURCE

    private fun of(modelInfo: ModelInfo): Any {
        if (globalType != null) {
            modelInfo.modelType = globalType!!
        }
        if (modelInfo.modelType == ModelType.SOURCE) {
            return rootProject.findProject(modelInfo.projectName) ?: modelInfo.resUri
        }
        return modelInfo.resUri
    }

    private val easyAopVersion by lazy {
        rootProject.getEnvStr("version")?.let {
            if (rootProject.getEnvBoolean("isSnapshot", false)) {
                "${it}-SNAPSHOT"
            } else {
                it
            }
        }
    }

    val easyAopAsm: Any by lazy {
        of(
            ModelInfo(
                "EasyAopASM",
                "com.wuba.easyaop:easyAopAsm:${easyAopVersion}",
                ModelType.MAVEN
            )
        )
    }
    val easyAopCfg: Any by lazy {
        of(
            ModelInfo(
                "EasyAopCfg",
                "com.wuba.easyaop:easyAopCfg:${easyAopVersion}",
                ModelType.MAVEN
            )
        )
    }
    val easyAOPPlugin: Any by lazy {
        of(
            ModelInfo(
                "EasyAOPPlugin",
                "com.wuba.easyaop:gradlePlugin:${easyAopVersion}",
                ModelType.MAVEN
            )
        )
    }
    val easyTools: Any by lazy {
        of(
            ModelInfo(
                "EasyTools",
                "com.wuba.easyaop:easyTools:${easyAopVersion}",
                ModelType.MAVEN
            )
        )
    }
}