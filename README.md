# Minepad
Use game controllers to interact with multiplayer Minecraft games

Minepad hosts a web server which players can visit in their browser. The client useragent communicates with the server to send
input from the player's gamepad (using the HTML5 Gamepad API) over a websocket to the server. The plugin on the server can then
control minigames or the player's movement in response to the controller.

Written in Kotlin
