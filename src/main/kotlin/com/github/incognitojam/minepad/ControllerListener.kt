package com.github.incognitojam.minepad;

interface ControllerListener {
    fun onConnect(controller: Controller)
    fun onButtonInteract(controller: Controller, button: String, value: Boolean)
    fun onTriggerInteract(controller: Controller, trigger: String, value: Double)
    fun onAxesInteract(controller: Controller, axes: String, value: Double)
    fun onDisconnect(controller: Controller)
}