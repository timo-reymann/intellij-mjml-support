package de.timo_reymann.mjml_support.editor.ui

import com.intellij.util.xmlb.annotations.Attribute
import javax.swing.JComponent

abstract class MjmlHtmlPanelProvider {
    abstract fun createHtmlPanel(): MjmlJCEFHtmlPanel
    abstract fun isAvailable(): AvailabilityInfo
    abstract fun getProviderInfo(): ProviderInfo?

    class ProviderInfo(name: String, className: String) {
        @Attribute("name")
        var name: String = name
            private set

        @Attribute("className")
        var className: String = className
            private set

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val info = other as ProviderInfo
            if (name != info.name) return false
            return className == info.className
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + className.hashCode()
            return result
        }

        override fun toString(): String = name
    }

    abstract class AvailabilityInfo {
        abstract fun checkAvailability(parentComponent: JComponent): Boolean

        companion object {
            val AVAILABLE: AvailabilityInfo = object : AvailabilityInfo() {
                override fun checkAvailability(parentComponent: JComponent): Boolean {
                    return true
                }
            }
            val UNAVAILABLE: AvailabilityInfo = object : AvailabilityInfo() {
                override fun checkAvailability(parentComponent: JComponent): Boolean {
                    return false
                }
            }
        }
    }
}
