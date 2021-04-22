package de.timo_reymann.mjml_support.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

val MJML_FILE_PATTERN = PlatformPatterns.psiFile()
    .withName(StandardPatterns.string().endsWith('.' + MjmlHtmlFileType.INSTANCE.defaultExtension))
