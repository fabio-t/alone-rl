
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
  
- Hunger effects: should we add them? eg, reducing stamina and health regeneration when severely hungry?
  Possibly to do in a way that there are two bars overlapped: the first goes full without effect. When it's full,
  it starts again to colour, more darkly, and there the reduction in regeneration progresses.
  A full doubly-coloured bar means zero regen.

- LookScreen must show a message when moving around; the white colour of the targeting it's also horrible. It would be
  nice to make it better, eg a darker shade of whatever background colour there is? (a TileTransformer would work nicely
  maybe)

- weapon damage must be included in the damage formula :) for now only strength it's used!
