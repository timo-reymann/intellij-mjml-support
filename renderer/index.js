const mjml2html = require("mjml")
const fs = require('fs');
const data = fs.readFileSync(0, 'utf-8');
const htmlOutput = mjml2html(data, {})

process.stdout.write(JSON.stringify(htmlOutput))

