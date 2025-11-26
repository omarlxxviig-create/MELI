package com.meli.inventory_service.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenInfo {
    private String googleUid; // sub claim
    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;
}
