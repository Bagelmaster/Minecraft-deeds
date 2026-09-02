# Minecraft Deeds

A Fabric mod for Minecraft Java Edition that lets players
claim land with deeds. Built for a small private server
with friends.

## Target
- Minecraft 26.2
- Fabric Loader 0.19.3 + Fabric API 0.158.0+26.2
- Fabric Loom 1.17. No mappings: 26.x ships with Mojang's
  real class names, so the code uses those directly.
- Java 25, Gradle 9.5.1 (via the included wrapper)

Versions live in `gradle.properties`. Check
https://fabricmc.net/develop for newer ones.

## Planned features
1. `/deed claim` – claim the area selected by survey tool
2. `/deed info` – show who owns the current parcel
3. Claims saved per world and survive restarts
4. `/deed trust <player>` and `/deed untrust <player>`
5. Later: container protection, claim limits, deed transfer

## Status
Scaffolded and building. Freeform plot claims are in:

- Survey tool: a plain stick for now. Hold it in your main
  hand, left-click a block for the first corner and
  right-click a block for the opposite corner. Plots are
  rectangles in X/Z and cover every Y level.
- `/deed claim` claims the selected plot. It is rejected if
  it overlaps any existing plot (yours or someone else's).
- `/deed info` prints the owner of the plot you are standing
  in, or "Unclaimed".
- Claims are saved in the world's `data` folder (dimension +
  plot corners + owner) and survive restarts. Selections are
  not saved; they only live until you claim or log out.

Not done yet: trust lists, protection, plot size limits, a
custom survey item.

## Code tour
All code is server-side and lives in
`src/main/java/com/bagelmaster/deeds/`:

| File | What it does |
| --- | --- |
| `Deeds.java` | Mod entry point. Registers the survey tool and the command. |
| `SurveyTool.java` | Listens for stick clicks and remembers each player's two corners. |
| `DeedCommand.java` | The `/deed claim` and `/deed info` commands. |
| `DeedState.java` | Saves and loads all claims (Minecraft `SavedData`). |
| `Claim.java` | One claimed plot: where it is and who owns it. |
| `Plot.java` | A rectangle in one dimension, with overlap and contains checks. |

## Building
```
./gradlew build
```
You need Java 25 installed. The finished mod jar is written to
`build/libs/deeds-<version>.jar`.
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
   server). Grab a stick (`/give @s stick`), left-click one
   corner block and right-click the opposite corner, then:
   ```
   /deed claim
   /deed info
   ```
   Step outside the plot and run `/deed info` again. Try
   selecting a plot that overlaps the first one to see it
   rejected.
4. To test "someone else owns it", run the dev server and join
   with two accounts. To check saving, leave the world, rejoin,
   and run `/deed info` inside the plot.

If you need readable Minecraft sources for browsing in the IDE,
run `./gradlew genSources` once.
