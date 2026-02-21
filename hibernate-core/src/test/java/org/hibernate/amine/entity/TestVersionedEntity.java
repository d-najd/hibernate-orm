/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.amine.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.testing.orm.junit.Jpa;

@Entity
@NamedQuery(
	name = "TestVersionedEntity.findByName",
	query = "select e from TestVersionedEntity e where e.name = :name"
)
@Table(name = "TestVersionedEntity")
@Jpa(
		annotatedClasses = {
				TestVersionedEntity.class,
		}
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
