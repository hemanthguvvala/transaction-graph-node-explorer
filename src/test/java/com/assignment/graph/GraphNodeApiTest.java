package com.assignment.graph;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GraphNodeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootNode_hasLevelZeroAndDirectChildren() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(0))
                .andExpect(jsonPath("$.isRoot").value(true))
                .andExpect(jsonPath("$.isLeaf").value(false))
                .andExpect(jsonPath("$.parentChain").isEmpty())
                .andExpect(jsonPath("$.children[*].id", containsInAnyOrder("N2", "N3")));
    }

    @Test
    void deepNode_hasCorrectLevelAndOrderedParentChain() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.parentChain[*].id", contains("N1", "N2", "N4")))
                .andExpect(jsonPath("$.isLeaf").value(true));
    }

    @Test
    void orphanNode_isTreatedAsRoot() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(0))
                .andExpect(jsonPath("$.isRoot").value(true))
                .andExpect(jsonPath("$.parentChain").isEmpty());
    }

    @Test
    void unknownNode_returns404() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NODE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Graph node N999 does not exist"));
    }
}
