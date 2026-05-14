package com.planing.diet_service.Diet.infrastructure.input.rest;

import com.planing.diet.dto.DietRequest;
import com.planing.diet.dto.DietResponse;
import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.domain.exception.OverlappingDietException;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.infrastructure.input.rest.mapper.DietRestMapper;
import com.planing.diet_service.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DietRestAdapterMvpTest {

    @Mock
    private DietInputPort dietInputPort;

    @Mock
    private DietRestMapper dietRestMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DietRestAdapter adapter = new DietRestAdapter(dietInputPort, dietRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/diets maps overlapping active diet to 409")
    void createDietMapsOverlappingDietTo409() throws Exception {
        LocalDate initDate = LocalDate.of(2026, 5, 11);
        LocalDate endDate = LocalDate.of(2026, 5, 17);
        Diet domainRequest = diet(null, "Overlapping", initDate, endDate);

        when(dietRestMapper.toDomain(any(DietRequest.class))).thenReturn(domainRequest);
        when(dietInputPort.createDiet(domainRequest))
                .thenThrow(new OverlappingDietException(initDate, endDate));

        mockMvc.perform(post("/api/v1/diets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Overlapping",
                                  "caloriesTarget": 2000,
                                  "initDate": "2026-05-11",
                                  "endDate": "2026-05-17"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A diet already exists overlapping the requested range 2026-05-11 to 2026-05-17. Delete the existing diet before creating a new overlapping diet."));
    }

    @Test
    @DisplayName("POST /api/v1/diets creates diet and returns generated day count")
    void createDietCreatesDiet() throws Exception {
        Diet domainRequest = diet(null, "Wave 1 Diet", LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 17));
        Diet created = diet(1L, "Wave 1 Diet", LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 17));

        when(dietRestMapper.toDomain(any(DietRequest.class))).thenReturn(domainRequest);
        when(dietInputPort.createDiet(domainRequest)).thenReturn(created);
        when(dietRestMapper.toResponse(created)).thenReturn(new DietResponse()
                .id(1L)
                .name("Wave 1 Diet")
                .caloriesTarget(2000)
                .initDate(LocalDate.of(2026, 5, 11))
                .endDate(LocalDate.of(2026, 5, 17))
                .totalDays(7));

        mockMvc.perform(post("/api/v1/diets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Wave 1 Diet",
                                  "caloriesTarget": 2000,
                                  "initDate": "2026-05-11",
                                  "endDate": "2026-05-17"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalDays").value(7));
    }

    @Test
    @DisplayName("POST /api/v1/diets maps invalid date range to 400")
    void createDietMapsInvalidDateRangeTo400() throws Exception {
        Diet domainRequest = diet(null, "Invalid", LocalDate.of(2026, 5, 17), LocalDate.of(2026, 5, 11));

        when(dietRestMapper.toDomain(any(DietRequest.class))).thenReturn(domainRequest);
        when(dietInputPort.createDiet(domainRequest))
                .thenThrow(new IllegalArgumentException("endDate cannot be before initDate."));

        mockMvc.perform(post("/api/v1/diets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid",
                                  "caloriesTarget": 2000,
                                  "initDate": "2026-05-17",
                                  "endDate": "2026-05-11"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("endDate cannot be before initDate."));
    }

    @Test
    @DisplayName("GET /api/v1/diets/range maps invalid range to 400")
    void getDietsByDateRangeMapsInvalidRangeTo400() throws Exception {
        when(dietInputPort.getDietsByDateRange(LocalDate.of(2026, 5, 17), LocalDate.of(2026, 5, 11)))
                .thenThrow(new IllegalArgumentException("'to' date cannot be before 'from' date."));

        mockMvc.perform(get("/api/v1/diets/range")
                        .queryParam("from", "2026-05-17")
                        .queryParam("to", "2026-05-11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("'to' date cannot be before 'from' date."));
    }

    private Diet diet(Long id, String name, LocalDate initDate, LocalDate endDate) {
        Diet diet = new Diet();
        diet.setId(id);
        diet.setName(name);
        diet.setCaloriesTarget(2000);
        diet.setInitDate(initDate);
        diet.setEndDate(endDate);
        return diet;
    }
}
