package com.shadowforgedmmo.engine.event

class EventManager {
    fun call(event: Event) {
        TODO()
    }

    fun <E : Event> subscribe(event: E, callback: (E) -> Unit) {
        TODO()
    }
}
