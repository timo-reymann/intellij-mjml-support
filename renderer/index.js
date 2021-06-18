let result;

try {
    const mjml2html = require("mjml")
    const fs = require('fs');
    const stdin = fs.readFileSync(0, 'utf-8');

    const {directory, content} = JSON.parse(stdin)

    let {html, errors} = mjml2html(content, {
        useMjmlConfigOptions: true,
        mjmlConfigPath: directory
    })
    result = {html, errors}
} catch (e) {
    result = {
        html: null,
        errors: [
            {
                line: -1,
                message:  (e || "unknown").toString(),
                tagName: "",
                formattedMessage: `Invalid mjml: ${e.toString()}`
            }
        ]
    }
}

process.stdout.write(JSON.stringify(result))
