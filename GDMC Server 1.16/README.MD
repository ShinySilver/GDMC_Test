# Requirements
  Java (version?)
  - A copy of Minecraft

# Install
  - Make sure you have Java installed.
  - Download BuildTool here: https://hub.spigotmc.org/jenkins/job/BuildTools/ Move it to a temporary folder.
  - Then run "java -jar BuildTool.jar --rev 1.16.5" to compile the latest version of spigot for 1.16.5. If it doesn't work, there is a website that offer precompiled jar: https://getbukkit.org/download/spigot
  - At last, move the newly generated spigot-1.16.5.jar to this folder.
  - If you want to use a specific world, place it in this folder, renammed "world". This "world" folder must contains a folder region, and be a valid minecraft save.
  - You can then run the server with "java -jar spigot-1.16.5.jar nogui" once to generate eula.txt, which you have to open and accept, then a second time for the server to start.
  - You can join the server by connecting to 'localhost' in the multiplayer menu.
  - To generate a village in a bounding box, move with your character and type "village <PlayerName> radiusX radiusZ" in the server console. You will be notified when the generation has finished.
  - Adding a command that does not require a player to be connected is trivial, but not a priority.
