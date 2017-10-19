  
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

- Must handle "linked entities" better using LinkListener. It's probably not needed for items, but for creatures, trees
  and boulders (eg, entities that can be destroyed) yes. When fire is implemented, items too should be handled.
  The approach is simple: inside the ActionSystem get hold of the EntityLinkManager instance and register
  the Action component with a LinkAdapter instance overriding the onTargetDead function.
  This means that when a target dies, the corresponding action is interrupted. Right now the relevant field is
  set to -1 but that's it.

- Data-driven via YAML:
  - Map thresholds for terrain (colours, character, elevation)
  - Item templates
  - Game configuration (height and width of terminal, font..)
  - Crafting recipes

- Fish behaviour: swim around, flee from any creature in the field of view making an action but completely ignore
  still creatures, maybe eat when on shallow water and consume when on deep water?
