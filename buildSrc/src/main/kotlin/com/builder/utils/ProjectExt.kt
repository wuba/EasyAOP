package com.builder.utils

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.*

/**
 *
 * Created by wswenyue on 2022/2/16.
 */

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
    return defValue
}

fun Project.getEnvMapWarp(keyStarts: String): Map<String, Any?>? {
    val starts =
        if (!keyStarts.endsWith(".")) {
            "$keyStarts."
        } else {
            keyStarts
        }

    return properties.filter {
        it.key.startsWith(starts) && !it.key.substringAfter(starts).contains(".")
    }.mapKeys {
        it.key.substringAfter(starts)
    }
}

fun Project.localProp(onlyRoot: Boolean = false): Properties? {
    val localFile = when (onlyRoot) {
        true -> File(rootDir, "local.properties")
        else -> {
            File(projectDir, "local.properties").let {
                if (!it.exists() || !it.isFile)
                    File(rootDir, "local.properties")
                else
                    it
            }
        }
    }
    if (!localFile.exists() || !localFile.isFile) {
        return null
    }
    return Properties().apply {
        load(localFile.inputStream())
    }
}


fun Project.isApplication(): Boolean = plugins.hasPlugin("com.android.application")

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