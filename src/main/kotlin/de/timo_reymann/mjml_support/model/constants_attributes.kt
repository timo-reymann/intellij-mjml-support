package de.timo_reymann.mjml_support.model

import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType

val ATTRIBUTE_CSS_CLASS = MjmlAttributeInformation(
    "css-class",
    MjmlAttributeType.CLASS,
    "class name, added to the root HTML element created"
)

val ATTRIBUTE_BACKGROUND_COLOR = MjmlAttributeInformation(
    "background-color",
    MjmlAttributeType.COLOR,
    "background color"
)

val ATTRIBUTE_FONT_FAMILY = MjmlAttributeInformation(
    "font-family",
    MjmlAttributeType.STRING,
    "font",
    "Ubuntu,Helvicta,Arial,sans-serif"
)

val ATTRIBUTE_ICON_ALIGN = MjmlAttributeInformation(
    "icon-align",
    MjmlAttributeType.STRING,
    "icon alignment",
    "middle"
)

val ATTRIBUTE_ICON_HEIGHT = MjmlAttributeInformation(
    "icon-height",
    MjmlAttributeType.PIXEL,
    "icon height"
)

val ATTRIBUTE_ICON_WIDTH = MjmlAttributeInformation(
    "icon-width",
    MjmlAttributeType.PIXEL,
    "icon width"
)

val ATTRIBUTE_ICON_POSITION = MjmlAttributeInformation(
    "icon-position",
    MjmlAttributeType.STRING,
    "display icon left or right",
    "right"
)

val ATTRIBUTE_ICON_UNWRAPPED_ALT = MjmlAttributeInformation(
    "icon-unwrapped-alt",
    MjmlAttributeType.STRING,
    "alt text when accordion is unwrapped",
    "-"
)

val ATTRIBUTE_ICON_UNWRAPPED_URL = MjmlAttributeInformation(
    "icon-unwrapped-url",
    MjmlAttributeType.STRING,
    "icon when accordion is unwrapped",
    "https://i.imgur.com/bIXv1bk.png"
)

val ATTRIBUTE_ICON_WRAPPED_ALT = MjmlAttributeInformation(
    "icon-wrapped-alt",
    MjmlAttributeType.STRING,
    "alt text when accordion is wrapped"
)

val ATTRIBUTE_ICON_WRAPPED_URL = MjmlAttributeInformation(
    "icon-wrapped-url",
    MjmlAttributeType.STRING,
    "icon when accordion is wrapped",
    "https://i.imgur.com/bIXv1bk.png"
)

val ATTRIBUTE_COLOR = MjmlAttributeInformation(
    "color",
    MjmlAttributeType.COLOR,
    "text color"
)

val ATTRIBUTE_FONT_SIZE = MjmlAttributeInformation(
    "font-size",
    MjmlAttributeType.PIXEL,
    "font size",
    "13px"
)

val ATTRIBUTE_FONT_STYLE = MjmlAttributeInformation(
    "font-style",
    MjmlAttributeType.STRING,
    "font style"
)

val ATTRIBUTE_FONT_WEIGHT = MjmlAttributeInformation(
    "font-weight",
    MjmlAttributeType.STRING,
    "text thickness",
    "normal"
)

val ATTRIBUTE_PADDING_BOTTOM = MjmlAttributeInformation(
    "padding-bottom",
    MjmlAttributeType.PIXEL,
    "padding bottom"
)

val ATTRIBUTE_PADDING_LEFT = MjmlAttributeInformation(
    "padding-left",
    MjmlAttributeType.PIXEL,
    "padding left"
)

val ATTRIBUTE_PADDING_RIGHT = MjmlAttributeInformation(
    "padding-right",
    MjmlAttributeType.PIXEL,
    "padding right"
)

val ATTRIBUTE_PADDING_TOP = MjmlAttributeInformation(
    "padding-top",
    MjmlAttributeType.PIXEL,
    "padding top"
)

val ATTRIBUTE_BORDER = MjmlAttributeInformation(
    "border",
    MjmlAttributeType.COMPLEX,
    "css border format"
)

val ATTRIBUTE_BORDER_RADIUS = MjmlAttributeInformation(
    "border-radius",
    MjmlAttributeType.PIXEL,
    "border radius"
)

val ATTRIBUTE_BORDER_BOTTOM = MjmlAttributeInformation(
    "border-bottom",
    MjmlAttributeType.COMPLEX,
    "css border format"
)

val ATTRIBUTE_BORDER_LEFT = MjmlAttributeInformation(
    "border-left",
    MjmlAttributeType.COMPLEX,
    "css border format"
)

val ATTRIBUTE_BORDER_RIGHT = MjmlAttributeInformation(
    "border-right",
    MjmlAttributeType.COMPLEX,
    "css border format"
)

val ATTRIBUTE_BORDER_TOP = MjmlAttributeInformation(
    "border-top",
    MjmlAttributeType.COMPLEX,
    "css border format"
)

val ATTRIBUTE_HEIGHT = MjmlAttributeInformation(
    "height",
    MjmlAttributeType.PIXEL,
    "height"
)

val ATTRIBUTE_WIDTH = MjmlAttributeInformation(
    "width",
    MjmlAttributeType.STRING,
    "width for element"
)

val ATTRIBUTE_ALT = MjmlAttributeInformation(
    "alt",
    MjmlAttributeType.STRING,
    "image description"
)

val ATTRIBUTE_HREF = MjmlAttributeInformation(
    "href",
    MjmlAttributeType.STRING,
    "url to resource"
)

val ATTRIBUTE_REL = MjmlAttributeInformation(
    "rel",
    MjmlAttributeType.STRING,
    "specify the rel attribute"
)

val ATTRIBUTE_TARGET = MjmlAttributeInformation(
    "target",
    MjmlAttributeType.STRING,
    "specify the target attribute for the link"
)

val ATTRIBUTE_TITLE = MjmlAttributeInformation(
    "title",
    MjmlAttributeType.STRING,
    "tooltip & accessiblity"
)

val ATTRIBUTE_CONTAINER_BACKGROUND_COLOR = MjmlAttributeInformation(
    "container-background-color",
    MjmlAttributeType.COLOR,
    "background color of the cell"
)

val ATTRIBUTE_ICON_SIZE = MjmlAttributeInformation(
    "icon-size",
    MjmlAttributeType.COMPLEX,
    "icon size (width and height)",
    "20px"
)

val ATTRIBUTE_ICON_PADDING = MjmlAttributeInformation(
    "icon-padding",
    MjmlAttributeType.PIXEL,
    "padding around the icons",
    "0px"
)

val ATTRIBUTE_BACKGROUND_URL = MjmlAttributeInformation(
    "background-url",
    MjmlAttributeType.URL,
    "absolute background url"
)

val ATTRIBUTE_MJ_CLASS = MjmlAttributeInformation(
    "mj-class",
    MjmlAttributeType.CLASS,
    "mj-class"
)
