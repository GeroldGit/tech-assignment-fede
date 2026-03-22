package com.petmanager.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document representation of a Pet.
 * Used exclusively by the MongoDB persistence adapter; the domain service
 * always works with the {@link com.petmanager.entity.Pet} domain class.
 */
@Document(collection = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetDocument {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("species")
    private String species;

    @Field("age")
    private Integer age;

    @Field("owner_name")
    private String ownerName;
}
