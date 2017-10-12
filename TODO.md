  
- complete transition to data-driven entity factory (not a single entity component should be hard-coded in bootstrap..
  ideally)
  
- rlforj must be changed so that instead of isObstacle, it has two functions: blocksLight and blocksStep.
  In this way we can have FOV algorithm only using blocksLight, pathfinding algorithms only using
  blocksStep, and allow the use of both for special cases. Generally, creatures will block step while trees/walls
  will block both step and light. In a more advanced game, we'd also have walls that don't stop light (windows),
  and something like smoke that doesn't block stop but blocks light.

- GUI needs to be redone (with Zircon?), to show a panel for inventory/crafting, the combat log, an input field maybe..

- throwing must be improved: range should be based on strength, accuracy on agility.
  
- Hide SingleGrid and MultipleGrid behind Map, so that I can generalise MovementSystem to move creatures and items,
  without if-else.. maybe? We'll see if this happens more often, if it's only for the movement of
  thrown stuff than we can leave it like this.
