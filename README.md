intellij-mjml-support
===

[![GitHub Release](https://img.shields.io/github/v/tag/timo-reymann/intellij-mjml-support.svg?label=version)](https://github.com/timo-reymann/intellij-mjml-support/releases)
[![JetBrains Plugins](https://img.shields.io/badge/JetBrains-Plugins-orange)](https://plugins.jetbrains.com/plugin/16418-mjml-support)
[![CircleCI](https://circleci.com/gh/timo-reymann/intellij-mjml-support.svg?style=shield)](https://app.circleci.com/pipelines/github/timo-reymann/intellij-mjml-support)

MJML support for the IntelliJ Platform.

> You are missing something or something is broken? - Feel free to file a PR/issue or open a discussion on GitHub!

## What's in the box?

Fore more details please look up the plugin description in the marketplace or directly in
the [plugin.xml](./src/main/resources/META-INF/plugin.xml).

## How can I use it?

1. Install it from the plugin repository
4. You are done, enjoy the magic!

## Extension

### Custom mjml resolution

You want to add custom mjml tags or custom resolving to match your needs?

Simply use the extension point `de.timo_reymann.intellij-mjml-support.tagInformationProvider`:

```xml

<extensions defaultExtensionNs="de.timo_reymann.intellij-mjml-support">
    <tagInformationProvider implementation="my.company.OurCustomTaginformationProvider"/>
</extensions>
```

### Custom rendering

- Execution context: parent folder for file to render
- Input from stdin:
  ```json
    {
      "directory": "absolute path to project root, this might be different from the current file location",
      "content": "file editor content to render"
    }
  ```
- Output to stdout must be in json in this format for:
  ```json
    {
      "html": "string|null",
      "errors": [
        {
          "line": "integr|null",
          "message":  "string|null",
          "tagName": "string|null",
          "formattedMessage": "string|null"
        } 
      ]
    }
  ```
  where errors can be empty, but can never be omitted!

## Notes about implementation

- The preview editor support is adapted from the official markdown plugin
- Preview uses bundled node_modules for mjml rendering currently, support for custom rendering is currently in
  development
