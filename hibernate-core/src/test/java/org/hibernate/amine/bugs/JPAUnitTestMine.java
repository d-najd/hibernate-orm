/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.amine.bugs;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NamedQuery;
import org.hibernate.amine.entity.TestBaseEntity;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;


@DomainModel(
		annotatedClasses = JPAUnitTestMine.TestBaseEntity.class
)
@SessionFactory
public class JPAUnitTestMine {
	@ParameterizedTest
	@ArgumentsSource(UnversionedArgumentsProvider.class)
	public void test(final LockModeType lockMode, final boolean clear, SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
						session.persist(new TestBaseEntity("test"));
						session.flush();

						if (clear) {
							session.clear();
						}

						TestBaseEntity result = session.createNamedQuery("TestBaseEntity.findByName", TestBaseEntity.class)
								.setParameter("name", "test")
								.setLockMode(lockMode)
								.getSingleResult();

						Assertions.assertEquals(lockMode, session.getLockMode(result));
				}
		);
	}

	@Entity(name = "TestBaseEntity")
	@NamedQuery(
			name = "TestBaseEntity.findByName",
			query = "select e from TestBaseEntity e where e.name = :name"
	)
	public static class TestBaseEntity {

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

	public static class UnversionedArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Stream.of(
					// Arguments.of( LockModeType.NONE, true),
					// Arguments.of(LockModeType.PESSIMISTIC_READ, true),
					// Arguments.of(LockModeType.PESSIMISTIC_WRITE, true),

					Arguments.of(LockModeType.NONE, false)
					// Arguments.of(LockModeType.PESSIMISTIC_READ, false),
					// Arguments.of(LockModeType.PESSIMISTIC_WRITE, false)
			);
		}
	}
}
