# Estelle sound and death fix

- Suppresses inherited zombie idle, hurt, and death vocalizations.
- Removes LivingDeathEvent cancellation that caused apparent death without entity removal.
- Clears the unique NPC registry on death so `/sky test npc estelle` can summon again.
- Combat/weapon/texture remain unchanged.
- NOTE: Forge Gradle build and in-game behavior have not been verified here.
