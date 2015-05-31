This plugin for IntelliJ/PyCharm executes [flake8](https://gitlab.com/pycqa/flake8) and parses the results in order to highlight issues in Python code.

## Requirements
* [Python](https://www.python.org/)
* [pip](https://pypi.python.org/pypi/pip)
* [flake8](https://gitlab.com/pycqa/flake8)
* [flake8-import-order](https://github.com/public/flake8-import-order)

Note: `flake8` must be executable via your bash terminal.

## Building
Run `./gradlew build` to create the plugin.

## Development
Run `./gradlew setup` to create the project files needed for development in IntelliJ/PyCharm.

## Limitations
Currently this plugin is only using the [flake8-import-order](https://github.com/public/flake8-import-order) plugin, so it will currently only identify issues with import order. Future improvements will include the ability to specify the flake8 arguments, however most of these other flake8 checks already exist in [PyCharm](https://www.jetbrains.com/pycharm/) and the [Python IntelliJ plugin](https://plugins.jetbrains.com/plugin/?idea&pluginId=631).

Only works on Linux and Mac.
