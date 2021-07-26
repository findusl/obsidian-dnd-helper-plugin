package settings

import App
import DndPlugin
import PluginSettingTab
import Setting

class SettingsTab(app: App, private val plugin: DndPlugin) : PluginSettingTab(app, plugin) {
    override fun display() {
        val container = containerEl
		// TODO Not sure how to define these functions on container
        js("container.empty();")
        js("container.createEl('h2', { text: 'Dnd Generator Plugin settings.' });")

		Setting(container)
			.setName("Path for town notes")
			.setDesc("Under what path do you want to save your towns? Each town will have a separate folder under that path.")
			.addText { text -> text
				.setPlaceholder("Places/Towns")
				.setValue(plugin.settings.townBasePath)
				.onChange {
					plugin.settings = plugin.settings.copy(townBasePath = it)
				}
			}

		Setting(container)
			.setName("Path for character notes")
			.setDesc("Under what path do you want to save your characters?")
			.addText { text -> text
				.setPlaceholder("NPCs")
				.setValue(plugin.settings.characterBasePath)
				.onChange {
					plugin.settings = plugin.settings.copy(characterBasePath = it)
				}
			}
    }

	private fun Settings.copy(
		townBasePath: String = this.townBasePath,
		characterBasePath: String = this.characterBasePath
	): Settings {
		return Settings(townBasePath, characterBasePath)
	}
}
