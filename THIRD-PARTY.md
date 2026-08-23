# Third-party components

Alone/RL is licensed under the GNU AGPL 3.0 (see `LICENSE`). It builds on:

| Component | License | Use |
|-----------|---------|-----|
| [artemis-odb](https://github.com/junkdog/artemis-odb) | Apache-2.0 | Entity-component-system framework |
| [artemis-odb-contrib](https://github.com/DaanVanYperen/artemis-odb-contrib) | MIT | Extra systems/utilities for artemis-odb |
| [AsciiPanel](https://github.com/trystan/AsciiPanel) | MIT | ASCII terminal rendering (copied under `src/main/java/asciiPanel/` from `372dfbae98`, font sheets in `src/main/resources/`; may carry local changes — see its README) |
| [rlforj-alt](https://github.com/fabio-t/rlforj-alt) | BSD-3-Clause | Field of view, line of sight, pathfinding (Gradle source dependency built from GitHub at tag `0.4.0` — see `settings.gradle.kts`) |
| [terrain-generator](https://github.com/fabio-t/terrain-generator) | Apache-2.0 | Island/terrain generation (Rust library; consumed as the `terrain-generator-0.2.1.jar` from its GitHub releases — Java FFM binding with embedded per-platform natives) |
| [Jackson](https://github.com/FasterXML/jackson) | Apache-2.0 | YAML data files |
| [Logback](https://logback.qos.ch) / [SLF4J](https://www.slf4j.org) | EPL-1.0/LGPL-2.1 dual · MIT | Logging |
