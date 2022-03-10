# UnitedCore

**UnitedCore** is a custom-made plugin for UnitedLands, which has most of our custom made features spread out over multiple 'nodes'. Info detailing each node is provided below. The current working nodes are:
  1. UnitedPvP
  2. UnitedChat
  3. UnitedItems 

## UnitedPvP
A custom-made pvp manager that handles the pvp status of players. It also has a few additions, such as force-enabling pvp for outlaws when they enter an outlawed town. 

### Commands
- `/pvp on` - Sets your pvp status to ON, meaning you can engage in combat with other players
- `/pvp off` - Sets your pvp status to off, meaning you can no longer engage in combat
- `/pvp status` - Returns your current pvp status (Either enabled, or disabled) 
- `/pvp on/off <player>` - Admin command, force sets the pvp status of <player>

### Permissions
  - `united.pvp.toggle` Gives permissions to use the `/pvp on/off` command
  - `united.pvp.cooldown.ignore` Allows you to ignore the pvp toggle command cooldown (120 seconds by default)
  - `united.pvp.admin` Allows you to set the status of other players. 
  
### Placeholders
  - `%unitedpvp_status%` - Returns either `⚔` if the pvp is enabled, or `🛡` if it's disabled
  - `%unitedpvp_status-string%` - Returns either `enabled` or `disabled`

## UnitedChat
  Used to manage most custom chat aspects. Most notably, the auto broadcast messages, gradientchat, player pings, and join MOTD 
  
  ### Commands
  #### Gradient Commands
  - `/gradient on/off` - Toggles your gradient on/off
  - `/gradient [preset]` - Sets your current gradient to on of the pre-made presets 
  - `/gradient [Hex 1] [Hex 2]` - Sets your gradient to a custom gradient with start and end hex codes
  #### ClearChat
  - `/cc` - Clears the current chat.
  
  ### Permissions
  - `united.chat.gradient` - Gives access to the primary /gradient command
  - `united.chat.gradient.<preset>` - Gives access to a specific preset defined in the config.yml
  - `united.chat.gradient.all` - Gives access to all presets, and the ability to set custom hex codes
  - `united.chat.admin` - Gives access to the `/cc` command 

  ## UnitedItems
  A plugin which handles custom items, trees, saplings, and more! Info about all the custom items can be found [here](https://github.com/UnitedLands/community/wiki/Custom-Items), this mainly details the plugin usage
  
  
  ### Commands
  #### Custom Item Commands
  - `/customitem help` - Shows a help message
  - `/customitem list` - Lists all available custom items
  - `/customitem listfqn` - Lists all custom items with their colored names
  - `/customitem give <name>` - Gives an item depending on how similar the name is to an existing item.
  #### Tree Commands
  - `/tree help` - Shows a help message
  - `/tree seed <name>` - Gives a seed/sapling of the requested tree
  - `/tree info <name>` - Gives information for the specified tree
  - `/tree list` - Lists all available trees 
  - `/tree give <player> <name>` - Gives the player a tree seed
  
  ### Permissions
  - `united.custom.admin` - Gives access to all commands above 
