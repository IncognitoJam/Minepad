package com.github.incognitojam.minepad

import com.connorlinfoot.bountifulapi.BountifulAPI
import com.google.gson.Gson
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ResourceHandler
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.net.URL
import java.text.DecimalFormat
import java.util.logging.Level
import java.util.logging.Logger

data class Configuration(val hostname: String, val interfacePort: Int, val socketPort: Int)

class Minepad : JavaPlugin() {

    private val format = DecimalFormat("#.00")
    private var server: Server? = null
    private val config: Configuration
    private val manager: ControllerManager

    init {
        val gson = Gson()

        val configFile = File(dataFolder, "config.json")
        if (!configFile.exists()) saveResource("config.json", false)

        var fileReader: FileReader? = null
        try {
            fileReader = FileReader(configFile)
        } catch (e: FileNotFoundException) {
            Minepad.fatal("Could not find config.json! Disabling plugin.")
            isEnabled = false
        }
        config = gson.fromJson(fileReader, Configuration::class.java)
        manager = ControllerManager(config.hostname, config.socketPort, config.interfacePort)
    }

    override fun onEnable() {
        listOf("index.html", "gamepad.min.js").forEach {
            File(dataFolder, it).delete()
            saveResource(it, true)
        }

        server = Server(config.interfacePort)
        val resourceHandler = ResourceHandler()
        resourceHandler.isDirectoriesListed = false
        resourceHandler.welcomeFiles = arrayOf("index.html")
        resourceHandler.resourceBase = dataFolder.absolutePath

        server?.handler = resourceHandler
        server?.start()

        manager.registerListener(object : ControllerListener {
            override fun onConnect(controller: Controller) {
                Minepad.debug("Controller connected, $controller")
                val player = Bukkit.getPlayer(controller.player) ?: return
                sendActionBar(player, "§aController connected")
            }

            override fun onButtonInteract(controller: Controller, button: String, value: Boolean) {
                Minepad.debug("onButtonInteract(controller: $controller, button: '$button', value: $value")
                val player = Bukkit.getPlayer(controller.player) ?: return

                var velocity = player.velocity
                if (controller.a && player.isOnGround) {
                    velocity = velocity.setY(0.5)
                }

                player.velocity = velocity
            }

            override fun onTriggerInteract(controller: Controller, trigger: String, value: Double) {
                Minepad.debug("onTriggerInteract(controller: $controller, trigger: '$trigger', value: $value")
            }

            override fun onAxesInteract(controller: Controller, axes: String, value: Double) {
                Minepad.debug("onAxesInteract(controller: $controller, axes: '$axes', value: $value")
            }

            override fun onDisconnect(controller: Controller) {
                Minepad.debug("Controller disconnected, $controller")
                val player = Bukkit.getPlayer(controller.player) ?: return
                sendActionBar(player, "§cController disconnected")
            }
        })

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            for (player in Bukkit.getOnlinePlayers()) {
                var velocity = player.velocity
                val location = player.location
                val direction = location.direction

                manager.getController(player.uniqueId)?.let { controller ->
                    val motion = direction.multiply(controller.leftY * 0.25F)
                    velocity = velocity.add(motion)
                }

                val speed = "Speed: ${format.format(velocity.length())}ms^-1"
                sendActionBar(player, speed)

                player.velocity = velocity
            }
        }, 10L, 1L)
    }

    override fun onDisable() {
        manager.disable()
        server?.stop()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        when (cmd.name.toLowerCase()) {
            "controller" -> {
                if (args.isEmpty()) {
                    sender.sendMessage("Expected arguments!")
                    return false
                } else if (sender is Player) {
                    when (args[0]) {
                        "connect" -> {
                            val (controller, existing) = manager.createController(sender)
                            if (existing) {
                                sender.sendMessage(TITLE + "You already have a controller!")
                            }
                            sendControllerMessage(sender, manager.generateURL(controller))
                        }

                        "disconnect" -> {
                            val controller = manager.getController(sender.uniqueId)
                            if (controller == null) {
                                sender.sendMessage(TITLE + "You have not connected a controller!")
                            } else {
                                manager.removeController(controller)
                            }
                        }
                    }
                } else {
                    sender.sendMessage("You must be a player to use controllers.")
                }
            }
        }
        return true
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val player = event.player
        val controller = manager.getController(player.uniqueId) ?: return
        manager.removeController(controller, reason = "Player left the game")
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player
        val (controller, _) = manager.createController(player)
        sendControllerMessage(player, manager.generateURL(controller))
    }

    companion object {
        private val LOG = Logger.getLogger("Minepad").apply {
            level = Level.INFO
        }

        const val TITLE = "§7[§bMinePad§7]§f "
        const val MESSAGE_DURATION = 3000

        private fun sendActionBar(player: Player, message: String, duration: Int = MESSAGE_DURATION) {
            BountifulAPI.sendActionBar(player, message, duration)
        }

        private fun sendControllerMessage(player: Player, url: URL) {
            val message = TextComponent(TITLE)
            message.addExtra("Connect your controller by clicking ")
            val link = TextComponent("here")
            link.color = ChatColor.YELLOW
            link.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url.toString())
            link.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Go to the web interface").create())
            message.addExtra(link)
            message.addExtra(".")
            player.spigot().sendMessage(message)
        }

        fun debug(message: String) {
            LOG.fine(TITLE + message)
        }

        fun info(message: String) {
            LOG.info(TITLE + message)
        }

        fun warn(message: String) {
            LOG.warning(TITLE + message)
        }

        fun fatal(message: String) {
            LOG.severe(TITLE + message)
        }
    }

}