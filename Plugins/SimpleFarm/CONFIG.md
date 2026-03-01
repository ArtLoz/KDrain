# SimpleFarm — Config

Config file: `SimpleFarm.json` (next to the plugin JAR).
On first run, the default config is extracted automatically.

## Fields

| Field           | Type   | Default | Description                                                    |
|-----------------|--------|---------|----------------------------------------------------------------|
| `farmZone`      | string | —       | Name of the farm zone (must match a defined FarmZone object)   |
| `buffId`        | int    | 4328    | Buff ID to track (Newbie Guide buff)                           |
| `soeId`         | int    | 10650   | Scroll of Escape item ID                                       |
| `ssId`          | int    | 2509    | Soulshot item ID (script stops when count < 300)               |
| `petRezItemId`  | int    | 0       | Item ID for pet resurrection (0 = no pet / skip)               |
| `escapeSkillId` | int    | 0       | Escape skill ID, used as fallback when no SoE in inventory     |

## Available Farm Zones

Zones are defined as `FarmZone` data objects in `kutils/location/farm/`.
Use the object name as `farmZone` value in config.

Example zones (once defined):
- `KamaelGold` — Gold farm near Kamael Village
- `KamaelGolem` — Golem farm near Kamael Village
- `GludioWindmill` — Windmill Hill from Gludin Village

## Example Config

```json
{
  "farmZone": "KamaelGold",
  "buffId": 4328,
  "soeId": 10650,
  "ssId": 2509,
  "petRezItemId": 0,
  "escapeSkillId": 0
}
```

## How It Works

1. **In town**: rez pet (if dead) → check SS → buff character → buff pet → teleport to spot
2. **On spot**: enable face control (auto-farm). Monitor for:
   - Character dead → respawn to town
   - Buff ending → fight off mobs → SoE home
   - Pet dead → fight off mobs → SoE home
   - SS < 300 → fight off mobs → SoE home → stop script
3. Loop back to step 1

## FarmZone Structure

Each zone defines:
- `town` — base town (TownLocation) for buffs and teleport
- `teleportPath` — dialog indices at Gatekeeper to reach the zone
- `spot` — GPS destination point at the farm location
- `buffDialogPath` — dialog indices at NPC Newbie for character buff
- `petBuffDialogPath` — dialog indices for pet buff (optional)
- `zoneFile` — `.zmap` resource path for face control
- `configFile` — `.xml` resource path for face control config (optional)
