# Mechanism to validate mjml code

* Status: accepted
* Date: 2021-03-26

## Context and Problem Statement

In order for the plugin to be useful for developers it should assist and provide mjml errors as an IntellIJ user would
expect.

So there are two types of inspections:
- warnings, that can be false positives or just suggestions
- errors, when the user is doing something that simply doesn't work

## Decision Drivers 

* should be easy to adjust/extend for custom components, ideally without any extra work
* should feel native to the user (like a typical Java/JavaScript-Routine)
* should be easy to implement without implementing a lexer etc; based on xml

## Considered Options

* Use mjml-validator package
* Build custom component model and use that for inspections etc.

## Decision Outcome

Chosen option: "Build custom component model and use that for inspections etc.",
because the mjml-validator seems harder to integrate and can't assist with custom components.

## Pros and Cons of the Options <!-- optional -->

### Use mjml-validator package

Use the official [mjml-validator](https://www.npmjs.com/package/mjml-validator) npm package.

* Good, because it doesnt require to implement everything
* Good, because it's exactly the same logic used in the rendering process
* Bad, because the package is written in Node.js that makes interoperability harder
* Bad, because it can't do real code assistance
* Bad, because it has no support for parsing custom components dynamically

### Build custom component model and use that for inspections etc.

Build a custom programmatic model of mjml tags, their attributes etc. and reimplement the logic from scratch.

So it's a composition out of IntelliJ components.

* Good, because it is the best we can do from an ux perspective
* Good, because it is easy to write, test and integrate the code
* Good, because the integration is reliable
* Good, because support for custom components is possible via ExtensionPoints, allowing even further extension  
* Bad, because it's an different implementation that might be incompatible with future versions
* Bad, because we are reinventing the wheel

## Links <!-- optional -->

* [npm package for mjml-validator](https://www.npmjs.com/package/mjml-validator)
