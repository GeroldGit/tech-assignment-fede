package com.petmanager.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document representation of a Pet.
 * Used exclusively by the MongoDB persistence adapter; the domain service
 * always works with the {@link com.petmanager.entity.Pet} domain class.
 */
@Document(collection = "pets")
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

    public PetDocument() {
    }

    public PetDocument(final String id, final String name, final String species,
                       final Integer age, final String ownerName) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.age = age;
        this.ownerName = ownerName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(final String species) {
        this.species = species;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }
}
