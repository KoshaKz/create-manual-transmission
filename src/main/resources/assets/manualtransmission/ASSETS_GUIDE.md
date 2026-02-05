# Asset Placement Guide

This directory structure has been created for you to place the assets from "Create: The Factory Must Grow" (TFMG).

Please copy the files as follows:

## Models
Copy these files from TFMG to `src/main/resources/assets/manualtransmission/models/block/steering_wheel/`:
* `wheel.json` (from `assets/tfmg/models/block/engine_controller/`)
* `pedal.json` (from `assets/tfmg/models/block/engine_controller/`)
* `transmission_lever.json` (from `assets/tfmg/models/block/engine_controller/`)
* `dial.json` (optional, if you want the dials)

## Textures
Copy the texture file from TFMG to `src/main/resources/assets/manualtransmission/textures/block/`:
* `engine_controller_parts.png` (from `assets/tfmg/textures/block/engines/`)

## Note
You may need to open the `.json` model files and update the texture paths inside them. 
Change references like `"tfmg:block/engines/engine_controller_parts"` to `"manualtransmission:block/engine_controller_parts"`.
