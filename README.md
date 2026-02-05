# Create: Manual Transmission

An addon for **Create** and **Create: The Factory Must Grow (TFMG)** that adds a realistic **Manual Transmission Steering Wheel**.

## Features

### 1. Advanced Steering Wheel Block
Inspired by the steering wheels in recent Create updates, this block offers enhanced control over your contraptions.

### 2. Interactive Gearbox HUD
When using the steering wheel, a **Gearbox UI** (H-shifter style) appears in the bottom-right corner of the screen.

- **Dynamic Shifting**: Hold `Alt` to unlock the mouse cursor and interact with the gear stick directly.
- **Clamped Movement**: The gear stick is constrained to the physical paths of the gearbox (it won't clip through the metal guides).
- **Engine Control**: Shifting gears actively changes the **Rotation Speed (RPM)** and **Torque** output of the connected engine or contraption.

## Planned Logic

- **Input Handling**: Detect `Alt` key press to toggle cursor mode.
- **Mouse Delta Tracking**: Calculate mouse movement to drag the shifter knob.
- **Path Constraints**: `Math.clamp` logic to ensure the knob follows the H-pattern (Neutral, 1-5, R).
- **Packet System**: Send gear state from Client -> Server to update the SpeedController or Motor.

## Installation

Requires:
- [Create Mod](https://www.curseforge.com/minecraft/mc-mods/create)
- [Create: The Factory Must Grow](https://www.curseforge.com/minecraft/mc-mods/create-industry) (Recommended)

## Development

This project uses Gradle.

```bash
./gradlew genSources
./gradlew runClient
```
