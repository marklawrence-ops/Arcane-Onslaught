# Arcane Onslaught

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

Arcane Onslaught is a 2D top-down auto-attack survival game where the player controls a mage who battles endless waves of monsters in an open arena. The mage automatically casts spells at intervals, and the player must dodge enemies while collecting experience orbs to unlock new spells and upgrade abilities. The goal is to survive as long as possible and create powerful spell synergies through upgrades.

# External Art Assets Used
- Spells - 16x16 Simple Fantasy RPG FX by Emcee Flesher (https://opengameart.org/content/16x16-simple-fantasy-rpg-fx)
- Enemies - 32rogues by Seth (https://sethbb.itch.io/32rogues)

# External Sound Assets Used
- Pixel Combat by Helton Yan & Beto Bezerra (https://heltonyan.itch.io/pixelcombat)

# External Font Used
- DungeonFont by vrtxrry (https://vrtxrry.itch.io/dungeonfont)
- Silver Font by PoppyWorks (https://poppyworks.itch.io/silver)

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
