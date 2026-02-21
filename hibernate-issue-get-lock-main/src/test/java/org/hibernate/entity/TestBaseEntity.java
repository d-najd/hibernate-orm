package org.hibernate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

@Entity
@NamedQuery(
    name = "TestBaseEntity.findByName",
    query = "select e from TestBaseEntity e where e.name = :name"
)
public class TestBaseEntity {

    @Id
    @GeneratedValue
    public Long id;

    public String name;

    public TestBaseEntity() {
    }

    public TestBaseEntity(final String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
