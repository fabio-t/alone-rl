# itch.io page copy

Ready-made art lives beside this file and in `screenshots/`:

* cover (630x500, itch's required ratio): `docs/assets/itch-cover.png`
* screenshots: `screenshots/gameplay.png`, `worldgen.png`, `crafting.png`,
  and the animated `gameplay.gif`

Draft text for the store page — edit freely, then paste into the itch.io
project editor. Page settings that matter:

* **Kind of project**: Downloadable
* **Pricing**: No payments (or "Donation" if you want a tip jar)
* **Platforms**: Windows, macOS, Linux (butler sets these per channel)
* **Classification**: Game · **Genre**: Role Playing · **Tags**: roguelike,
  ascii, survival, procedural-generation, open-source, singleplayer
* **Community**: Comments — good for feedback while it's in development

---

## Title

Alone/RL

## Short description (tagline)

Survive alone on a procedurally generated island, in ASCII.

## Description

**Alone/RL** is a single-player ASCII roguelike about surviving, alone, on an
island inhabited by animals. It takes after *UnReal World* and *Wayward*, with
simpler gameplay.

It's real-time, but defaults to a turn-based mode where the world only advances
while you act — hold or toggle a key to let time run freely.

### The island is simulated, not decorated

Every world is generated from scratch: fractal noise shaped into an island,
carved by **simulated hydraulic erosion**, then run through a real hydrology
pass — so rivers follow the valleys the erosion cut, and lakes settle where
water actually collects.

### Animals live there whether you watch or not

Creatures aren't spawned around you. They exist, and they keep going when
you're elsewhere. Herbivores seek grass and flee predators; carnivores hunt and
scavenge; some species move in herds and packs. Every creature has its own
field of view — hide behind a boulder and you genuinely aren't seen. The game
doesn't cheat: they can only do what you can.

### Survive

Eat, keep your health and stamina up, and stay out of reach of things bigger
than you. Cut trees with a sharp edge, smash boulders with a blunt one, and
craft the basics of neolithic technology — knives, spears, axes, bark armour.
Or throw a rock at a rabbit and hope.

## Controls

* **Arrow keys** — move (two together for diagonals). Move into things to
  attack, cut or crush them.
* **g** get item · **d** drop · **e** eat · **w** wear/wield · **c** craft
* **l** look around; **t** throws an equipped weapon at the target
* **Ctrl+Space** toggle real-time/turn-based · **Space** pause, or hold to let
  time pass

## Install

Download the build for your platform, unzip it, and run it. A Java runtime is
bundled — there's nothing else to install.

*macOS*: the app isn't signed yet, so the first launch needs right-click →
Open (or the itch app).

## Notes

This is still in development and not feature-complete — expect rough edges, and
please leave a comment if you hit something odd.

Free software under the GNU AGPL 3.0. Source, issues and releases:
https://github.com/fabio-t/alone-rl
