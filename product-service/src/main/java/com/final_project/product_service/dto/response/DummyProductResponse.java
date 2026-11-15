package com.final_project.product_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyProductResponse(
        List<DummyProductItem> products,
        Integer total,
        Integer skip,
        Integer limit
) {}
