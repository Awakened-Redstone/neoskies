[//]: # (A skyblock mod with customizable islands, advanced island protection and support for expansion mods.)

[//]: # ()
[//]: # (NeoSkies provides it's own API and registries for other mods to add content and features and integrate with the island system.)

[//]: # ()
[//]: # (Each island have their own settings, those settings allow the owner to decide how other players can interact with the island, such as allowing interaction with doors, redstone, containers, etc.)

[//]: # ()
[//]: # (The mod uses the Common Economy API for currency, each island has it's account, that is shared between all island members.)

[//]: # ()
[//]: # (The mod is still in development and will change over time.)

[//]: # ()
[//]: # (This mod is a fork of Skylands, and the current island system and island templates are &#40;mostly&#41; from the original mod.)

[//]: # ()
[//]: # (Please provide feedback on the [discord server]&#40;https://discord.gg/MTqsjwMpN2&#41;, it helps a lot to continue developing the mod)

## About
<!-- modrinth_exclude.start -->
<img align="right" width="128" src="src/main/resources/assets/neoskies/icon.png">
<!-- modrinth_exclude.end -->

NeoSkies is a skyblock Minecraft mod, with island settings, teams, island upgrades, dedicated worlds for each island, economy integration (with Common Economy API) and more!
This is a server-side mod, fully compatible with **vanilla** clients

[//]: # ([![Modrinth Page]&#40;https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/cozy/available/modrinth_64h.png&#41;]&#40;https://modrinth.com/mod/neoskies&#41;)

### User Commands

- **/sb home** -> Open the mod GUI
- **/sb hub** -> Teleport to the Hub.
- **/sb create** -> Creates an Island.
- **/sb home** -> Teleport to your Island.
- **/sb visit <player>** -> Visit someone's Island.
- **/sb home <player>** -> Teleport to an Island you are member of.
- **/sb members invite <player>** -> Invite player to your Island.
- **/sb members remove <player>** -> Remove player from your Island.
- **/sb accept <player>** -> Accept Island join invite request.
- **/sb kick <player>** -> Kick player from your Island.
- **/sb ban <player>** -> Ban player from visiting your Island.
- **/sb unban <player>** -> Unban player to allow visiting your Island.
- **/sb delete** -> Deletes your Island.
- **/sb settings set-spawn-pos** -> Changes home position.
- **/sb settings set-visits-pos** -> Changes visits position.
- **/sb settings toggle-visits** -> Enables/disables ability to visit your Island.
- **/sb help** -> Sends this list.

### Admin Commands

- **/sba hub \(pos|protection)** -> Modify the hub settings.
- **/sba delete-island \<player>** -> Delete a player's island
- **/sba settings** -> Change the mod settings (WIP)
- **/sba balance <island> \(get|set|add|remove)** -> Modify an island's balance
- **/sba island-data \(find|get)** -> Get the data of an island
- **/sba modify <island> \(size|gamerule)** -> Modify an island property
- **/sba reload** -> Reload the mod data


This mod is a fork of [Skylands](https://modrinth.com/mod/skylands), made to have more feature parity with skyblock plugins like [bSkyblock](https://modrinth.com/plugin/bskyblock) and [ASkyblock](https://www.spigotmc.org/resources/askyblock.1220/)
