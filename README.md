# Better With Hands

Did you know that you have a second hand? Look down. It's right there. You've had it this whole time.

For years, Steve has been living with a mysterious condition — completely ignoring his left arm. He'd hold a single pickaxe while his other hand just... hung there. Doing nothing. Watching. It's frankly a miracle he survived this long.

**I cured him.**

Better With Hands is a reimagining of the offhand system for BTA (Better Than Adventure). This isn't a lazy port — it's what dual-wielding should have been from the start. Every interaction, every small detail has been crafted to feel like it was always part of the game.

Steve can finally use both hands. You're welcome.

## What does this mod do?

Hold a torch while mining. Keep blocks ready while exploring. Swap between sword and pickaxe on the fly. Your offhand isn't just a slot — it's a fully functional second hand with proper animations, rendering, and interactions.

## Features

### Offhand Slot
A dedicated slot in your inventory for a second item. Access it from your inventory screen — look for the hand icon to the left of your armor slots.

### Full Rendering
Your offhand item appears in both first-person and third-person views. The item is properly mirrored and positioned to look natural.

### Proper Swing Animation
When you use your offhand, you'll see a full swing animation — your arm moves, your torso rotates, just like the main hand. No shortcuts, no static arms.

### HUD Display
A hotbar slot appears next to your main hotbar showing your offhand item. It smoothly slides in when you equip something and slides out when empty.

### Multiplayer Support
Everything syncs properly in multiplayer. Your settings, your offhand contents, your animations — other players see it all.

## Controls

| Key | Action |
|-----|--------|
| **F** | Swap items between main hand and offhand |
| **X** | Toggle hand priority (quick switch which hand is "active") |

Both keys can be rebound in Options.

## Settings

Find all settings in **Options > Better With Hands**.

### Behavior

**Use Offhand When Main Empty** (default: ON)
When your main hand is empty, the game automatically uses your offhand item like pickaxe.

**Swap Hand Priority** (default: OFF)  
Flips which hand the game considers "primary." When ON, actions default to your offhand instead of main hand. Toggle this with the X key during gameplay.

**Dynamic Hand Priority (Experimental)** (default: OFF)  
The smart option. The game automatically picks the right hand based on what you're doing:
- *Mining a block?* Uses whichever hand has the better tool for that block
- *Attacking?* Prefers swords over other items
- *Placing blocks?* Uses the hand holding blocks

Example: Pickaxe in main hand, sword in offhand. Mine normally, but when a creeper appears, your attacks automatically use the sword.

### Display

**Left-Handed**  
Follows the game's own left-handed setting. Your main hand moves to the left, the offhand to the right, and the HUD slot moves to the other side of the hotbar.

**Indicator Type**  
A visual marker showing which hand is currently active:
- *Border* — Highlight around the active slot
- *Vanilla* — Highlight in the style of the hotbar selection
- *Dot* — Small dot above the active slot  
- *Arrow* — Arrow pointing to the active slot
- *None* — No indicator

**Show Indicator Only With Offhand** (default: ON)  
The indicator only appears when you have something in your offhand. Keeps the screen clean when you're not using dual-wield.

## How Priority Works

The mod needs to know which hand to use when you click. Here's the logic:

1. **Default behavior:** Main hand is primary. Offhand is backup.

2. **"Use When Empty" ON:** If main hand is empty, offhand takes over automatically.

3. **"Swap Priority" ON:** Offhand becomes primary. Main hand becomes backup.

4. **"Dynamic Priority" ON:** The game decides based on context. Mining? Best tool wins. Combat? Sword wins. Placing? Blocks win.

The indicator on your HUD always shows which hand will be used.

## Practical Examples

**Mining Setup**  
Main hand: Pickaxe | Offhand: Torches  
Mine normally. When you need light, empty your hand (or press X) and place torches.

**Combat Ready**  
Main hand: Sword | Offhand: Shield or Food  
Fight with your sword, have healing ready.

**Building**  
Main hand: Blocks | Offhand: Different blocks  
Switch between materials with X instead of scrolling through your hotbar.

**Dynamic Mining** (with Dynamic Priority ON)  
Main hand: Pickaxe | Offhand: Axe  
Mine stone with pickaxe, approach a tree and it automatically switches to axe.

**Dual-Wield Swords**  
Main hand: Sword | Offhand: Sword  
Left click attacks with offhand, right click attacks with main hand. Channel your inner Kirito.

## Additional Details

- **Item Pickup:** Picked up items go into your offhand first when they stack with it
- **Q to Drop:** When offhand has priority, pressing Q drops from your offhand
- **Tool Stats:** Mining speed and harvest level are checked correctly for offhand tools

## Installation

1. Install BTA 8.0.1 with Fabric and HalpLibe
2. Place the mod `.jar` file in your `mods` folder
3. Launch the game

## Multiplayer Notes

The mod works in multiplayer with some considerations:
- Both client and server need the mod. On a server without it the offhand stays inactive
- Your settings sync to the server when you join
- Other players see your offhand items and animations

## Credits

- [Better Than Adventure](https://www.betterthanadventure.net/) - The BTA mod team

