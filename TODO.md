  
- rlforj was changed to support blocksLight and blocksStep in addition to isObstacle. FoV/LoS algorithms by default
  only care about "light-blocking" obstacles, while pathfinding uses isObstacle (which is true in case something
  either blocks light, or it blocks movement, or both).
  A component LightBlocker must now be added to become light-blocking entities (trees and boulders for now).
  Now I need to used a modified AStar that takes only a subset of the map (eg, within the visible radius) otherwise
  it's computationally too hard.

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
  set to -1 but that's it. In other words, the action still runs its course to the end, and silently fails at
  the "do" stage if the targets have become invalid.

- Data-driven via YAML:
  - Map thresholds for terrain (colours, character, elevation)
  - Item templates
  - Game configuration (height and width of terminal, font..)
  - Crafting recipes

- Fish behaviour: swim around, flee from any creature in the field of view making an action but completely ignore
  still creatures, maybe eat when on shallow water and consume when on deep water?
  Most of the above done, although fish don't eat for now. Maybe ever. Also to keep things generic, I don't check
  in the FleeFromActionBehaviour if the creature is a fish or not. This makes it so that fishes escape from other
  fishes too.. and end up dead on the seashore. That may be stupid (although it's kinda funny :P)
  
- Hunger effects: should we add them? eg, reducing stamina and health regeneration when severely hungry?
  Possibly to do in a way that there are two bars overlapped: the first goes full without effect. When it's full, it starts again
  to colour, more darkly, and there the reduction in regeneration progresses. A full doubly-coloured bar means zero
  regen.
