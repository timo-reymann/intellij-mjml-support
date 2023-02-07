package de.timo_reymann.mjml_support.util

import com.intellij.notification.*
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import java.util.*

object MessageBusUtil {
    val NOTIFICATION_GROUP = MjmlBundle.message("notification_group")

    fun showMessage(type: NotificationType, title: String, message: String): Notification {
        val notification = Notification(NOTIFICATION_GROUP + " (" + capitalize(type.name) + ")", title, message, type)
        Notifications.Bus.notify(notification)
        return notification
    }

    private fun capitalize(str: String?): String? {
        return if (str == null || str.isEmpty()) {
            str
        } else str.substring(0, 1).uppercase(Locale.getDefault()) + str.lowercase(Locale.getDefault()).substring(1)
    }
}
