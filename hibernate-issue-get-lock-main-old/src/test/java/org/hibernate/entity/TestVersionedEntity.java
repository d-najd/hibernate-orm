package org.hibernate.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Version;

@Entity
@NamedQuery(
    name = "TestVersionedEntity.findByName",
    query = "select e from TestVersionedEntity e where e.name = :name"
)
public class TestVersionedEntity {

    @Id
    @GeneratedValue
    public Long id;

    @Version
    private Timestamp version;

    public String name;

    public TestVersionedEntity() {
    }

    public TestVersionedEntity(final String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Timestamp getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }
}
