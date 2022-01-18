## Description

This plugin helps you automatically import information into your campaign obsidian notebooks.
This way you can quickly expand your world with existing information and then 
adapt it as needed for your campaign.

So far supported:

- [Kassoon NPC Generator](https://www.kassoon.com/dnd/npc-generator/): The plugin imports the NPC and
  the following attributes if present: *Occupation, Race, Gender, Voice, Description, Ideals, Flaws, 
  Bonds, Personality, Motivation* and *History*
- [Kassoon Town Generator](https://www.kassoon.com/dnd/town-generator/): The plugin imports the Town.
  Any referenced NPCs are imported as described above and linked. 
  Furthermore any Services described in the town are generated to separate files. The following
  attributes are imported if present: *Population, Size, Demographics, Wealth, Description, Defenses
  Services* and *Organizations* 

## Build and run

### CORS proxy

As obsidian is an electron app, requests to external websites are subject to CORS checking. 
Therefore, this plugin needs a local CORS proxy to access the generator on https://kassoon.com. 
Run the following steps to set this up:
- Install the local cors proxy: `npm install -g local-cors-proxy`
- Run it for https://kassoon.com: `lcp --proxyUrl https://www.kassoon.com`
  This should print a few lines, one of them saying `PORT: 8010`. If the port is a different one, 
  the plugin will not work. Try freeing the port on your computer for the plugin to work.

### Running the plugin

Create a `local.properties` file and add the key `obsidianPluginFolderPath` with the path to the 
plugin folder in your obsidian vault. For example if my vault is `C:/Users/someone/my_vault` then 
add the following to the `local.properties` file: 
`obsidianPluginFolderPath=C:/Users/someone/my_vault/.obsidian/plugins`

Then run `gradle copyToObsidianVault` and the plugin will be built and copied to your vault. 
Any missing folders on the path will be created. Then open obsidian and in the settings under 
community plugins activate the plugin.

Deploying and redeploying the plugin requires restarting obsidian to detect the changes.
