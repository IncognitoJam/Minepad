package com.github.incognitojam.minepad

import com.corundumstudio.socketio.SocketIOClient
import java.util.*

/**
 * The Controller class stores information about a controller session such as the player
 * using the controller and the code used to login to this session through the web
 * interface. It also provides helper properties to access the data from the gamepad
 * object which stores the information sent from the web interface to the server
 * about the activity of the user and their controller.
 *
 * The Controller class is designed for Xbox 360 controllers and features all of the
 * buttons and triggers present on this controller, apart from the home button which
 * is not usually passed to the computer.
 */
data class Controller(val player: UUID, val code: String, var client: SocketIOClient? = null, var gamepad: Gamepad = Gamepad()) {

    /**
     * The 'A' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var a = false
        get() = gamepad.FACE_1

    /**
     * The 'B' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var b = false
        get() = gamepad.FACE_2

    /**
     * The 'X' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var x = false
        get() = gamepad.FACE_3

    /**
     * The 'Y' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var y = false
        get() = gamepad.FACE_4

    /**
     * The 'LB' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var lb = false
        get() = gamepad.LEFT_TOP_SHOULDER

    /**
     * The 'RB' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var rb = false
        get() = gamepad.RIGHT_TOP_SHOULDER

    /**
     * The 'LT' trigger on the Xbox 360 controller
     *
     * Values: 0.0 when unpressed, 1.0 when fully pressed
     */
    var lt = .0
        get() = gamepad.LEFT_BOTTOM_SHOULDER

    /**
     * The 'RT' trigger on the Xbox 360 controller
     *
     * Values: 0.0 when unpressed, 1.0 when fully pressed
     */
    var rt = .0
        get() = gamepad.RIGHT_BOTTOM_SHOULDER

    /**
     * The 'Home' button on the Xbox 360 controller
     *
     * @value false when unpressed, true when pressed
     *
     * Note: This button may not always be recorded correctly due to
     * interference from the Windows 10 Game DVR program which will
     * intercept this button being pressed. To resolve this you must
     * disable "Open Game Bar using Xbox on a controller" in the Game
     * DVR settings in the Xbox app.
     */
    var home = false
        get() = gamepad.HOME

    /**
     * The 'BACK' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var back = false
        get() = gamepad.SELECT_BACK

    /**
     * The 'START' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var start = false
        get() = gamepad.START_FORWARD

    /**
     * The 'Left Stick' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var ls = false
        get() = gamepad.LEFT_STICK

    /**
     * The 'Right Stick' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var rs = false
        get() = gamepad.RIGHT_STICK

    /**
     * The 'DPAD UP' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var up = false
        get() = gamepad.DPAD_UP

    /**
     * The 'DPAD DOWN' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var down = false
        get() = gamepad.DPAD_DOWN

    /**
     * The 'DPAD LEFT' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var left = false
        get() = gamepad.DPAD_LEFT

    /**
     * The 'DPAD RIGHT' button on the Xbox 360 controller
     *
     * Values: false when unpressed, true when pressed
     */
    var right = false
        get() = gamepad.DPAD_RIGHT

    /**
     * The left analog stick x-axis on the Xbox 360 controller
     *
     * Values: from -1.0 when fully left to 1.0 when fully right
     */
    var leftX = .0
        get() = gamepad.LEFT_STICK_X

    /**
     * The left analog stick y-axis on the Xbox 360 controller
     *
     * Values: from -1.0 when fully down to 1.0 when fully up
     */
    var leftY = .0
        get() = -gamepad.LEFT_STICK_Y

    /**
     * The right analog stick x-axis on the Xbox 360 controller
     *
     * Values: from -1.0 when fully left to 1.0 when fully right
     */
    var rightX = .0
        get() = gamepad.RIGHT_STICK_X

    /**
     * The right analog stick y-axis on the Xbox 360 controller
     *
     * Values: from -1.0 when fully down to 1.0 when fully up
     */
    var rightY = .0
        get() = -gamepad.RIGHT_STICK_Y

    override fun toString(): String {
        return "Controller(player=$player, code='$code', client=$client, gamepad=$gamepad)"
    }

    companion object {
        val BUTTONS = listOf("FACE_1", "FACE_2", "FACE_3", "FACE_4", "LEFT_TOP_SHOULDER", "RIGHT_TOP_SHOULDER", "HOME", "SELECT_BACK",
                "START_FORWARD", "LEFT_STICK", "RIGHT_STICK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT")
        val TRIGGERS = listOf("LEFT_BOTTOM_SHOULDER", "RIGHT_BOTTOM_SHOULDER")
        val AXES = listOf("LEFT_STICK_X", "LEFT_STICK_Y", "RIGHT_STICK_X", "RIGHT_STICK_Y")
    }

}

class Gamepad {
    var FACE_1 = false // A
    var FACE_2 = false // B
    var FACE_3 = false // X
    var FACE_4 = false // Y

    var LEFT_TOP_SHOULDER = false // LB
    var RIGHT_TOP_SHOULDER = false // RB
    var LEFT_BOTTOM_SHOULDER = .0 // LT
    var RIGHT_BOTTOM_SHOULDER = .0 // RT

    var HOME = false // home
    var SELECT_BACK = false // BACK
    var START_FORWARD = false // START

    var LEFT_STICK = false // Left Analog
    var RIGHT_STICK = false // Right Analog

    var DPAD_UP = false // UP
    var DPAD_DOWN = false // DOWN
    var DPAD_LEFT = false // LEFT
    var DPAD_RIGHT = false // RIGHT

    var LEFT_STICK_X = .0 // Left Analog X
    var LEFT_STICK_Y = .0 // Left Analog Y
    var RIGHT_STICK_X = .0 // Right Analog X
    var RIGHT_STICK_Y = .0 // Right Analog Y

    override fun toString(): String {
        return "Gamepad(FACE_1=$FACE_1, FACE_2=$FACE_2, FACE_3=$FACE_3, FACE_4=$FACE_4, LEFT_TOP_SHOULDER=$LEFT_TOP_SHOULDER, RIGHT_TOP_SHOULDER=$RIGHT_TOP_SHOULDER, LEFT_BOTTOM_SHOULDER=$LEFT_BOTTOM_SHOULDER, RIGHT_BOTTOM_SHOULDER=$RIGHT_BOTTOM_SHOULDER, SELECT_BACK=$SELECT_BACK, START_FORWARD=$START_FORWARD, LEFT_STICK=$LEFT_STICK, RIGHT_STICK=$RIGHT_STICK, DPAD_UP=$DPAD_UP, DPAD_DOWN=$DPAD_DOWN, DPAD_LEFT=$DPAD_LEFT, DPAD_RIGHT=$DPAD_RIGHT, HOME=$HOME, LEFT_STICK_X=$LEFT_STICK_X, LEFT_STICK_Y=$LEFT_STICK_Y, RIGHT_STICK_X=$RIGHT_STICK_X, RIGHT_STICK_Y=$RIGHT_STICK_Y)"
    }

}

data class ControlEvent(val control: String, val value: Double)