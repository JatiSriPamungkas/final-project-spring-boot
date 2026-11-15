package com.final_project.product_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyProductItem(
        Long id,
        String title,
        String description,
        String category,
        Double price
) {}
