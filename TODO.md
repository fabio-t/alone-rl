- Make cells able to only contain one type of entities having a specific Component (eg, "Solid" or whatever).
  Actually, should be two components: one to pass light through, and one not. In this way, items would freely pile up
  in each cell, animals would occupy a cell singularly (and consider cells occupied by other animals, or trees,
  inaccessible) and trees would block field of view in addition to occupying a cell. It will require a bit of changes
  but it's better this way, for a roguelike.
  
- complete transition to data-driven entity factory (not a single entity component should be hard-coded in bootstrap..
  ideally)

- add items and inventory management for player

- add fight system

- carnivores should eat

- transform it into a "turn"-based game by tweaking the game loop so that logic waits for player input, then
  it runs as long as the player action lasts. This may or may not prove a hassle..
