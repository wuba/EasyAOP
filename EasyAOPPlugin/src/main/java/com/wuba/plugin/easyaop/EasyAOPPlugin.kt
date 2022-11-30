package com.wuba.plugin.easyaop

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.google.gson.Gson
import com.wuba.easy.log.green
import com.wuba.easy.log.red
import com.wuba.easy.utils.CommUtils
import com.wuba.easyaop.EasyBuildConfig
import com.wuba.easyaop.cfg.EasyConfig
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import com.wuba.plugin.easyaop.transform.EasyAOPTransform
import com.wuba.plugin.easyaop.transform.ProjectType
import com.wuba.plugin.easyaop.utils.getEnvStr
import com.wuba.plugin.easyaop.utils.isApplication
import com.wuba.plugin.easyaop.utils.isDynamicFeature
import com.wuba.plugin.easyaop.utils.isLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

/**
 *
 * Created by wswenyue on 2021/12/17.
 */
class EasyAOPPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!(target.isApplication() || target.isDynamicFeature() || target.isLibrary())) {
            println("Unsupported project!!! only support android : [application]/[dynamic-feature]/[library]".red())
            return
        }
        println("================= Welcome to use EasyAOP (${EasyBuildConfig.VERSION}) ====================".green())
        var cfgYamlPath =
            target.getEnvStr(EasyAOPConstant.easyAOPCfgYamlPath, null)
        if (CommUtils.isEmpty(cfgYamlPath)) {
            cfgYamlPath = File(
                target.projectDir.absolutePath,
                EasyAOPConstant.defEasyAOPCfgYamlFileName
            ).absolutePath
        }
        println("easy config YamlPath ==>$cfgYamlPath".green())
        val easyCfg = buildEasyConfig(cfgYamlPath, target)
            ?: throw RuntimeException("Easy Config load failure!!!")

        println("=========apply====EasyAOPPlugin=====${target.name}====>".green())
        if (target.isApplication() || target.isDynamicFeature()) {
            target.extensions.getByType(AppExtension::class.java)
                .registerTransform(EasyAOPTransform(target, ProjectType.APP, easyCfg))
        } else if (target.isLibrary()) {
            target.extensions.getByType(LibraryExtension::class.java)
                .registerTransform(EasyAOPTransform(target, ProjectType.APP, easyCfg))
        } else {
            throw RuntimeException("unknown error !!!")
        }
    }

    private fun buildEasyConfig(cfgYamlPath: String?, project: Project): EasyConfigWarp? {
        if (CommUtils.isEmpty(cfgYamlPath)) {
            println("cfgPath is empty!!!".red())
            return null
        }
        var cfgPath = Paths.get(cfgYamlPath as String)
        if (!cfgPath.isAbsolute) {
//                println("cfgPath is relative Path...")
            cfgPath = Paths.get(project.projectDir.absolutePath, cfgYamlPath)
        }
        println("read cfg path${cfgPath}".green())
        val cfgFile = cfgPath.toFile()
        if (!cfgFile.exists() || !cfgFile.isFile) {
            println("cfg not exists or not a file==>${cfgFile.absolutePath}".red())
            return null
        }
        val cfg = EasyConfig.buildFromYaml(cfgFile, project.properties) ?: return null
        println(Gson().toJson(cfg))
        return EasyConfigWarp(cfg)
    }


}