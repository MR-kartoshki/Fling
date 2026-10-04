# Fling

**Fling your items!**

Hold your drop control to charge a throw, then release it to fling an item.
Catch nearby items with your use control, and give dropped items custom rotation
and ground rendering.

## Installation

- Required: Minecraft 26.3, Java 25 (or newer) and Fabric Loader 0.19.5 (or newer)
- Put the Fling JAR in the client's `mods` directory. For multiplayer throwing and catching, install Fling and Fabric API on the server too.
- Singleplayer uses the integrated server automatically.

## Controls

| Action | Default control |
| --- | --- |
| Drop one item | Tap **Q** |
| Charge and throw one item | Hold **Q**, then release |
| Drop or throw the whole stack | Hold **Ctrl** when pressing **Q** |
| Catch a nearby item | Aim at it and press **right-click** |

Fling follows Minecraft's **Drop Selected Item** and **Use Item/Place Block**
bindings, including mouse bindings. Rebind them in Minecraft's Controls menu.
The Ctrl state (full stack throw) is captured when charging starts.

While charging in first person, a bar below the crosshair shows charge percentage and whether you are throwing a single item or the whole stack. 
The default full charge takes two seconds. Releases below 7.5% charge perform a normal drop.
Opening a menu, losing focus, disconnecting, or changing hotbar slots cancels the charge.

## Configuration

Fling creates `config/fling.json` on first launch:

```json
{
  "throwing": true,
  "catching": true,
  "customRendering": true,
  "chargeDuration": 2.0,
  "throwStrength": 1.0
}
```

| Setting | Meaning |
| --- | --- |
| `throwing` | Enable charged throws. Disabling this restores vanilla dropping locally. |
| `catching` | Enable manual catching. Normal automatic pickup is unaffected. |
| `customRendering` | Enable custom dropped-item rendering; client only. |
| `chargeDuration` | Seconds to reach full charge, from **0.25** to **10**. |
| `throwStrength` | Throw speed multiplier, from **0.1** to **5**; **1** preserves the default speed. |

## Limitations

- Throwing uses the selected main-hand hotbar item. Offhand throws are unsupported.
- Item movement, water behavior, despawning, and merging use vanilla physics.
- Custom rendering affects all dropped items and may conflict with other mods that replace the item renderer.
- The charge indicator is hidden with the HUD and outside first-person view.

## License

Original source code is licensed under the MIT License. Adapted portions of
`ItemPhysics.java` are covered by LGPL-3.0, as described below.

All original visual and audio assets, including the mod icon, textures, sounds,
and other media under `src/main/resources/assets/fling/`, are licensed under
CC BY 4.0 unless otherwise stated.

See:
- `LICENSE`
- `LICENSE-ASSETS`
- `LICENSE-ItemPhysicLite`
- `LICENSE-CreativeCore`
- `LICENSE-GPL-3.0`
- `NOTICE`

`ItemPhysics` adapts the client rotation calculation and custom item submit pipeline from [ItemPhysic Lite](https://github.com/CreativeMD/ItemPhysicLite), branch `26.x`, Copyright CreativeMD. The fluid viscosity calculation follows `CreativeFabricLoader.getFluidViscosityMultiplier` from [CreativeCore](https://github.com/CreativeMD/CreativeCore), branch `26.x`, Copyright CreativeMD. Both adapted portions are modified for Fling and covered by LGPL-3.0; the license text is included in `LICENSE-ItemPhysicLite`.

Fling icon © RadRad, licensed under CC BY 4.0.
