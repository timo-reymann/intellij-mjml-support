const mjml2html = require("mjml")
const fs = require('fs');
const data = fs.readFileSync(0, 'utf-8');

let result;

try {
    let {html, errors} = mjml2html(data, {
        useMjmlConfigOptions: true
    })
    result = {html, errors}
} catch (e) {
    result = {
        html: null,
        errors: [
            {
                line: -1,
                message:  e.toString(),
                tagName: "",
                formattedMessage: `Invaldi mjml: ${e.toString()}`
            }
        ]
    }
}

process.stdout.write(JSON.stringify(result))
