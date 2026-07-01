package com.assignment.graph;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GraphApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void maxDepth_returnsNestedChildrenTree() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1").param("maxDepth", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childrenTree.id").value("N1"))
                .andExpect(jsonPath("$.childrenTree.children[*].id", containsInAnyOrder("N2", "N3")))
                .andExpect(jsonPath("$.childrenTree.children[0].children").isNotEmpty());
    }

    @Test
    void invalidMaxDepth_returns400() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1").param("maxDepth", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));

        mockMvc.perform(get("/api/graph/nodes/N1").param("maxDepth", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));
    }

    @Test
    void levelAggregates_areComputedForSubtree() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levelAggregates", hasSize(4)))
                .andExpect(jsonPath("$.levelAggregates[0].level").value(0))
                .andExpect(jsonPath("$.levelAggregates[0].nodeCount").value(1));
    }

    @Test
    void childrenTransactions_filterByType() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1/children-transactions").param("txnType", "SALARY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("N1"))
                .andExpect(jsonPath("$.childrenTransactions[*].txnType", everyItem(is("SALARY"))));
    }
}
