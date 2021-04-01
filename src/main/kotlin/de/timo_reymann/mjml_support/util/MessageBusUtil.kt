package de.timo_reymann.mjml_support.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.Notifications

import com.intellij.notification.NotificationType

object MessageBusUtil {
    val NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("MJML Support")
    private const val GROUP_ID = "MJML Support"

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
