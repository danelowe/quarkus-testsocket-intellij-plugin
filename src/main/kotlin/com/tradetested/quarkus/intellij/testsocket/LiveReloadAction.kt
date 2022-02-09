package com.tradetested.quarkus.intellij.testsocket

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import com.tradetested.quarkus.testsocket.spi.command.DisableLiveReload
import com.tradetested.quarkus.testsocket.spi.command.EnableLiveReload

class LiveReloadAction(
    private val state: ContinuousTestRunProfileState,
    private val manager: ContinuousTestWebSocketManager
) : DumbAwareToggleAction("Toggle Live Reload", "Turn live reload on/off", AllIcons.Actions.BuildAutoReloadChanges) {
    override fun isSelected(e: AnActionEvent): Boolean = state.liveReload

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        manager.run(if (state) EnableLiveReload() else DisableLiveReload())
    }
}