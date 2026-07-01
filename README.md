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

## Endpoint
`GET /api/graph/nodes/{id}` — returns node details, level, parentChain, direct
children, the node's transactions, and next-level (children's) transactions.

Unknown id returns `404`:
`{ "error": "NODE_NOT_FOUND", "message": "Graph node N999 does not exist" }`

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
- Cycles are guarded with a visited set and return `400 CYCLE_DETECTED`.
- Child lists are id-sorted for deterministic responses.
