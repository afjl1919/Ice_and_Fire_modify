# Ice and Fire
Ice and Fire 是一款由Raptorfarian和Alexthe666制作的Minecraft模组，为游戏添加了各种神话生物，如龙、半鹰半狮兽、精灵等！

# Ice and Fire 1.20.1：三种龙穴由 Feature 迁移为 Structure
本修改把以下三个地下龙穴从 `Feature<NoneFeatureConfiguration>` 改为原生 `Structure`：
- `iceandfire:fire_dragon_cave`
- `iceandfire:ice_dragon_cave`
- `iceandfire:lightning_dragon_cave`

目标版本：Minecraft 1.20.1 Forge，对应本源码包 `Ice_and_Fire-1.20.1-2.1.13-beta-5`。

## 主要实现

### 1. 原生 Structure 与 StructurePiece
- `DragonCaveStructure`：负责候选区块、概率、高度、生物群系、距离和龙穴间距判定，并创建结构起点。
- `DragonCavePiece`：保存龙种、中心、半径、年龄、性别和随机种子，通过 NBT 持久化；在每个相交区块内分块放置洞穴。
- `DragonCaveType`：集中维护火龙、冰龙、雷龙各自的方块、矿石标签、钟乳石、宝藏堆、实体及战利品表。

大型洞穴不再依赖一次 Feature 调用跨越多个区块，而是由 `StructurePiece#postProcess` 按区块边界生成，可被结构系统保存和定位。

### 2. 注册与数据文件

新增并注册：
- `iceandfire:dragon_cave` StructureType
- `iceandfire:dragon_cave` StructurePieceType
- 三个 `worldgen/structure/*.json`
- 三个 `worldgen/structure_set/*.json`
- 三个 `tags/worldgen/biome/has_structure/*.json`

同时移除三种龙穴原有的 configured feature、placed feature 和 biome modifier 注入。

### 3. 保留的玩法逻辑

仍会生成：
- 对应龙种的洞壁方块和矿石
- 主洞室与侧洞室
- 洞顶钟乳石
- 金/银/铜宝藏堆
- 雌性/雄性龙穴箱子及原战利品表
- 75–124 日龄、沉睡、禁用成长、带巢穴位置的三级龙

继续读取这些配置项：
- `generateDragonDenChance`
- `oreToStoneRatioForDragonCaves`
- `dragonDenGoldAmount`
- `dangerousWorldGenDistanceLimit`
- `dangerousWorldGenSeparationLimit`
- 模组原有的三种龙穴生物群系配置

## 定位命令

```mcfunction
/locate structure iceandfire:fire_dragon_cave
/locate structure iceandfire:ice_dragon_cave
/locate structure iceandfire:lightning_dragon_cave
```

只会影响尚未生成的新区块；已生成区块中的旧龙穴不会自动迁移。

## 与旧 Feature 行为的差异

1. `Structure.GenerationContext` 没有可用的实时世界出生点或 `IafWorldData` 实例，因此出生点距离改为相对世界原点 `(0, 0)` 判断。
2. 原来的危险地物间距由持久化世界数据统一管理；结构起点阶段无法访问该数据。本实现使用确定性的局部优先级，限制三种地下龙穴彼此之间的距离，但不会与龙巢、独眼巨人洞穴等其他危险 Feature 做完全相同的互斥。
3. 为了让跨区块结果稳定，洞穴内部采用“结构种子 + 方块坐标”的位置随机数。外形与内容规则保持一致，但同一世界种子下的精确洞形和矿石位置不会与旧版 Feature 完全相同。
4. 三个 StructureSet 使用 `spacing: 1`，再由 `DragonCaveStructure` 应用原有的 `1 / generateDragonDenChance` 概率。这样配置仍可在运行时生效，但结构定位扫描的候选区块会比使用固定大间距的结构更多。
5. 结构生物群系标签先覆盖主世界，再在 `findGenerationPoint` 中执行原有动态 BiomeConfig 检查。这是为了保留用户配置，而不是把动态配置硬编码成静态数据包标签。

## 构建

运行

```bash
./gradlew build
```

可在 `build\libs` 中找到输出的 `.jar`文件
