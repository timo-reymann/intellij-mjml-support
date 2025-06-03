intellij-mjml-support
===
[![CircleCI](https://circleci.com/gh/timo-reymann/intellij-mjml-support.svg?style=shield)](https://app.circleci.com/pipelines/github/timo-reymann/intellij-mjml-support)
[![Version](https://img.shields.io/jetbrains/plugin/v/16418-mjml-support)](https://plugins.jetbrains.com/plugin/16418-mjml-support/versions)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16418-mjml-support)](https://plugins.jetbrains.com/plugin/16418-mjml-support)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/16418-mjml-support)](https://plugins.jetbrains.com/plugin/16418-mjml-support/reviews)
[![Renovate](https://img.shields.io/badge/renovate-enabled-green?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAzNjkgMzY5Ij48Y2lyY2xlIGN4PSIxODkuOSIgY3k9IjE5MC4yIiByPSIxODQuNSIgZmlsbD0iI2ZmZTQyZSIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTUgLTYpIi8+PHBhdGggZmlsbD0iIzhiYjViNSIgZD0iTTI1MSAyNTZsLTM4LTM4YTE3IDE3IDAgMDEwLTI0bDU2LTU2YzItMiAyLTYgMC03bC0yMC0yMWE1IDUgMCAwMC03IDBsLTEzIDEyLTktOCAxMy0xM2ExNyAxNyAwIDAxMjQgMGwyMSAyMWM3IDcgNyAxNyAwIDI0bC01NiA1N2E1IDUgMCAwMDAgN2wzOCAzOHoiLz48cGF0aCBmaWxsPSIjZDk1NjEyIiBkPSJNMzAwIDI4OGwtOCA4Yy00IDQtMTEgNC0xNiAwbC00Ni00NmMtNS01LTUtMTIgMC0xNmw4LThjNC00IDExLTQgMTUgMGw0NyA0N2M0IDQgNCAxMSAwIDE1eiIvPjxwYXRoIGZpbGw9IiMyNGJmYmUiIGQ9Ik04MSAxODVsMTgtMTggMTggMTgtMTggMTh6Ii8+PHBhdGggZmlsbD0iIzI1YzRjMyIgZD0iTTIyMCAxMDBsMjMgMjNjNCA0IDQgMTEgMCAxNkwxNDIgMjQwYy00IDQtMTEgNC0xNSAwbC0yNC0yNGMtNC00LTQtMTEgMC0xNWwxMDEtMTAxYzUtNSAxMi01IDE2IDB6Ii8+PHBhdGggZmlsbD0iIzFkZGVkZCIgZD0iTTk5IDE2N2wxOC0xOCAxOCAxOC0xOCAxOHoiLz48cGF0aCBmaWxsPSIjMDBhZmIzIiBkPSJNMjMwIDExMGwxMyAxM2M0IDQgNCAxMSAwIDE2TDE0MiAyNDBjLTQgNC0xMSA0LTE1IDBsLTEzLTEzYzQgNCAxMSA0IDE1IDBsMTAxLTEwMWM1LTUgNS0xMSAwLTE2eiIvPjxwYXRoIGZpbGw9IiMyNGJmYmUiIGQ9Ik0xMTYgMTQ5bDE4LTE4IDE4IDE4LTE4IDE4eiIvPjxwYXRoIGZpbGw9IiMxZGRlZGQiIGQ9Ik0xMzQgMTMxbDE4LTE4IDE4IDE4LTE4IDE4eiIvPjxwYXRoIGZpbGw9IiMxYmNmY2UiIGQ9Ik0xNTIgMTEzbDE4LTE4IDE4IDE4LTE4IDE4eiIvPjxwYXRoIGZpbGw9IiMyNGJmYmUiIGQ9Ik0xNzAgOTVsMTgtMTggMTggMTgtMTggMTh6Ii8+PHBhdGggZmlsbD0iIzFiY2ZjZSIgZD0iTTYzIDE2N2wxOC0xOCAxOCAxOC0xOCAxOHpNOTggMTMxbDE4LTE4IDE4IDE4LTE4IDE4eiIvPjxwYXRoIGZpbGw9IiMzNGVkZWIiIGQ9Ik0xMzQgOTVsMTgtMTggMTggMTgtMTggMTh6Ii8+PHBhdGggZmlsbD0iIzFiY2ZjZSIgZD0iTTE1MyA3OGwxOC0xOCAxOCAxOC0xOCAxOHoiLz48cGF0aCBmaWxsPSIjMzRlZGViIiBkPSJNODAgMTEzbDE4LTE3IDE4IDE3LTE4IDE4ek0xMzUgNjBsMTgtMTggMTggMTgtMTggMTh6Ii8+PHBhdGggZmlsbD0iIzk4ZWRlYiIgZD0iTTI3IDEzMWwxOC0xOCAxOCAxOC0xOCAxOHoiLz48cGF0aCBmaWxsPSIjYjUzZTAyIiBkPSJNMjg1IDI1OGw3IDdjNCA0IDQgMTEgMCAxNWwtOCA4Yy00IDQtMTEgNC0xNiAwbC02LTdjNCA1IDExIDUgMTUgMGw4LTdjNC01IDQtMTIgMC0xNnoiLz48cGF0aCBmaWxsPSIjOThlZGViIiBkPSJNODEgNzhsMTgtMTggMTggMTgtMTggMTh6Ii8+PHBhdGggZmlsbD0iIzAwYTNhMiIgZD0iTTIzNSAxMTVsOCA4YzQgNCA0IDExIDAgMTZMMTQyIDI0MGMtNCA0LTExIDQtMTUgMGwtOS05YzUgNSAxMiA1IDE2IDBsMTAxLTEwMWM0LTQgNC0xMSAwLTE1eiIvPjxwYXRoIGZpbGw9IiMzOWQ5ZDgiIGQ9Ik0yMjggMTA4bC04LThjLTQtNS0xMS01LTE2IDBMMTAzIDIwMWMtNCA0LTQgMTEgMCAxNWw4IDhjLTQtNC00LTExIDAtMTVsMTAxLTEwMWM1LTQgMTItNCAxNiAweiIvPjxwYXRoIGZpbGw9IiNhMzM5MDQiIGQ9Ik0yOTEgMjY0bDggOGM0IDQgNCAxMSAwIDE2bC04IDdjLTQgNS0xMSA1LTE1IDBsLTktOGM1IDUgMTIgNSAxNiAwbDgtOGM0LTQgNC0xMSAwLTE1eiIvPjxwYXRoIGZpbGw9IiNlYjZlMmQiIGQ9Ik0yNjAgMjMzbC00LTRjLTYtNi0xNy02LTIzIDAtNyA3LTcgMTcgMCAyNGw0IDRjLTQtNS00LTExIDAtMTZsOC04YzQtNCAxMS00IDE1IDB6Ii8+PHBhdGggZmlsbD0iIzEzYWNiZCIgZD0iTTEzNCAyNDhjLTQgMC04LTItMTEtNWwtMjMtMjNhMTYgMTYgMCAwMTAtMjNMMjAxIDk2YTE2IDE2IDAgMDEyMiAwbDI0IDI0YzYgNiA2IDE2IDAgMjJMMTQ2IDI0M2MtMyAzLTcgNS0xMiA1em03OC0xNDdsLTQgMi0xMDEgMTAxYTYgNiAwIDAwMCA5bDIzIDIzYTYgNiAwIDAwOSAwbDEwMS0xMDFhNiA2IDAgMDAwLTlsLTI0LTIzLTQtMnoiLz48cGF0aCBmaWxsPSIjYmY0NDA0IiBkPSJNMjg0IDMwNGMtNCAwLTgtMS0xMS00bC00Ny00N2MtNi02LTYtMTYgMC0yMmw4LThjNi02IDE2LTYgMjIgMGw0NyA0NmM2IDcgNiAxNyAwIDIzbC04IDhjLTMgMy03IDQtMTEgNHptLTM5LTc2Yy0xIDAtMyAwLTQgMmwtOCA3Yy0yIDMtMiA3IDAgOWw0NyA0N2E2IDYgMCAwMDkgMGw3LThjMy0yIDMtNiAwLTlsLTQ2LTQ2Yy0yLTItMy0yLTUtMnoiLz48L3N2Zz4=)](https://renovatebot.com)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=timo-reymann_intellij-mjml-support&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=timo-reymann_intellij-mjml-support)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=timo-reymann_intellij-mjml-support&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=timo-reymann_intellij-mjml-support)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Ftimo-reymann%2Fintellij-mjml-support.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Ftimo-reymann%2Fintellij-mjml-support?ref=badge_shield)

<p align="center">
	<img width="800" src="./.github/images/feature_gallery.gif">
	<br />
    MJML support for the IntelliJ Platform.
</p>

## Features

- Syntax highlighting
- Inspections for your MJML code
- Powerful Live-Preview and tooling
- Autocompletion for color codes, tags, attributes etc.
- Support for custom MJML components

## Requirements

- IDEA-based IDE compatible with the plugin

## Installation

1. Press (Ctrl+Alt+S/⌘/) to open the IDE settings and select Plugins.
2. Search for `MJML Support` in the Marketplace and click Install.

### You live on the edge?

There is also the channel `snapshot` (https://plugins.jetbrains.com/plugins/snapshot/list) available, for more details
about set up please
see [JetBrains Marketplace Docs](https://plugins.jetbrains.com/docs/marketplace/custom-release-channels.html#configuring-a-custom-channel-in-intellij-platform-based-ides)

The versioning used there is not based on semantic versioning, but rather: `YYYY.MM.DD-BUILDNUM` and is on

## Usage

- MJML files are automatically picked, you can also find some
  screenshots in the marketplace.

## Motivation

I wanted MJML Support for my beloved JetBrains IDEs.

## Contributing

I love your input! I want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the configuration
- Submitting a fix
- Proposing new features
- Becoming a maintainer

To get started please read the [Contribution Guidelines](./CONTRIBUTING.md).

## Documentation

### Extending plugin functionality with additional plugins

If you want to support custom functionality or resolving maybe specific
to the needs of your company, there are some entrypoints available.

#### Custom mjml resolution

You want to add custom mjml tags or custom resolving to match your needs?

Use the extension point `de.timo_reymann.intellij-mjml-support.tagInformationProvider`:

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
      "content": "file editor content to render",
      "filePath": "absolute path to file",
      "options": {
        "mjmlConfigPath": "mjml config path or empty string"
      }
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

### Notes about implementation

- The preview editor support is adapted from the official Markdown plugin
- Preview for rendering is available
    - using Node.js with bundled node_modules for MJML rendering, with the possibility to use custom mjml config and
      specify custom node scripts
    - bundled MRML for MJML rendering, with the possibility to use custom WASI implementations

### Architecture Decision Records

For [architecture decision records](https://adr.github.io/) please take a look at [docs/adr;](./docs/adr) this will give
you an idea why implementations are as they are.

## Development

### Requirements

- [Java](https://openjdk.org/)
- [Gradle](https://gradle.org/)
- [Node.js](https://nodejs.org/en/download)
- [Rust](https://www.rust-lang.org/tools/install)

### Test

```shell
# To run unit tests
./gradlew test

# To run plugin verifier to check compability
./gradlew runPluginVerifier
```

### Build

```shell
./gradlew buildPlugin
```
