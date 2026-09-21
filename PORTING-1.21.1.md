# Minecraft 1.21.1 NeoForge port

This branch starts from upstream `1.20.6` (commit `643b9a623c910eb2fc9511a2838b0ffcaf2bd99f`).
It targets Minecraft 1.21.1, NeoForge 21.1.251 and Architectury API 13.0.8.

The first pass deliberately enables only `common` and `neoforge` in Gradle.
Fabric sources remain untouched and must be ported separately before that module is
re-enabled. This makes the NeoForge migration reviewable without claiming
cross-loader support prematurely.

## Verified

- `:common:compileJava` passes with Java 21.
- `:neoforge:build` produces `HQM-1.21.1-5.19.0-neoforge.jar`.
- `:neoforge:runClient` reaches the Minecraft main menu with HQM in the mod list.

## Not yet verified

- Joining a world, opening the quest book, selecting a chapter, completing tasks,
  and reward claims.
- Dedicated server startup and client/server synchronization.
- Rendering fidelity for custom rotations, selection highlights and fluid icons.

The port updates resource location factories, crafting input, and the 1.21 vertex
builder API. These changes compile but the rendering paths need in-game testing.

## Build

Use Java 21 and run `./gradlew :neoforge:build`. The generated JAR is under
`neoforge/build/libs/`. Do not publish it as a release until the unverified
paths above have been tested.
