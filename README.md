# Minecraft Deeds

A Fabric mod for Minecraft Java Edition that lets players
claim land with deeds. Built for a small private server
with friends.

## Target
- Minecraft 1.21.11 (the last 1.21.x release)
- Fabric Loader 0.19.3 + Fabric API 0.141.6+1.21.11
- Yarn mappings 1.21.11+build.6, Fabric Loom 1.17
- Java 21, Gradle 9.5.1 (via the included wrapper)

Versions live in `gradle.properties`. Check
https://fabricmc.net/develop for newer ones.

## Planned features
1. `/deed claim` – claim the area selected by survey tool
2. `/deed info` – show who owns the current parcel
3. Claims saved per world and survive restarts
4. `/deed trust <player>` and `/deed untrust <player>`
5. Later: container protection, claim limits, deed transfer

## Status
Scaffolded and building. First feature is in:

- `/deed claim` claims the chunk you are standing in.
  Claiming a chunk someone else owns is rejected.
- `/deed info` prints the owner of the current chunk, or
  "Unclaimed".
- Claims are saved in `<world>/data/deeds.dat` (keyed by
  dimension + chunk position) and survive restarts.

Not done yet: survey tool / custom parcel sizes, trust
lists, protection.

## Code tour
All code is server-side and lives in
`src/main/java/com/bagelmaster/deeds/`:

| File | What it does |
| --- | --- |
| `Deeds.java` | Mod entry point. Registers the command. |
| `DeedCommand.java` | The `/deed claim` and `/deed info` commands. |
| `DeedState.java` | Saves and loads all claims (Minecraft `PersistentState`). |
| `Claim.java` | One claimed chunk: where it is and who owns it. |
| `ChunkKey.java` | Dimension + chunk coordinates, used as the map key. |

## Building
```
./gradlew build
```
The finished mod jar is written to `build/libs/deeds-<version>.jar`.
Drop it (plus the Fabric API jar) into a Fabric server's `mods/`
folder to use it. Every push also runs this build on GitHub
Actions (see the "build" workflow).

## Running the dev client
Loom sets up run configurations for you, no manual install needed.

1. Open the project in IntelliJ IDEA (File > Open > pick the folder)
   and let Gradle import finish. Loom also generates
   "Minecraft Client" and "Minecraft Server" run configurations in
   the IDE's run menu.
2. Or from a terminal:
   ```
   ./gradlew runClient    # launches a dev Minecraft client
   ./gradlew runServer    # launches a dev dedicated server
   ```
   The first run downloads Minecraft and may take a few minutes.
   The game files live in `run/` (git-ignored).
3. In the client, create a singleplayer world (or join the dev
   server). Then try:
   ```
   /deed info
   /deed claim
   /deed info
   ```
   Walk to another chunk (F3 shows chunk coordinates) and repeat.
4. To test "someone else owns it", run the dev server, join with
   two accounts, or check that `deeds.dat` exists under
   `run/saves/<world>/data/` after leaving the world.

If you need readable Minecraft sources for browsing in the IDE,
run `./gradlew genSources` once.
