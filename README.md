# Transaction Graph Node Explorer

REST service that explores a hierarchical transaction graph. Each node is a logical
entity (account/bucket) that holds transactions; the hierarchy is defined by `parentId`.
Given a node id, the API returns the node, its level, its parent chain, its direct
children, and the transactions of the node and its children.

## Tech Stack
- Java 17
- Spring Boot 3.3.2
- Maven

## Prerequisites
- JDK 17
- Maven 3.9+

## Run
    mvn spring-boot:run

Runs on http://localhost:8080. The dataset
`src/main/resources/transactions-graph.json` (20 nodes, 100 transactions) is loaded
into memory at startup.

## Endpoints

`GET /api/graph/nodes/{id}`
Returns node details, level, parentChain, direct children, the node's transactions,
next-level (children's) transactions, and per-level aggregates for the subtree.

Query params:
- `maxDepth` (default 1, effective max 5): when > 1, also returns a nested `childrenTree`.
  Values < 0 or > 10 return `400 INVALID_PARAMETER`; values 6–10 are clamped to 5.

`GET /api/graph/nodes/{id}/children-transactions`
Returns node id, name, level, and the transactions of the direct children.
Optional filters: `minAmount`, `maxAmount`, `txnType` (SALARY | TRANSFER | POS | ATM).

Errors:
- Unknown id → `404` `{ "error": "NODE_NOT_FOUND", "message": "Graph node N999 does not exist" }`
- Invalid parameter → `400` `{ "error": "INVALID_PARAMETER", ... }`
- Cycle detected → `400` `{ "error": "CYCLE_DETECTED", ... }`

## Example
    GET /api/graph/nodes/N1
    {
      "id": "N1",
      "parentId": null,
      "name": "Node 1",
      "accountNumber": "ACC1001",
      "level": 0,
      "isRoot": true,
      "isLeaf": false,
      "parentChain": [],
      "children": [ { "id": "N2", "level": 1 }, { "id": "N3", "level": 1 } ],
      "transactions": [ ... ],
      "nextLevelTransactions": [ ... ]
    }

## Assumptions
- Level 0 = root (`parentId` null) or orphan (parent id not present in dataset).
- A node's level = 1 + its parent's level.
- `parentChain` is ordered root → direct parent (excludes the node itself).
- `children` are direct children only; `nextLevelTransactions` are their transactions.
- Orphan example: `N19` (parent `N99` missing) → level 0, isRoot true, empty parentChain.
- Cycles are guarded with a visited set (both upward and subtree traversal) and return `400 CYCLE_DETECTED`.
- Child lists are id-sorted for deterministic responses.
- `maxDepth`: default 1, effective max 5; `< 0` or `> 10` rejected with 400; 6–10 clamped to 5.
- Level aggregates use levels relative to the requested node (it is level 0).
- `totalAmount` in aggregates = absolute sum of transaction amounts, regardless of direction.
