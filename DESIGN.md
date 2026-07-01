# Design

## Layers
- `model` — GraphNode, NodeTransaction (Jackson-mapped POJOs).
- `repository` — loads the JSON once at startup, holds in-memory indexes, exposes lookups.
- `service` — all graph logic (level, parentChain, children, tree, aggregates, filters, cycles).
- `controller` — REST endpoints under `/api/graph/nodes`.
- `dto` — response shapes.
- `exception` — custom exceptions + `@RestControllerAdvice` error mapping.

## Data loading
- On startup (`@PostConstruct`) the file `classpath:transactions-graph.json` is parsed with Jackson.
- Two indexes are built:
  - `nodeById: Map<String, GraphNode>`
  - `childrenByParentId: Map<String, List<GraphNode>>`
- Two passes: index all nodes first, then link children. A child is registered only if its
  `parentId` exists in `nodeById`, so a missing parent never breaks loading.
- Child lists are id-sorted for deterministic output. Node/file order is not assumed.

## Level and parentChain
- Computed by walking `parentId` upward, collecting ancestors (prepended so the list is
  root → direct parent).
- `level` = size of parentChain. Root (`parentId == null`) and orphan (parent missing) → 0.
- `isRoot` = `parentId == null` or parent not present. `isLeaf` = no children.
- Child summaries get `parentLevel + 1` without re-walking.

## Orphans
- A non-null `parentId` that is absent from the dataset stops the upward walk: level 0,
  isRoot true, empty parentChain (e.g. `N19` → `N99`).

## Cycles
- Every traversal carries a `visited` set:
  - upward walk (parentChain),
  - subtree walk (childrenTree, aggregates).
- Re-entering a visited node throws `CycleDetectedException` → HTTP 400 `CYCLE_DETECTED`,
  so a cyclic dataset cannot cause an infinite loop or stack overflow.

## Bonus features
- `maxDepth` nested tree: DFS bounded by depth (requested node = depth 0), default 1, capped at 5,
  `< 0` / `> 10` rejected with 400.
- Level aggregates: DFS over the subtree, bucketed by level relative to the requested node;
  `totalAmount` = absolute sum of amounts, regardless of direction.
- Filtered children-transactions: direct-children transactions filtered by
  minAmount / maxAmount / txnType.

## Scaling / extension
- In-memory maps are read-only after startup, so reads are thread-safe.
- For large datasets: move to a DB and use recursive CTEs, a closure table, or a graph DB for
  ancestor/descendant queries; precompute and cache levels at load.
- For deep trees: prefer iterative BFS/DFS (already used for aggregates) over deep recursion.
- For large transaction volumes: paginate transaction lists and stream aggregates.
