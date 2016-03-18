# Alone: the roguelike

Simple single-player ANSI roguelike. It's intended as a toy project to learn a bit about single-player game programming
(and also specifically to learn artemis-odb) without the hassle of game physics and most importantly graphics.

It deviates a bit from the "roguelike canon" already at this early stage: the creatures and objects are permanent.
A basic reproductive ability and genetic modification should allow for an increasingly difficult environment,
adapting to itself and to the player growing in strength.

## Field of View

Thanks to [rlforj](https://github.com/kba/rlforj), we are now using "precise permissive FOV", providing with a nice shadowing/light casting.

![alt tag](screenshots/fov.png)
