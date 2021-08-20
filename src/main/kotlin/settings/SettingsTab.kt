package settings

import dependencies.App
import DndPlugin
import dependencies.PluginSettingTab
import dependencies.Setting

class SettingsTab(app: App, private val plugin: DndPlugin) : PluginSettingTab(app, plugin) {
    override fun display() {
        val container = containerEl
		// TODO Not sure how to define these functions on container
        js("container.empty();")
        js("container.createEl('h2', { text: 'Dnd Helper Plugin settings.' });")

		// look at this guys code to make this better:
		// https://github.com/mgmeyers/obsidian-kanban/blob/93014c2512507fde9eafd241e8d4368a8dfdf853/src/Settings.ts#L101
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
