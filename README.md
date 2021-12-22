# IntelliJ plugin for Quarkus Continuous Testing

Quarkus provides a [Continuous Testing Mode](https://quarkus.io/guides/continuous-testing), 
that allows all tests, or only failing tests to be re-run from the dev mode CLI, or when code is saved.

The tests run with much less startup time, because it use classes that are hot-swapped into the dev mode server instance,
rather than rebuilding the project for each test run. However, there are some drawbacks:

- It uses a CLI interface, which makes results harder to read than IntelliJ test results, 
  which are displayed in a tree UI with filterable console output in the IDE.
- It only allows running tests touched by changes, or failing tests, or all tests. 
  IntelliJ's test UI, on the other hand, allows you to easily choose which tests to run using context menus or gutter icons.
- The file change polling on some systems can be quite slow with default settings.

This extension is an attempt to fix these drawbacks by integrating Continuous Testing mode with IntelliJ.
It offers the same UX as you'd expect from IntelliJ, but provides it by communicating with Quarkus dev mode over a
[custom websocket endpoint](https://github.com/danelowe/quarkus-testsocket).

The tests run super-fast, and are easy to control and view.

To run the tests, make sure to configure IntelliJ not to run tests via gradle.
Then, start up quarkus dev mode, and use the IntelliJ run configurations with the Quarkus logo.