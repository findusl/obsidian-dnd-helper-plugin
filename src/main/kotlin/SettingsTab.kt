class SettingsTab(app: App, private val plugin: DndPlugin) : PluginSettingTab(app, plugin) {
    override fun display() {
        val container = containerEl
		// TODO Not sure how to define these functions on container
        js("container.empty();")
        js("container.createEl('h2', { text: 'Dnd Generator Plugin settings.' });")

		Setting(container)
			.setName("Path for town notes")
			.setDesc("Under what path do you want to save your towns? Each town will have a seperate folder under that path.")
			.addText {text -> text
				.setPlaceholder("places/towns")
				.setValue(plugin.settings.townBasePath)
				.onChange {
                    plugin.settings.townBasePath = it
                }
			}
    }
}
