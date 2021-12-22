package com.tradetested.quarkus.intellij.testsocket.configuration

import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.junit2.configuration.JUnitTestKindFragment
import com.intellij.execution.ui.CommonJavaFragments
import com.intellij.execution.ui.ConfigurationModuleSelector
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ui.layout.panel
import javax.swing.JPanel

class ContinuousTestSettingsEditor(runConfiguration: ContinuousTestRunConfiguration) :
    RunConfigurationFragmentedEditor<ContinuousTestRunConfiguration>(runConfiguration, JavaRunConfigurationExtensionManager.instance) {

    override fun createRunFragments(): MutableList<SettingsEditorFragment<ContinuousTestRunConfiguration, *>> {
        val fragments = mutableListOf<SettingsEditorFragment<ContinuousTestRunConfiguration, *>>()
        val moduleClasspath = CommonJavaFragments.moduleClasspath<ContinuousTestRunConfiguration>()
        val moduleSelector = ConfigurationModuleSelector(project, moduleClasspath.component())
        fragments.add(WebsocketEndpointFragment())

        fragments.add(moduleClasspath)
        @Suppress("UNCHECKED_CAST")
        fragments.add(JUnitTestKindFragment(project, moduleSelector) as SettingsEditorFragment<ContinuousTestRunConfiguration, *>)
        return fragments
    }
}

class WebsocketEndpointFragment() :
    SettingsEditorFragment<ContinuousTestRunConfiguration, JPanel>(
        "quarkus.ct.endpoint",
        null,
        null,
        null,
        90,
        null,
        null,
        { true }) {

    private val component = panel {}

    private var endpoint: String = ""

    override fun applyEditorTo(s: ContinuousTestRunConfiguration) {
        s.endpoint = endpoint
    }

    override fun resetEditorFrom(s: ContinuousTestRunConfiguration) {
        endpoint = s.endpoint
    }

    init {
        this.myComponent = panel {
            row("Websocket Endpoint", separated = true) {
                textField({ endpoint }, { value -> endpoint = value})
            }
        }
    }
}