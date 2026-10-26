# Sky Combat — GitHub starter (Forge 1.20.1 / Java 17)

A **standalone ForgeGradle source project**, not an MCreator workspace. Based on the prior SkyCombatTest NPC AI source, with Estelle texture/staff assets imported from the supplied workspace.

## Build on GitHub
1. Create an empty GitHub repository.
2. Upload **the contents of this folder** to the repository root (keep `.github/workflows/build.yml`).
3. Open **Actions → Build Forge Mod → Run workflow**, or push a commit.
4. Download `skycombat-forge-jar` from the successful workflow's Artifacts section.

Locally, Java 17 and Gradle 8.8 are required: `gradle build`. This ZIP does **not** include Gradle Wrapper binaries. Forge dependencies download on first build.

## Test
- `/sky test npc estelle` (OP)
- `/sky test npc joshua` (OP; **temporary Estelle texture**, Joshua skin not provided)
- Right-click NPC to recruit/dismiss; NPC auto-fights and uses simplified craft/arts logic.
- `/sky stats`, `/sky test enemy 10`, `/sky craft whirlwind`, `/sky arts`

## Visuals
Renderer uses `PlayerModel` with the vanilla 64x64 classic layout, enabling hat/jacket/sleeves/pants outer layers. The Estelle staff is a 2D held item. **No custom 3D hair or costume geometry has been created.**

## Known limitations
- **Not build-tested in this environment**. Please share the GitHub Actions build log if it fails.
- This is a **combat prototype**; craft/arts, recovery, retreat and uniqueness logic are provisional, not a full Trails system.
- Named NPC uniqueness and chunk-unload behavior should be tested carefully. Current roster intentionally prevents spawning duplicates for unloaded NPCs.
- Imported texture is reused for Joshua until a separate skin is supplied.

## Estelle staff: 2.5-block 3D model

- `assets/skycombat/models/item/estelle_staff.json` defines a 40-pixel-long (2.5 block) rod with a 2-pixel-diameter shaft, grip, and reinforced ends.
- Textures: `staff_wood.png`, `staff_grip.png`, `staff_cap.png`.
- This is a visual-only change; attack damage and collision reach are unchanged.
- Display transforms are initial test values; refine after checking first- and third-person views in game.
- Push the project files to GitHub and run the existing Actions workflow.
