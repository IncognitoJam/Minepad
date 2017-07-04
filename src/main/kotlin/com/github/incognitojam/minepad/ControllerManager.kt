package com.github.incognitojam.minepad

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.bukkit.entity.Player
import java.net.URL
import java.security.SecureRandom
import java.util.*

class ControllerManager(private val hostname: String, socketPort: Int, private val interfacePort: Int) {

    private val server: SocketIOServer
    private val controllerMap = HashMap<UUID, Controller>()
    private var listeners: List<ControllerListener> = ArrayList()

    init {
        val configuration = Configuration()
        configuration.port = socketPort

        server = SocketIOServer(configuration)
        server.addEventListener("validate_code", String::class.java) { client, code, _ ->
            val controller = getController(code)
            val result = controller != null
            if (result) controller?.client = client
            client.sendEvent("validate_code_result", result)
            Minepad.debug("Event \"validate_code\": code: $code, result: $result")
        }
        server.addEventListener("update_state", String::class.java) { client, jsonString, _ ->
            val event = createControlEvent(jsonString) ?: return@addEventListener
            val control = event.control
            val value = event.value
            Minepad.debug("event: update_state, control: $control, value: $value")

            val controller = getController(client) ?: return@addEventListener
            val gamepad = controller.gamepad

            when (control) {
                in Controller.BUTTONS -> {
                    val realValue = value == 1.0
                    val field = gamepad.javaClass.getField(control)
                    field.setBoolean(gamepad, realValue)
                    listeners.forEach { it.onButtonInteract(controller, control, realValue) }
                }

                in Controller.TRIGGERS -> {
                    val field = gamepad.javaClass.getField(control)
                    field.setDouble(gamepad, value)
                    listeners.forEach { it.onTriggerInteract(controller, control, value) }
                }

                in Controller.AXES -> {
                    val field = gamepad.javaClass.getField(control)
                    field.setDouble(gamepad, value)
                    listeners.forEach { it.onAxesInteract(controller, control, value) }
                }

                else -> {
                    Minepad.warn("Unknown control: $control")
                }
            }

            controller.gamepad = gamepad
        }
        server.addEventListener("gamepad_state", Boolean::class.java) { client, state, _ ->
            val controller = getController(client) ?: return@addEventListener
            when (state) {
                true -> listeners.forEach { it.onConnect(controller) }
                false -> listeners.forEach { it.onDisconnect(controller) }
            }
            Minepad.debug("Event \"gamepad_state\": state: $state")
        }
        server.addDisconnectListener { client ->
            getController(client)?.let { controller -> listeners.forEach { it.onDisconnect(controller) } }
        }
        server.startAsync()
    }

    /**
     * Register a listener to receive events about attached controllers
     */
    fun registerListener(listener: ControllerListener) {
        listeners += listener
    }

    /**
     * Unregister a listener from receiving events about attached controllers
     */
    fun unregisterListener(listener: ControllerListener) {
        listeners -= listener
    }

    fun disable() {
        server.stop()
    }

    val controllers
        get() = controllerMap.values

    /**
     * Retrieve the {@link Controller} object with this unique code.
     *
     * @param code The unique code for this controller object
     * @return Returns the Controller with this code, or null if it does not exist.
     */
    fun getController(code: String): Controller? = controllers.firstOrNull { it.code == code }

    /**
     * Retrieve the {@link Controller} object for this player.
     *
     * @param playerId The unique id for this player
     * @return Returns the Controller for this player, or null if it does not exist.
     */
    fun getController(playerId: UUID): Controller? = controllers.firstOrNull { it.player == playerId }

    private fun getController(client: SocketIOClient): Controller? = controllers.firstOrNull { it.client == client }

    /**
     * Create a new {@link Controller} for this player
     *
     * @param player The player to create the new controller for.
     * @return Returns a {@link Pair} which contains both the Controller object as well as a boolean value signifying whether or not the
     * controller already existed previously or if it was newly created. A false value signifies that the controller is new, and a true
     * value signifies that the controller had already existed.
     */
    fun createController(player: Player): Pair<Controller, Boolean> {
        val controller = getController(player.uniqueId)
        if (controller == null) {
            val code = randomCode(6)
            val newController = Controller(player.uniqueId, code)
            controllerMap.put(player.uniqueId, newController)
            return Pair(newController, false)
        }
        return Pair(controller, true)
    }

    /**
     * Generate a URL for the player to use to connect their controller using the web interface.
     *
     * @param controller The controller object which the player is using and wishes to connect
     * @return Returns the URL which the player should connect to
     */
    fun generateURL(controller: Controller): URL {
        return URL("http://$hostname:$interfacePort/?code=${controller.code}")
    }

    /**
     * Remove and disconnect a controller from the Minepad system.
     *
     * @param controller The controller to be removed from the system.
     * @param reason The reason shown to the user in the web interface when they are disconnected.
     */
    fun removeController(controller: Controller, reason: String = "Disconnected") {
        controllerMap.remove(controller.player)
        controller.client?.sendEvent("force_disconnect", reason)
        listeners.forEach { it.onDisconnect(controller) }
    }

    private val CODE_ALPHABET = "0123456789BCDFGHJKLMNPQRSTVWXYZ"
    private val random = SecureRandom()
    private val gson = Gson()

    private fun randomCode(length: Int): String {
        val builder = StringBuilder(length)
        var first = true
        var controller: Controller? = null
        var code: String? = null
        while (first || controller != null) {
            first = false
            for (i in 0 until length)
                builder.append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)])
            code = builder.toString()
            controller = getController(code)
        }
        return code as String
    }

    private fun createControlEvent(jsonString: String): ControlEvent? {
        try {
            return gson.fromJson<ControlEvent>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}