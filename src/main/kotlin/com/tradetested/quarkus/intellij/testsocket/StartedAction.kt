package com.tradetested.quarkus.intellij.testsocket

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import com.tradetested.quarkus.testsocket.spi.command.Pause
import com.tradetested.quarkus.testsocket.spi.command.Resume

class StartedAction(
    private val state: ContinuousTestRunProfileState,
    private val manager: ContinuousTestWebSocketManager
    ) : DumbAwareToggleAction("Pause/Resume Continuous Testing", "Pause or resume continuous testing", AllIcons.Actions.RunAll) {
    override fun isSelected(e: AnActionEvent): Boolean = state.started

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        manager.run(if (state) Resume() else Pause())
    }
}