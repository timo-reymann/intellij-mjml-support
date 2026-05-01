# Bundle MJML v4 and v5

* Status: accepted
* Date: 2026-05-01

Technical Story: [#362](https://github.com/timo-reymann/intellij-mjml-support/issues/362)

## Context and Problem Statement

MJML 5.0.0 introduces several breaking changes against the currently bundled v4.18.0:

* `mj-include` is ignored by default unless `includePath` is passed to `mjml2html()`.
* The minifier was replaced (`html-minifier` + `js-beautify` → `htmlnano` + `cssnano`); generated HTML
  differs visibly.
* The `<body>` skeleton is now driven by `mj-body` instead of the global skeleton.
* The `mjml-migrate` helper was removed.
* Node.js 20+ is required.

A direct upgrade would silently break templates that rely on `mj-include` and surprise users with
different rendered output. We want to give users an explicit choice and time to migrate.

## Decision Drivers

* Avoid silent breakage for existing templates using `mj-include`.
* Let users opt into v5 on their own schedule, but ship v5 in the bundle so adoption does not
  require custom rendering scripts.
* Keep v4 working without changes for users who cannot move to Node 20+ yet.

## Considered Options

* Bundle both v4 and v5; v4 is default; v5 is opt-in via Settings.
* Bundle v5 only and rely on the existing custom-rendering-script setting for v4 users.
* Stay on v4 for now and revisit later.

## Decision Outcome

Chosen option: **Bundle both v4 and v5**.

* Default remains MJML v4 — existing users see no behavior change after upgrade.
* MJML v5 is selectable under **Tools → MJML Settings → Bundled MJML version**.
* The plugin preflight-checks the project Node.js version when v5 is selected and surfaces a
  clear error in the preview if it is below 20.
* v4 is kept for one major plugin release in maintenance mode (critical fixes only) and will be
  retired in a future major version.

## Pros and Cons of the Options

### Bundle both v4 and v5

* Good, because no existing user is broken on upgrade.
* Good, because users can switch back without uninstalling or reconfiguring custom scripts.
* Good, because we can communicate the v5 differences directly in the settings dialog.
* Bad, because the plugin payload grows by roughly the size of the second renderer bundle.
* Bad, because we have to maintain two npm projects and two CI test jobs for the
  maintenance window.

### Bundle v5 only and require custom scripts for v4

* Good, because no extra payload.
* Bad, because every user with `mj-include` templates would see silent breakage on upgrade.
* Bad, because rolling back to v4 forces the user to set up Node, run `npm install` for the v4
  bundle, and point the plugin at a custom script — high friction.

### Stay on v4

* Good, because no work needed.
* Bad, because it postpones the v5 transition indefinitely; new users miss out on v5 fixes and
  performance improvements.
