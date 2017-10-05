  
- complete transition to data-driven entity factory (not a single entity component should be hard-coded in bootstrap..
  ideally)

- transform it into a "turn"-based game by tweaking the game loop so that logic waits for player input, then
  it runs as long as the player action lasts. This may or may not prove a hassle..
  
- rlforj must be changed so that instead of isObstacle, it has two functions: blocksLight and blocksStep, or
  something similar. In this way we can have FOV algorithm only using blocksLight, pathfinding algorithms only using
  blocksStep, and allow the use of both for special cases. Generally, creatures will block step while trees/walls
  will block light.
  This is important so that we'll be able to stop using line of sight instead of pathfinding. We want, for example,
  to see behind a rabbit or even a wolf - but then we have to plan a course around them. So we need both LOS and
  AStar.
