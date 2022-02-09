package com.tradetested.quarkus.intellij.testsocket

import com.intellij.execution.ExecutionManager
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestConfigurationType
import com.tradetested.quarkus.intellij.testsocket.configuration.ContinuousTestRunConfiguration
import org.jetbrains.concurrency.Promise
import java.util.concurrent.atomic.AtomicReference

/**
 * We don't extend from AsyncProgramRunner, or GenericProgramRunner because they seemed to run build process.
 * Now, we have solved that in [ContinuousTestConfigurationFactory], so we can use AsyncProgramRunner now?
 */
class ContinuousTestProgramRunner : ProgramRunner<RunnerSettings> {

    override fun getRunnerId(): String = ContinuousTestConfigurationType.ID

    override fun canRun(executorId: String, profile: RunProfile) =
        (executorId == DefaultRunExecutor.EXECUTOR_ID)
                && (profile is ContinuousTestRunConfiguration)

    override fun execute(environment: ExecutionEnvironment) {
        val state = environment.state ?: return
        val executionResult = state.execute(environment.executor, this)!!

        FileDocumentManager.getInstance().saveAllDocuments()

        // This opens the run window, but also starts the build. Find out why, and also use AsyncProgramRunner
        // in doStartRunProfile somewhere, forceCompilationInTests is false, but isUnitTestMode is also false
        // can we prevent compileAndRun from running the build?
        // doGetBeforeRunTasks returns a make task
        ExecutionManager.getInstance(environment.project).startRunProfile(environment) {
            val result = AtomicReference<RunContentDescriptor>()
            ApplicationManager.getApplication().invokeAndWait {
                val contentBuilder = RunContentBuilder(executionResult, environment)
                if (state is ContinuousTestRunProfileState) {
                    state.actions.forEach {
                        contentBuilder.addAction(it)
                    }
                }
//            if (state !is JavaCommandLineState || (state as JavaCommandLineState).shouldAddJavaProgramRunnerActions()) {
//                DefaultJavaProgramRunner.addDefaultActions(contentBuilder, executionResult, state is JavaCommandLine)
//            }
                result.set(contentBuilder.showRunContent(environment.contentToReuse))
            }
            Promise.resolve(result.get())
        }


    }
}