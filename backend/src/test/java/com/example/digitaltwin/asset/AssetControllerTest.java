package com.example.digitaltwin.asset;

import com.example.digitaltwin.history.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AssetRepository repository;
    @MockitoBean
    private HistoryService history;

    @Test
    void listWithoutAtReturnsCurrentAssets() throws Exception {
        when(repository.findAll()).thenReturn(List.of(asset("Live pump")));

        mvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Live pump"));

        verify(history, never()).stateAt(any());
    }

    @Test
    void listWithAtReturnsHistoricalStateInTheSameShape() throws Exception {
        Instant at = Instant.parse("2026-10-10T14:03:00Z");
        when(history.stateAt(at)).thenReturn(List.of(asset("Past pump")));

        mvc.perform(get("/api/assets").param("at", "2026-10-10T14:03:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Past pump"))
                .andExpect(jsonPath("$[0].temperature").value(21.5));

        verify(repository, never()).findAll();
    }

    @Test
    void listWithInvalidAtIsBadRequest() throws Exception {
        mvc.perform(get("/api/assets").param("at", "yesterday"))
                .andExpect(status().isBadRequest());
    }

    private static Asset asset(String name) {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setName(name);
        asset.setShape(Asset.Shape.BOX);
        asset.setColor("#0000ff");
        asset.setTemperature(21.5);
        return asset;
    }
}
