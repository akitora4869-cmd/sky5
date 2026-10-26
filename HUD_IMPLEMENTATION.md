# SkyCombat party HUD (Forge 1.20.1)

- Anchored at upper-right, 194 logical GUI pixels wide; each party member occupies 83px vertically.
- Server sends player and owned, living companions only. A roaming NPC is not shown.
- Player portrait renders the live Minecraft skin face (8,8 8x8) and hat layer (40,8 8x8).
- Estelle portrait uses `assets/skycombat/textures/gui/estelle_face.png` cropped from the supplied screenshot.
- Circle ring uses the user-provided 80x80 image, scaled to 64x64; its transparent center shows the portrait.
- Server owns HP/EP/CP values. Updates are sent every 10 player ticks; no arbitrary client fake stats.
- Vanilla hearts are hidden while valid HUD data is available. Hunger/experience/hotbar remain vanilla.
- CP ring is a static supplied decoration; numeric CP is live. A dynamic circular fill can be added in a later iteration.
- Only currently loaded NPC entities are enumerated. An NPC in an unloaded chunk will not show until loaded.
- Build has not been run locally (Gradle executable / Forge caches unavailable). Test with GitHub Actions.

## stas1 frame update
- Replaced portrait_ring.png with supplied stas1.png (80x80 RGBA; center already transparent).
- The HUD remains rendered at 50% scale; ring is 40 GUI pixels on screen.
- Player skin face and hat layer render under the ring. Estelle portrait also renders under the ring.
- The frame already includes CP lettering, so the overlay only draws the numeric CP value.
- Build/game display not yet verified.
