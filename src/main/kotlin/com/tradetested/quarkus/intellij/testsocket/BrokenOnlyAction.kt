package com.tradetested.quarkus.intellij.testsocket

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import com.tradetested.quarkus.testsocket.spi.command.DisableBrokenOnlyMode
import com.tradetested.quarkus.testsocket.spi.command.EnableBrokenOnlyMode

class BrokenOnlyAction(
    private val state: ContinuousTestRunProfileState,
    private val manager: ContinuousTestWebSocketManager
) : DumbAwareToggleAction("Toggle Broken Only", "Turn broken-only mode on or off", AllIcons.RunConfigurations.ToolbarFailed) {
    override fun isSelected(e: AnActionEvent): Boolean = state.brokenOnly

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        manager.run(if (state) EnableBrokenOnlyMode() else DisableBrokenOnlyMode())
    }
}