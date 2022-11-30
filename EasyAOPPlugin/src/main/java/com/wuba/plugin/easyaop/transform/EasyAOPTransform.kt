package com.wuba.plugin.easyaop.transform

import com.android.build.api.transform.TransformInvocation
import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import com.wuba.easy.utils.CommUtils
import com.wuba.easyaop.EasyBuildConfig
import com.wuba.easyaop.asm.ClassAOPEngine
import com.wuba.easyaop.cfg.warp.EasyConfigWarp
import com.wuba.plugin.easyaop.EasyAOPConstant
import org.gradle.api.Project
import java.io.File

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
class EasyAOPTransform(
    project: Project,
    projectType: ProjectType,
    private val cfg: EasyConfigWarp
) : BaseTransform(
    project,
    projectType
) {

    private val tag: String = "EasyAOPTransform"

    private val classAOPEngine: ClassAOPEngine by lazy {
        ClassAOPEngine(cfg)
    }

    override fun getName(): String {
        return tag
    }

    override fun isIncremental(): Boolean {
        //指明是否是增量构建
        return true
    }

    private fun initLogger() {
        Log.baseTag = EasyAOPConstant.baseTag
        Log.isLogEnable = cfg.isLogEnable
        Log.minLogLevel = cfg.minLogLevel
        val workDir = File(project.buildDir, EasyAOPConstant.workDirName)
        if (workDir.exists()) {
            workDir.mkdirs()
        }
        Log.init(workDir)
    }

    override fun onPreTransform(transformInvocation: TransformInvocation?) {
        super.onPreTransform(transformInvocation)
        initLogger()
    }

    override fun onPostTransform(transformInvocation: TransformInvocation?) {
        super.onPostTransform(transformInvocation)
        Log.closeResWaitIoFinished()
    }

    override fun doTransform(transformInvocation: TransformInvocation?) {
        val buildType = when (transformInvocation?.isIncremental) {
            true -> "增量编译"
            false -> "全量编译"
            null -> "未知"
        }
        Log.debug(tag) {
            "=>=>=>=>=>=>${buildType}[${projectType.name}]=>=>=>(EasyAopVersion:${EasyBuildConfig.VERSION})=>=>=>[begin]"
        }
        val begin = System.currentTimeMillis()

        transformInvocation?.let { transformContext ->
            TransformWorker(
                project,
                transformContext,
                classAOPEngine,
                cfg.isNeedDexCheck,
                jarFilter = cfg::checkMatchRootJars
            ).doTransform()
        }
        val duration = CommUtils.durationTimeFormat(
            System.currentTimeMillis() - begin
        )
        Log.debug(tag) {
            "<=<=<=<=<=<=${buildType}[${projectType.name}](处理总耗时：${duration})<=<=<=<=<=<=[end]"
        }
    }

}