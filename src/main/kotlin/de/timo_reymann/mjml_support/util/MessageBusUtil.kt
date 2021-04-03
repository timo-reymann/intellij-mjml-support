package de.timo_reymann.mjml_support.util

import com.intellij.notification.*
import de.timo_reymann.mjml_support.bundle.MjmlBundle

object MessageBusUtil {
    private val GROUP_ID = MjmlBundle.message("notification_group")
    val NOTIFICATION_GROUP: NotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(GROUP_ID)

    fun showMessage(type: NotificationType, title: String, message: String): Notification {
        val notification = Notification(GROUP_ID + " (" + capitalize(type.name) + ")", title, message, type)
        Notifications.Bus.notify(notification)
        return notification
    }

    private fun capitalize(str: String?): String? {
        return if (str == null || str.isEmpty()) {
            str
        } else str.substring(0, 1).toUpperCase() + str.toLowerCase().substring(1)
    }
}
