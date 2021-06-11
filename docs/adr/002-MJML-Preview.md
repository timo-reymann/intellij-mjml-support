# Solution for MJML Preview

* Status: accepted
* Date: 20201-04-01 <!-- not a joke ;) -->

## Context and Problem Statement

An important part of supporting MJML is providing a fast and responsive preview right in the IDE

## Decision Drivers

* should be performant
* easy to integrate
* output should be as close to mail result as possible

## Considered Options

* Integrate with GraalVM
* Bundle preview script with Webpack and use integrated WebView
* Bundle script and dependencies with the plugin and execute it with node.js

## Decision Outcome

Chosen option: "Bundle script and dependencies with the plugin and execute it with node.js
", because the other solutions havent been
working.

## Pros and Cons of the Options <!-- optional -->

### Integrate with GraalVM

With the graalvm it is possible to run JavaScript and Java in an homogenous way

* Good, because its very fast and can be embedded
* Good, because it should be pretty fast
* Bad, because GraalVM is still not in a "stable enough" state
* Bad, because it can't be bundled with the plugin (since it would explode the artifact size)

### Bundle preview script with Webpack and use integrated WebView

Bundle the script together with Webpack and run that inside a WebView.

No node.js needed.

* Good, because it makes the requirements lower
* Good, because it is easy to integrate and can handle incremental input
* Bad, because it is very complex
* Bad, because it cant handle file includes
* Bad, because it is necessary to fake some node apis

### Bundle script and dependencies with the plugin and execute it with node.js

Mjml can not only be used as an cli but also with Node.js. IntelliJ also provides us with Node.js interpreter
infrastructure.

* Good, because the result is exactly what mjml will render
* Good, because we can support custom rendering (like with template engines etc.)
* Bad, because it's hard to implement in a cross-platform manner
* Bad, because the integration might take some time to call the script on every change (can be a bit better with
  debounce)

## Links 

* [GraalVM](https://www.graalvm.org/)
* [GraalVM Java Interoperability](https://www.graalvm.org/reference-manual/js/JavaInteroperability/)
