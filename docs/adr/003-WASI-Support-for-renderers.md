# WASI Support for Renderers

* Status: accepted
* Date: 2025-05-20

Technical Story: [#254](https://github.com/timo-reymann/intellij-mjml-support/issues/254)

## Context and Problem Statement

Currently the plugin uses Node.js to run the MJML renderer. This works well, but it has some drawbacks, such as the need
for a Node.js installation. As Node.js support is only available for the paid IntelliJ versions, this limits the
plugin's usability in the free versions of IntelliJ IDEA and other JetBrains IDEs.

## Decision Drivers <!-- optional -->

* easier to use in free versions of IntelliJ IDEA and other JetBrains IDEs
* allows using custom renderers without Node.js

## Considered Options

* Implement using Rust and MRML for initial WASI support
* Dont implement WASI support

## Decision Outcome

Chosen option: "Implement using Rust and MRML for initial WASI support",
because Rust allows us to implement a WASI-compatible renderer that can be used in all JetBrains IDEs, including the
free versions.

## Pros and Cons of the Options <!-- optional -->

### Implement using Rust and MRML for initial WASI support

Implementing the renderer using Rust and the MRML library allows us to create a WASI-compatible binary that can be
used in all JetBrains IDEs, including the free versions.

* Good, because it allows us to use the same renderer in all JetBrains IDEs, including the free versions
* Good, because Rust is memory-safe
* Bad, because it requires additional development effort to implement the renderer in Rust

### Dont implement WASI support

Not implementing WASI support means that the plugin will continue to use Node.js for rendering, which works well but
limits the plugin's usability in the free versions of IntelliJ IDEA and other JetBrains IDEs.

* Good, because it is easier to maintain the existing Node.js implementation
* Good, because it allows us to use the existing MJML renderer without changes
* Bad, because it limits the plugin's usability in the free versions of IntelliJ IDEA and other JetBrains IDEs
* Bad, because it requires a Node.js installation
