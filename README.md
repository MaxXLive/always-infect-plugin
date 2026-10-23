# AlwaysInfect

Paper plugin (Minecraft 26.3) that controls the chance of a zombie turning a villager into a zombie villager,
independent of the difficulty. By default every villager killed by a zombie is infected, as on Hard.

## Config

`plugins/AlwaysInfect/config.yml`, read on server start:

```yaml
# true: replace the vanilla chance (Easy 0%, Normal 50%, Hard 100%) on every difficulty
override-default: true
# chance from 0.0 to 1.0
override-percentage: 1.0
```

## Build

```
./gradlew build
```

The jar ends up in `build/libs/`. Every push to `master` builds a new GitHub release (`v<mc>-patch-<n>`).
