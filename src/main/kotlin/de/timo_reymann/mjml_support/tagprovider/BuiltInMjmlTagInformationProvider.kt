package de.timo_reymann.mjml_support.tagprovider

import com.intellij.openapi.project.Project
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider
import de.timo_reymann.mjml_support.model.*

class BuiltInMjmlTagInformationProvider : MjmlTagInformationProvider() {
    private var tags: MutableMap<String, MjmlTagInformation> = mutableMapOf()

    override fun getAll(project: Project): List<MjmlTagInformation> {
        return this.tags.entries
            .map { it.value }
    }

    override fun getByTagName(project: Project, tagName: String): MjmlTagInformation? {
        return tags[tagName]
    }

    private fun register(vararg informations: MjmlTagInformation) {
        informations.forEach {
            tags[it.tagName] = it
        }
    }

    override fun getPriority() = 100

    init {
        register(
            MjmlTagInformation(
                "mjml",
                "A MJML document starts with a <mjml> tag, it can contain only mj-head and mj-body tags. Both have the same purpose of head and body in a HTML document.",
                attributes = arrayOf(
                    MjmlAttributeInformation("lang", MjmlAttributeType.STRING, "used as <html lang=\"\"> attribute"),
                    MjmlAttributeInformation(
                        "owa",
                        MjmlAttributeType.STRING,
                        "if set to \"desktop\", switch force desktop version for older (self-hosted) version of Outlook.com that doesn't support media queries (cf. this issue)"
                    )
                ),
                allowedParentTags = PARENT_NONE
            ),
            MjmlTagInformation(
                "mj-head",
                "mj-head contains head components, related to the document such as style and meta elements (see <a href='https://mjml.io/documentation/#standard-head-components'>head components</a>).",
                allowedParentTags = PARENT_TOP_LEVEL_ONLY
            ),
            MjmlTagInformation(
                "mj-body",
                "This is the starting point of your email.",
                notes = arrayOf(
                    "mj-body replaces the couple mj-body and mj-container of MJML v3."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation("width", MjmlAttributeType.PIXEL, "email's width")
                ),
                allowedParentTags = PARENT_TOP_LEVEL_ONLY
            ),
            MjmlTagInformation(
                "mj-include",
                """
                        The mjml-core package allows you to include external mjml files to build your email template.
                        You can wrap your external mjml files inside the default mjml > mj-body tags to make it easier to preview outside the main template
                        The MJML engine will then replace your included files before starting the rendering process
                    """.trimIndent(),
                notes = arrayOf(
                    "Note that the file must be a file with a `.mjml` extension"
                ),
                attributes = arrayOf(
                    MjmlAttributeInformation("path", MjmlAttributeType.PATH, "path to mjml file that will be included"),
                    MjmlAttributeInformation(
                        "type",
                        MjmlAttributeType.STRING,
                        "file type to include should be css or html"
                    ),
                    MjmlAttributeInformation("css-inline", MjmlAttributeType.STRING, "Inline included css")
                ),
                allowedParentTags = PARENT_ANY
            ),
            MjmlTagInformation(
                "mj-attributes",
                "This tag allows you to modify default attributes on a mj-tag and add mj-class to them.",
                notes = arrayOf(
                    "You can use mj-all to set default attributes for every components inside your MJML document",
                    "Note that the apply order of attributes is: inline attributes, then classes, then default mj-attributes and then defaultMJMLDefinition"
                ),
                allowedParentTags = PARENT_HEAD_ONLY
            ),
            MjmlTagInformation(
                "mj-breakpoint",
                "This tag allows you to control on which breakpoint the layout should go desktop/mobile.",
                attributes = arrayOf(
                    MjmlAttributeInformation("width", MjmlAttributeType.PIXEL, "breakpoint's value")
                ),
                allowedParentTags = PARENT_ANY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-font",
                "This tag allows you to import fonts if used in your MJML document",
                attributes = arrayOf(
                    ATTRIBUTE_HREF,
                    MjmlAttributeInformation("name", MjmlAttributeType.STRING, "name of the font")
                ),
                allowedParentTags = PARENT_ANY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-preview",
                "This tag allows you to set the preview that will be displayed in the inbox of the recipient.",
                allowedParentTags = PARENT_HEAD_ONLY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-style",
                """
                    This tag allows you to set CSS styles that will be applied to the HTML in your MJML document as well as the HTML outputted. 
                    The CSS styles will be added to the head of the rendered HTML by default, but can also be inlined by using the inline="inline" attribute.

                    Here is an example showing the use in combination with the css-class attribute, which is supported by all body components.
                """.trimIndent(),
                attributes = arrayOf(
                    MjmlAttributeInformation("inline", MjmlAttributeType.STRING, "set to inline to inline styles")
                ),
                notes = arrayOf(
                    " Mjml generates multiple html elements from a single mjml element. For optimal flexibility, the `css-class` will be applied to the most outer html element, so if you want to target a specific sub-element with a css selector, you may need to look at the generated html to see which exact selector you need."
                ),
                allowedParentTags = PARENT_HEAD_ONLY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-title",
                "This tag allows you to set the title of an MJML document",
                allowedParentTags = PARENT_HEAD_ONLY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-accordion",
                "mj-accordion is an interactive MJML component to stack content in tabs, so the information is collapsed and only the titles are visible. Readers can interact by clicking on the tabs to reveal the content, providing a great experience on mobile devices where space is scarce.",
                attributes = arrayOf(
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_ICON_ALIGN,
                    ATTRIBUTE_ICON_HEIGHT,
                    ATTRIBUTE_ICON_WIDTH,
                    ATTRIBUTE_ICON_WRAPPED_ALT,
                    ATTRIBUTE_ICON_WRAPPED_URL,
                    ATTRIBUTE_ICON_POSITION,
                    ATTRIBUTE_ICON_UNWRAPPED_ALT,
                    ATTRIBUTE_ICON_UNWRAPPED_URL,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "padding",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP
                ),
                allowedParentTags = listOf("mj-column", "mj-hero")
            ),
            MjmlTagInformation(
                "mj-accordion-element",
                "This component enables you to create a accordion pane",
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_ICON_ALIGN,
                    ATTRIBUTE_ICON_HEIGHT,
                    ATTRIBUTE_ICON_POSITION,
                    ATTRIBUTE_ICON_UNWRAPPED_ALT,
                    ATTRIBUTE_ICON_UNWRAPPED_URL,
                    ATTRIBUTE_ICON_WIDTH,
                    ATTRIBUTE_ICON_WRAPPED_ALT,
                    ATTRIBUTE_ICON_WRAPPED_URL
                ),
                allowedParentTags = listOf("mj-accordion")
            ),
            MjmlTagInformation(
                "mj-accordion-title",
                "This component enables you to add and style a title to your accordion",
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "padding",
                        "16px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP
                ),
                allowedParentTags = listOf("mj-accordion", "mj-accordion-element"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-accordion-text",
                "This component enables you to add and style a text to your accordion",
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "padding",
                        "16px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "1"
                    )
                ),
                allowedParentTags = listOf("mj-accordion-element"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-button",
                "Displays a customizable button.",
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "horizontal alignment",
                        "center"
                    ),
                    MjmlAttributeInformation(
                        "background-color",
                        MjmlAttributeType.COLOR,
                        "button background color",
                        "#414141"
                    ),
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_BORDER_BOTTOM,
                    ATTRIBUTE_BORDER_LEFT,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_BORDER_RIGHT,
                    ATTRIBUTE_BORDER_TOP,
                    ATTRIBUTE_COLOR,
                    MjmlAttributeInformation(
                        "container-background-color",
                        MjmlAttributeType.COLOR,
                        "button container background color"
                    ),
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    ATTRIBUTE_FONT_WEIGHT,
                    ATTRIBUTE_HEIGHT,
                    MjmlAttributeInformation(
                        "href",
                        MjmlAttributeType.URL,
                        "link to be triggered when the button is clicked"
                    ),
                    MjmlAttributeInformation(
                        "inner-padding",
                        MjmlAttributeType.COMPLEX,
                        "inner button padding",
                        "10px 25px"
                    ),
                    MjmlAttributeInformation(
                        "letter-spacing",
                        MjmlAttributeType.PIXEL,
                        "letter spacing"
                    ),
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.COMPLEX,
                        "line height on link",
                        "120%"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "padding",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_REL,
                    ATTRIBUTE_TARGET,
                    MjmlAttributeInformation(
                        "text-align",
                        MjmlAttributeType.STRING,
                        "text align button content",
                        "none"
                    ),
                    MjmlAttributeInformation(
                        "text-decoration",
                        MjmlAttributeType.STRING,
                        "text decoration for button link",
                        "none"
                    ),
                    MjmlAttributeInformation(
                        "text-transform",
                        MjmlAttributeType.STRING,
                        "text transform for button link"
                    ),
                    MjmlAttributeInformation(
                        "vertical-align",
                        MjmlAttributeType.STRING,
                        "vertical alignment",
                        "middle"
                    ),
                    ATTRIBUTE_WIDTH
                ),
                allowedParentTags = listOf("mj-column", "mj-hero"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-carousel",
                """
                    mj-carousel displays a gallery of images or "carousel". Readers can interact by hovering and clicking on thumbnails depending on the email client they use.

                    This component enables you to set the styles of the carousel elements.
                """.trimIndent(),
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "horizontal alignment",
                        "center"
                    ),
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_ICON_WIDTH,
                    MjmlAttributeInformation(
                        "left-icon",
                        MjmlAttributeType.URL,
                        "icon on the left of the main image",
                        "https://mjml.io/assets/img/left-arrow.png"
                    ),
                    MjmlAttributeInformation(
                        "right-icon",
                        MjmlAttributeType.URL,
                        "icon on the right of the main image",
                        "https://mjml.io/assets/img/right-arrow.png"
                    ),
                    MjmlAttributeInformation(
                        "tb-border",
                        MjmlAttributeType.COMPLEX,
                        "border of the thumbnails",
                        "none"
                    ),
                    MjmlAttributeInformation(
                        "tb-border-radius",
                        MjmlAttributeType.PIXEL,
                        "border-radius of the thumbnails",
                        "none"
                    ),
                    MjmlAttributeInformation(
                        "tb-hover-border-color",
                        MjmlAttributeType.COLOR,
                        "css border color of the hovered thumbnail"
                    ),
                    MjmlAttributeInformation(
                        "tb-selected-border-color",
                        MjmlAttributeType.COLOR,
                        "css border color of the selected thumbnail"
                    ),
                    MjmlAttributeInformation(
                        "tb-width",
                        MjmlAttributeType.PIXEL,
                        "thumbnail width"
                    ),
                    MjmlAttributeInformation(
                        "thumbnails",
                        MjmlAttributeType.STRING,
                        "should the thumbnails be visible or not",
                        "hidden"
                    )
                ),
                allowedParentTags = listOf("mj-column", "mj-hero"),
                definedCssClasses = arrayOf(
                    "mj-carousel-main",
                    "mj-carousel-previous",
                    "mj-carousel-previous",
                    "mj-carousel-next",
                    "mj-carousel-thumbnail",
                    "mj-carousel-radio",
                    "mj-carousel-content",
                    "mj-carousel-previous-icons",
                    "mj-carousel-images",
                    "mj-carousel-image",
                    "mj-carousel-next-icons"
                )
            ),
            MjmlTagInformation(
                "mj-carousel-image",
                "This component enables you to add and style the images in the carousel.",
                attributes = arrayOf(
                    ATTRIBUTE_ALT,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_HREF,
                    ATTRIBUTE_REL,
                    MjmlAttributeInformation(
                        "src",
                        MjmlAttributeType.URL,
                        "image source"
                    ),
                    ATTRIBUTE_TARGET,
                    MjmlAttributeInformation(
                        "thumnails-src",
                        MjmlAttributeType.URL,
                        "image source to have a thumbnail different than the image it's linked to"
                    ),
                    ATTRIBUTE_TITLE
                ),
                allowedParentTags = listOf("mj-carousel"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-column",
                """
                    Columns enable you to horizontally organize the content within your sections. They must be located under mj-section tags in order to be considered by the engine. To be responsive, columns are expressed in terms of percentage.

                    Every single column has to contain something because they are responsive containers, and will be vertically stacked on a mobile view. Any standard component, or component that you have defined and registered, can be placed within a column – except mj-column or mj-section elements.
                """.trimIndent(),
                notes = arrayOf(
                    "Columns are meant to be used as a container for your content. They must not be used as offset. Any mj-element included in a column will have a width equivalent to 100% of this column's width.",
                    "Columns cannot be nested into columns, and sections cannot be nested into columns as well."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "inner-background-color",
                        MjmlAttributeType.COLOR,
                        "requires: a padding, inner background color for column"
                    ),
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_BORDER_BOTTOM,
                    ATTRIBUTE_BORDER_LEFT,
                    ATTRIBUTE_BORDER_RIGHT,
                    ATTRIBUTE_BORDER_TOP,
                    ATTRIBUTE_BORDER_RADIUS,
                    MjmlAttributeInformation(
                        "inner-border",
                        MjmlAttributeType.STRING,
                        "css border format"
                    ),
                    MjmlAttributeInformation(
                        "inner-border-bottom",
                        MjmlAttributeType.STRING,
                        "css border format ; requires a padding"
                    ),
                    MjmlAttributeInformation(
                        "inner-border-left",
                        MjmlAttributeType.STRING,
                        "css border format ; requires a padding"
                    ),
                    MjmlAttributeInformation(
                        "inner-border-right",
                        MjmlAttributeType.STRING,
                        "css border format ; requires a padding"
                    ),
                    MjmlAttributeInformation(
                        "inner-border-top",
                        MjmlAttributeType.STRING,
                        "css border format ; requires a padding"
                    ),
                    MjmlAttributeInformation(
                        "inner-border-radius",
                        MjmlAttributeType.PIXEL,
                        "css border format ; requires a padding"
                    ),
                    ATTRIBUTE_WIDTH,
                    MjmlAttributeInformation(
                        "vertical-align",
                        MjmlAttributeType.STRING,
                        "vertical alignment",
                        "top"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters"
                    ),
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS
                ),
                allowedParentTags = listOf("mj-group", "mj-section")
            ),
            MjmlTagInformation(
                "mj-divider",
                "Displays a horizontal divider that can be customized like a HTML border.",
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "border-color",
                        MjmlAttributeType.COLOR,
                        "#000000"
                    ),
                    MjmlAttributeInformation(
                        "border-style",
                        MjmlAttributeType.STRING,
                        "dashed/dotted/solid",
                        "solid"
                    ),
                    MjmlAttributeInformation(
                        "border-width",
                        MjmlAttributeType.PIXEL,
                        "divider's border width",
                        "4px"
                    ),
                    MjmlAttributeInformation(
                        "container-background-color",
                        MjmlAttributeType.COLOR,
                        "inner element background color"
                    ),
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "width",
                        MjmlAttributeType.COMPLEX,
                        "divider width",
                        "100%"
                    )
                ),
                allowedParentTags = listOf("mj-column", "mj-hero"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-group",
                "mj-group allows you to prevent columns from stacking on mobile. To do so, wrap the columns inside a mj-group tag, so they'll stay side by side on mobile.",
                notes = arrayOf(
                    "Column inside a group must have a width in percentage, not in pixel",
                    "You can have both column and group inside a Section",
                    "<b>iOS 9 Issue:</b> If you use a HTML beautifier for MJML output, iOS9 will render your columns inside a mj-group as stacked. On the output HTML, remove the blank space between the two columns inside a mj-group."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_WIDTH,
                    MjmlAttributeInformation(
                        "width",
                        MjmlAttributeType.PIXEL,
                        "group width",
                        "(100 / number of non-raw elements in section)%"
                    ),
                    MjmlAttributeInformation(
                        "vertical-align",
                        MjmlAttributeType.STRING,
                        "middle/top/bottom",
                        "top"
                    ),
                    ATTRIBUTE_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "direction",
                        MjmlAttributeType.STRING,
                        "set the display order of direct children",
                        "ltr"
                    ),
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS
                ),
                allowedParentTags = listOf("mj-section")
            ),
            MjmlTagInformation(
                "mj-hero",
                "The hero you need",
                notes = arrayOf(
                    "The height attribute is required only for the fixed-height mode",
                    "<b>The background position does not work in fluid-height mode on outlook.com</b>",
                    "For better result we encourage you to use a background image width equal to the hero container width and always specify a fallback background color, in case the user email client does not support background images.",
                    "Please keep the hero container height below the image height. When the hero container height - both in fixed or fluid modes - is greater than the background image height, we can’t guarantee a perfect rendering in all supported email clients"
                ),
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "background-height",
                        MjmlAttributeType.PIXEL,
                        "height of the image used (mandatory in fixed-height mode)"
                    ),
                    MjmlAttributeInformation(
                        "background-position",
                        MjmlAttributeType.STRING,
                        "background image position",
                        "center center"
                    ),
                    ATTRIBUTE_BACKGROUND_URL,
                    MjmlAttributeInformation(
                        "background-width",
                        MjmlAttributeType.PIXEL,
                        "width of the image used",
                        "parent element width"
                    ),
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_HEIGHT,
                    MjmlAttributeInformation(
                        "height",
                        MjmlAttributeType.PIXEL,
                        "hero section height (required for fixed-height mode)",
                        "0px"
                    ),
                    MjmlAttributeInformation(
                        "mode",
                        MjmlAttributeType.STRING,
                        "choose if the height is fixed based on the height attribute or fluid",
                        "fluid-height"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "0px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "vertical-align",
                        MjmlAttributeType.STRING,
                        "content vertical alignment",
                        "top"
                    ),
                    MjmlAttributeInformation(
                        "width",
                        MjmlAttributeType.PIXEL,
                        "hero container width",
                        "parent elemenent width"
                    )
                ),
                allowedParentTags = PARENT_ANY
            ),
            MjmlTagInformation(
                "mj-image",
                "Displays a responsive image in your email. It is similar to the HTML <img /> tag. Note that if no width is provided, the image will use the parent column width.",
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "image alignment",
                        "center"
                    ),
                    ATTRIBUTE_ALT,
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation(
                        "fluid-on-mobile",
                        MjmlAttributeType.STRING,
                        "if \"true\", will be full width on mobile even if width is set"
                    ),
                    ATTRIBUTE_HEIGHT,
                    ATTRIBUTE_HREF,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_REL,
                    MjmlAttributeInformation(
                        "src",
                        MjmlAttributeType.URL,
                        "image source"
                    ),
                    MjmlAttributeInformation(
                        "srcset",
                        MjmlAttributeType.COMPLEX,
                        "enables to set a different image source based on the viewport\t"
                    ),
                    ATTRIBUTE_TARGET,
                    ATTRIBUTE_TITLE,
                    MjmlAttributeInformation(
                        "target",
                        MjmlAttributeType.STRING,
                        "link target on click",
                        "_blank"
                    ),
                    ATTRIBUTE_TITLE,
                    MjmlAttributeInformation(
                        "usemap",
                        MjmlAttributeType.STRING,
                        "reference to image map, be careful, it isn't supported everywhere\t"
                    ),
                    ATTRIBUTE_WIDTH
                ),
                allowedParentTags = PARENT_ANY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-navbar",
                "Displays a menu for navigation with an optional hamburger mode for mobile devices.",
                notes = arrayOf(
                    "The \"hamburger\" feature only work on mobile device with all iOS mail client, for others mail clients the render is performed on an normal way, the links are displayed inline and the hamburger is not visible."
                ),
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "align content left/center/right",
                        "center"
                    ),
                    MjmlAttributeInformation(
                        "base-url",
                        MjmlAttributeType.STRING,
                        "base url for children components"
                    ),
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation(
                        "hamburger",
                        MjmlAttributeType.STRING,
                        "activate the hamburger navigation on mobile if the value is hamburger"
                    ),
                    MjmlAttributeInformation(
                        "ico-align",
                        MjmlAttributeType.STRING,
                        "hamburger icon alignment, left/center/right (hamburger mode required)",
                        "center"
                    ),
                    MjmlAttributeInformation(
                        "ico-close",
                        MjmlAttributeType.STRING,
                        "char code for a custom close icon (hamburger mode required)",
                        "8855"
                    ),
                    MjmlAttributeInformation(
                        "ico-color",
                        MjmlAttributeType.COLOR,
                        "hamburger icon color (hamburger mode required)",
                        "#000000"
                    ),
                    MjmlAttributeInformation(
                        "ico-font-family",
                        MjmlAttributeType.STRING,
                        "hamburger icon font (only on hamburger mode)",
                        "Ubuntu, Helvetica, Arial, sans-serif"
                    ),
                    MjmlAttributeInformation(
                        "ico-font-size",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon size (hamburger mode required)",
                        "30px"
                    ),
                    MjmlAttributeInformation(
                        "ico-line-height",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon line height (hamburger mode required)",
                        "30px"
                    ),
                    MjmlAttributeInformation(
                        "ico-open",
                        MjmlAttributeType.STRING,
                        "char code for a custom open icon (hamburger mode required)",
                        "9776"
                    ),
                    MjmlAttributeInformation(
                        "ico-padding",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon padding, supports up to 4 parameters (hamburger mode required)",
                        "10px"
                    ),
                    MjmlAttributeInformation(
                        "ico-padding-bottom",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon bottom offset (hamburger mode required)",
                        "10px"
                    ),
                    MjmlAttributeInformation(
                        "ico-padding-left",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon left offset (hamburger mode required)",
                        "10px"
                    ),
                    MjmlAttributeInformation(
                        "ico-padding-right",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon right offset (hamburger mode required)",
                        "10px"
                    ),
                    MjmlAttributeInformation(
                        "ico-padding-top",
                        MjmlAttributeType.PIXEL,
                        "hamburger icon top offset (hamburger mode required)",
                        "10px"
                    ),
                    MjmlAttributeInformation(
                        "ico-text-decoration",
                        MjmlAttributeType.STRING,
                        "hamburger icon text decoration none/underline/overline/line-through (hamburger mode required)"
                    ),
                    MjmlAttributeInformation(
                        "ico-text-transform",
                        MjmlAttributeType.STRING,
                        "hamburger icon text transformation none/capitalize/uppercase/lowercase (hamburger mode required)\t"
                    )
                ),
                allowedParentTags = listOf("mj-column", "mj-hero")
            ),
            MjmlTagInformation(
                "mj-navbar-link",
                "This component should be used to display an individual link in the navbar.",
                notes = arrayOf(
                    "The mj-navbar-link component must be used inside a mj-navbar component only."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    ATTRIBUTE_FONT_WEIGHT,
                    ATTRIBUTE_HREF,
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "22px"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_REL,
                    ATTRIBUTE_TARGET,
                    MjmlAttributeInformation(
                        "text-decoration",
                        MjmlAttributeType.STRING,
                        "underline/overline/none"
                    ),
                    MjmlAttributeInformation(
                        "text-transform",
                        MjmlAttributeType.STRING,
                        "capitalize/uppercase/lowercase/none",
                        "uppercase"
                    )
                ),
                allowedParentTags = listOf("mj-navbar"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-raw",
                """
                    Displays raw HTML that is not going to be parsed by the MJML engine. Anything left inside this tag should be raw, responsive HTML. If placed inside <mj-head>, its content will be added at the end of the <head>.
                    
                    If you use mj-raw to add templating language, and use the minify option, you might get a Parsing error, especially when using the < character. You can tell the minifier to ignore some content by wrapping it between two <!-- htmlmin:ignore --> tags.
                """.trimIndent(),
                allowedParentTags = PARENT_ANY,
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-section",
                """
                    Sections are intended to be used as rows within your email. They will be used to structure the layout.
                    
                    The full-width property will be used to manage the background width. By default, it will be 600px. With the full-width property on, it will be changed to 100%.
                """.trimIndent(),
                notes = arrayOf(
                    "<b>Inverting the order in which columns display:</b> set the `direction` attribute to `rtl` to change the order in which columns display on desktop. Because MJML is mobile-first, structure the columns in the <b>order you want them to stack on mobile</b>, and use `direction` to change the order they display <b>on desktop</b>.",
                    "Sections cannot be nested into sections. Also, any content in a section should also be wrapped in a column."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "background-repeat",
                        MjmlAttributeType.STRING,
                        "css background repeat",
                        "repeat"
                    ),
                    MjmlAttributeInformation(
                        "background-size",
                        MjmlAttributeType.COMPLEX,
                        "css background size",
                        "auto"
                    ),
                    ATTRIBUTE_BACKGROUND_URL,
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_BORDER_BOTTOM,
                    ATTRIBUTE_BORDER_LEFT,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_BORDER_RIGHT,
                    ATTRIBUTE_BORDER_TOP,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation(
                        "direction",
                        MjmlAttributeType.STRING,
                        "set the display order of direct children",
                        "ltr"
                    ),
                    MjmlAttributeInformation(
                        "full-width",
                        MjmlAttributeType.STRING,
                        "make the section full-width"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "20px 0"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "text-align",
                        MjmlAttributeType.STRING,
                        "css text align",
                        "center"
                    )
                ),
                allowedParentTags = listOf("mj-body", "mj-wrapper")
            ),
            MjmlTagInformation(
                "mj-social",
                "Displays calls-to-action for various social networks with their associated logo. You can add social networks with the mj-social-element tag.",
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "left/right/center",
                        "center"
                    ),
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    ATTRIBUTE_FONT_WEIGHT,
                    ATTRIBUTE_ICON_HEIGHT,
                    ATTRIBUTE_ICON_SIZE,
                    MjmlAttributeInformation(
                        "inner-padding",
                        MjmlAttributeType.PIXEL,
                        "social network surrounding padding",
                        "4px"
                    ),
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "22px"
                    ),
                    MjmlAttributeInformation(
                        "mode",
                        MjmlAttributeType.STRING,
                        "vertical/horizontal",
                        "horizontal"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_ICON_PADDING,
                    MjmlAttributeInformation(
                        "text-padding",
                        MjmlAttributeType.COMPLEX,
                        "padding around the texts",
                        "4px 4px 4px 0"
                    ),
                    MjmlAttributeInformation(
                        "text-decoration",
                        MjmlAttributeType.STRING,
                        "underline/overline/none"
                    )
                ),
                allowedParentTags = listOf("mj-column", "mj-hero")
            ),
            MjmlTagInformation(
                "mj-social-element",
                """
                    This component enables you to display a given social network inside mj-social.
                    Note that default icons are transparent, which allows background-color to actually be the icon color.
                """.trimIndent(),
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "left/right/center",
                        "center"
                    ),
                    ATTRIBUTE_ALT,
                    ATTRIBUTE_BACKGROUND_COLOR,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    ATTRIBUTE_FONT_WEIGHT,
                    ATTRIBUTE_HREF,
                    ATTRIBUTE_ICON_HEIGHT,
                    ATTRIBUTE_ICON_SIZE,
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "22px"
                    ),
                    MjmlAttributeInformation(
                        "mode",
                        MjmlAttributeType.STRING,
                        "vertical/horizontal",
                        "horizontal"
                    ),
                    MjmlAttributeInformation(
                        "name",
                        MjmlAttributeType.STRING,
                        "social network name, see supported list below"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "4px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_ICON_PADDING,
                    MjmlAttributeInformation(
                        "text-padding",
                        MjmlAttributeType.COMPLEX,
                        "padding around the text",
                        "4px 4px 4px 0"
                    ),
                    MjmlAttributeInformation(
                        "src",
                        MjmlAttributeType.URL,
                        "image source",
                        "Each social name has its own default+"
                    ),
                    ATTRIBUTE_TARGET,
                    ATTRIBUTE_TITLE,
                    MjmlAttributeInformation(
                        "text-decoration",
                        MjmlAttributeType.STRING,
                        "underline/overline/none",
                        "none"
                    ),
                    MjmlAttributeInformation(
                        "vertical-align",
                        MjmlAttributeType.STRING,
                        "top/middle/bottom",
                        "middle"
                    )
                ),
                allowedParentTags = listOf("mj-social"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-spacer",
                "Displays a blank space.",
                attributes = arrayOf(
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_HEIGHT,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_WIDTH
                ),
                allowedParentTags = PARENT_ANY
            ),
            MjmlTagInformation(
                "mj-table",
                "This tag allows you to display table and filled it with data. It only accepts plain HTML.",
                attributes = arrayOf(
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "self horizontal alignment",
                        "left"
                    ),
                    ATTRIBUTE_BORDER,
                    MjmlAttributeInformation(
                        "cellpadding",
                        MjmlAttributeType.PIXEL,
                        "space between cells"
                    ),
                    MjmlAttributeInformation(
                        "cellspacing",
                        MjmlAttributeType.PIXEL,
                        "space between cell and border"
                    ),
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "22px"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "width",
                        MjmlAttributeType.STRING,
                        "table width",
                        "100%"
                    )
                ),
                allowedParentTags = listOf("mj-column", "mj-hero")
            ),
            MjmlTagInformation(
                "mj-text",
                "This tag allows you to display text in your email.",
                notes = arrayOf(
                    "`MjText` can contain any HTML tag with any attributes. Don't forget to encode special characters to avoid unexpected behaviour from MJML's parser"
                ),
                attributes = arrayOf(
                    ATTRIBUTE_COLOR,
                    ATTRIBUTE_FONT_FAMILY,
                    ATTRIBUTE_FONT_SIZE,
                    ATTRIBUTE_FONT_STYLE,
                    ATTRIBUTE_FONT_WEIGHT,
                    MjmlAttributeInformation(
                        "line-height",
                        MjmlAttributeType.PIXEL,
                        "space between the lines",
                        "1"
                    ),
                    MjmlAttributeInformation(
                        "letter-spacing",
                        MjmlAttributeType.PIXEL,
                        "letter spacing"
                    ),
                    ATTRIBUTE_HEIGHT,
                    MjmlAttributeInformation(
                        "text-decoration",
                        MjmlAttributeType.STRING,
                        "underline/overline/line-through/none",
                    ),
                    MjmlAttributeInformation(
                        "text-transform",
                        MjmlAttributeType.STRING,
                        "uppercase/lowercase/capitalize"
                    ),
                    MjmlAttributeInformation(
                        "align",
                        MjmlAttributeType.STRING,
                        "left/right/center/justify",
                        "left"
                    ),
                    ATTRIBUTE_CONTAINER_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "10px 25px"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS
                ),
                allowedParentTags = listOf("mj-column", "mj-hero"),
                canHaveChildren = false
            ),
            MjmlTagInformation(
                "mj-wrapper",
                "The full-width property will be used to manage the background width. By default, it will be 600px. With the full-width property on, it will be changed to 100%.",
                notes = arrayOf(
                    "You can't nest a full-width section inside a full-width wrapper, section will act as a non-full-width section.",
                    "If you're using a background-url on a `mj-wrapper` then do not add one into a section within the mj-wrapper. Outlook Desktop doesn't support nested VML, so it will most likely break your email. Also, if you use a background-color on mj-wrapper and a background-url on its section/hero child, the background-color will be over the background-image on Outlook. There is no way to keep the vml image over the content and under the wrapper background-color due to z-index being ignored on most tags."
                ),
                attributes = arrayOf(
                    ATTRIBUTE_BACKGROUND_COLOR,
                    MjmlAttributeInformation(
                        "background-repeat",
                        MjmlAttributeType.STRING,
                        "css background repeat",
                        "repeat"
                    ),
                    MjmlAttributeInformation(
                        "background-size",
                        MjmlAttributeType.STRING,
                        "css background size",
                        "auto"
                    ),
                    ATTRIBUTE_BACKGROUND_URL,
                    ATTRIBUTE_BORDER,
                    ATTRIBUTE_BORDER_BOTTOM,
                    ATTRIBUTE_BORDER_LEFT,
                    ATTRIBUTE_BORDER_RADIUS,
                    ATTRIBUTE_BORDER_RIGHT,
                    ATTRIBUTE_BORDER_TOP,
                    ATTRIBUTE_CSS_CLASS,
                    ATTRIBUTE_MJ_CLASS,
                    MjmlAttributeInformation(
                        "full-width",
                        MjmlAttributeType.STRING,
                        "make the wrapper full-width"
                    ),
                    MjmlAttributeInformation(
                        "padding",
                        MjmlAttributeType.COMPLEX,
                        "supports up to 4 parameters",
                        "20px 0"
                    ),
                    ATTRIBUTE_PADDING_BOTTOM,
                    ATTRIBUTE_PADDING_LEFT,
                    ATTRIBUTE_PADDING_RIGHT,
                    ATTRIBUTE_PADDING_TOP,
                    MjmlAttributeInformation(
                        "text-align",
                        MjmlAttributeType.STRING,
                        "css text align",
                        "center"
                    )
                ),
                allowedParentTags = listOf("mj-body", "mj-column", "mj-hero", "mj-wrapper")
            )
        )
    }
}
