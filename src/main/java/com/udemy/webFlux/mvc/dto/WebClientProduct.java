package com.udemy.webFlux.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebClientProduct {
    private String id;
    private String name;
    private String description;
    private String price;
}
