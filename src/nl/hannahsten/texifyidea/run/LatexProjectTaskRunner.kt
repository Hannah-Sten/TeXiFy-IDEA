//package nl.hannahsten.texifyidea.run
//
//import com.intellij.execution.RunManager
//import com.intellij.execution.executors.DefaultRunExecutor
//import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
//import com.intellij.execution.impl.RunManagerImpl
//import com.intellij.execution.runners.ExecutionEnvironmentBuilder
//import com.intellij.openapi.module.ModuleType
//import com.intellij.openapi.project.Project
//import com.intellij.task.ModuleBuildTask
//import com.intellij.task.ProjectTask
//import com.intellij.task.ProjectTaskContext
//import com.intellij.task.ProjectTaskRunner
////import nl.hannahsten.texifyidea.modules.LatexModuleType
//import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
//import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
//import org.jetbrains.concurrency.AsyncPromise
//import org.jetbrains.concurrency.Promise
//
///**
// * This implements the 'build project' button, to the left of the run config dropdown.
// * It's not completely clear what it should be doing, but I think in other languages it just builds
// * the complete project, so I think the most sensible is to run all run configurations.
// */
//class LatexProjectTaskRunner : ProjectTaskRunner() {
//
//    override fun canRun(projectTask: ProjectTask): Boolean {
//        // Only run if we're only going to build LaTeX run configurations: don't interfere with other languages (e.g. maven tasks)
//        return projectTask is ModuleBuildTask && (ModuleType.get(projectTask.module).id == LatexModuleType.ID || RunManager.getInstance(projectTask.module.project).allConfigurationsList.all { it is LatexRunConfiguration })
//    }
//
//    override fun run(project: Project, context: ProjectTaskContext, vararg tasks: ProjectTask?): Promise<Result> {
//        // Each task will probably be a module, but we don't use it
//
//        project.getLatexRunConfigurations().forEach { runConfig ->
//            val latexSettings = RunManagerImpl.getInstanceImpl(project).getSettings(runConfig) ?: return@forEach
//            val environment = ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), latexSettings)?.build()
//                ?: return@forEach
//            RunConfigurationBeforeRunProvider.doExecuteTask(environment, latexSettings, null)
//        }
//
//        // Not sure what we're suppose to return
//        val promise = AsyncPromise<Result>()
//        // TaskRunnerResults.SUCCESS is experimental, we cannot use it
//        promise.setResult(object : Result {
//            override fun isAborted() = false
//            override fun hasErrors() = false
//        })
//        return promise
//    }
//}