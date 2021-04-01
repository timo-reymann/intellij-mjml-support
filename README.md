intellij-mjml-support
===

[![GitHub Release](https://img.shields.io/github/v/tag/timo-reymann/intellij-mjml-support.svg?label=version)](https://github.com/timo-reymann/intellij-mjml-support/releases)
[![JetBrains Plugins](https://img.shields.io/badge/JetBrains-Plugins-orange)](https://plugins.jetbrains.com/plugin/16418-mjml-support)

MJML support for the IntelliJ Platform.

> Please note that is plugin is in early development and may miss some features
> or needs some optimization here and there.
>
> Feel free to also take a look at the roadmap to see whats up next.
>
> You are missing something? - Feel free to file a PR/issue or open a discussion on GitHub!

## What's in the box?

- MJML file icons
- Syntax highlighting and auto complete for mjml files
- Auto complete for built in mjml tags
- Syntax highlighting for custom tags and css
- path completion for mj-includes
- class usage for css-class attribute
- url references
- color annotation and detection for attributes
- code inspections to highlight common errors

## How can I use it?

1. Install it from the plugin repository
2. Restart your IDE to initially load the plugin
4. You are done, enjoy the magic!

## Roadmap / Milestones

1. Basic Support
    - [x] higlighting of tags and css
    - [x] basic auto completion for tags and attributes
    - [x] documentation in the editor for mjml components with link to docs
    - [x] import ALL tags (currently till mj-accordion)
2. Extended assistance
    - [x] auto completion for mj-include
    - [x] path validation for mj-include
    - [x] css class usage
    - [x] auto complete for units / default attributes
    - [x] color annotation
3. MJML Validation & Live preview
    - [x] code inspection with validation of attributes
    - [x] show live preview like for markdown with rendered template
    - [x] build errors
4. Custom Component Support
    - [x] mjml config with json schema
    - [x] ide configuration with mjml config file
    - [x] support for custom components [PARTIAL with configuration]
