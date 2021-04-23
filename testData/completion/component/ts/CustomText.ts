import mjml2html from "mjml";
import {BodyComponent} from "mjml-core";
import {registerComponent} from "mjml-core";
import {MJMLCustomComponent} from "mjml-custom-component-decorator/lib/MJMLCustomComponent";

@MJMLCustomComponent({
    attributes: {
        text: {
            type: 'string',
            default: 'Hello World'
        },
        'color': {
            type: "color"
        },
        'empty': {},
        'bool':{
            type: 'boolean',
            default: false
        }
    },
    allowedParentTags: ["mj-column"]
})
export class CustomText extends BodyComponent {
    render() {
        return this.renderMJML(`
            <mj-text align="center" color="${this.getAttribute('text-color')}">
                ${this.getAttribute("text")}
            </mj-text>`
        )
    }
}

registerComponent(CustomText)
