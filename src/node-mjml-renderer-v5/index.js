async function main() {
    const mjml2html = require("mjml")
    const fs = require('fs');
    const stdin = fs.readFileSync(0, 'utf-8');

    const {directory, content, options: { mjmlConfigPath }, filePath, projectRoot} = JSON.parse(stdin)

    // MJML 5 throws "Specified filePath does not exist" when filePath is set but missing on disk.
    // Production passes a real file path, but for unsaved/in-memory templates we fall back to the
    // directory so the parser can still derive the include base.
    let resolvedFilePath = filePath
    if (resolvedFilePath && !fs.existsSync(resolvedFilePath)) {
        resolvedFilePath = directory
    }

    // The template's own directory is the parser's cwd and therefore already allowed by MJML.
    // Only pass the project root, so shared partials in sibling/parent folders work without
    // tripping the security check on the redundant cwd entry.
    const includePaths = []
    if (projectRoot && projectRoot !== directory) {
        includePaths.push(projectRoot)
    }

    const options = {
        useMjmlConfigOptions: true,
        mjmlConfigPath: mjmlConfigPath || directory,
        filePath: resolvedFilePath || directory,
        ignoreIncludes: false
    }
    if (includePaths.length) {
        options.includePath = includePaths
    }

    const {html, errors} = await mjml2html(content, options)
    return {html, errors}
}

main()
    .then((result) => process.stdout.write(JSON.stringify(result)))
    .catch((e) => {
        const result = {
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
        process.stdout.write(JSON.stringify(result))
    })
