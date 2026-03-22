package com.petmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    private String name;
    private String species;
    private Integer age;
    private String ownerName;
}
