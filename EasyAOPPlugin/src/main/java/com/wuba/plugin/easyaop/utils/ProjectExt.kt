package com.wuba.plugin.easyaop.utils

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.wuba.easy.utils.CommUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.*

/**
 *
 * Created by wswenyue on 2021/11/22.
 */

fun Project.isApplication(): Boolean = plugins.hasPlugin("com.android.application")
fun Project.isDynamicFeature(): Boolean = plugins.hasPlugin("com.android.dynamic-feature")
fun Project.isLibrary(): Boolean = plugins.hasPlugin("com.android.library")

fun Project.getAndroid(): BaseExtension = extensions.getByName("android") as BaseExtension

fun Project.getApiLevel(): Int? {
//    getAndroid().defaultConfig?.targetSdkVersion?.apiLevel
    return try {
//        val def = getAndroid().defaultConfig!!
//        def.javaClass
        //getTargetSdkVersion
//        getAndroid().defaultConfig?.targetSdkVersion?.apiLevel
        null
    } catch (e: Exception) {
        null
    }
}

fun TransformInvocation.getVariant(project: Project): BaseVariant {
    return project.getAndroid().let { android ->
        val variantName = context.variantName
        when (android) {
            is AppExtension -> when {
                variantName.endsWith("AndroidTest") -> android.testVariants.single { it.name == variantName }
                variantName.endsWith("UnitTest") -> android.unitTestVariants.single { it.name == variantName }
                else -> android.applicationVariants.single { it.name == variantName }
            }
            is LibraryExtension -> android.libraryVariants.single { it.name == variantName }
            else -> throw RuntimeException("variant not found")
        }
    }
}

fun Project.localProp(): Properties? {
    // current project local properties
    var localFile = File(projectDir, "local.properties")
    if (!localFile.exists() || !localFile.isFile) {
        // root project local properties
        localFile = File(rootDir, "local.properties")
    }
    if (!localFile.exists() || !localFile.isFile) {
        return null
    }
    return Properties().apply {
        load(localFile.inputStream())
    }
}

fun Project.getEnvBoolean(key: String, defValue: Boolean = false): Boolean {
    val value = getEnvStr(key) ?: return defValue
    return value.toBoolean()
}

fun Project.getEnvInt(key: String, defValue: Int = 0): Int {
    val value = getEnvStr(key) ?: return defValue
    return value.toInt()
}

fun Project.getEnvStr(key: String, defValue: String? = null): String? {
    if (hasProperty(key)) {
        return property(key).toString()
    }
    if (rootProject.hasProperty(key)) {
        return rootProject.property(key).toString()
    }
    val value = CommUtils.getSystemEnv(key)
    if (CommUtils.isNotEmpty(value)) {
        return value
    }
    return defValue
}


inline fun <reified T : DefaultTask> Project.buildTask(
    name: String = "",
    group: String = "",
    description: String = "",
    dependsOnTask: TaskProvider<out Task>? = null
): TaskProvider<out Task> {
    val taskName = if (CommUtils.isEmpty(name)) {
        T::class.java.simpleName
    } else {
        name
    } as String

    return (try {
        tasks.named(taskName)
    } catch (e: UnknownTaskException) {
        null
    } ?: tasks.register(taskName, T::class.java) {
        it.group = group
        it.description = description
        if (dependsOnTask != null) {
            it.dependsOn(dependsOnTask)
        }
    })
}


