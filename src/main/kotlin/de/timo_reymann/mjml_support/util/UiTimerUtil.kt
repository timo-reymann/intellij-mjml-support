package de.timo_reymann.mjml_support.util

import javax.swing.Timer

object UiTimerUtil {
    fun singleExecutionAfter(seconds : Int, action : () -> Unit) {
        val timer = Timer(0) { action() }
        timer.isRepeats = false
        timer.initialDelay = seconds * 1_000
        timer.start()
    }
}
