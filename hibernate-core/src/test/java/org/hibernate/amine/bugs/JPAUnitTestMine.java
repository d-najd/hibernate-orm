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
import jakarta.persistence.Version;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Timestamp;
import java.util.stream.Stream;

@DomainModel(annotatedClasses = { JPAUnitTestMine.TestBaseEntity.class, JPAUnitTestMine.TestVersionedEntity.class })
@SessionFactory
public class JPAUnitTestMine {

	@Test
	public void test() {

	}

	@AfterEach
	public void tearDown(SessionFactoryScope scope) {
		scope.getSessionFactory().getSchemaManager().truncate();
	}

	@ParameterizedTest
	@ArgumentsSource(VersionedArgumentsProvider.class)
	public void testVersioned(final LockModeType lockMode, final boolean clear, @NonNull SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			session.persist( new TestVersionedEntity( "test" ) );
			session.flush();

			if ( clear ) {
				session.clear();
			}

			TestVersionedEntity result = session.createNamedQuery( "TestVersionedEntity.findByName",
							TestVersionedEntity.class )
					.setParameter( "name", "test" )
					.setLockMode( lockMode )
					.getSingleResult();

			Assertions.assertEquals( lockMode, session.getLockMode( result ) );
		});
	}

	@ParameterizedTest
	@ArgumentsSource(UnversionedArgumentsProvider.class)
	public void testUnversioned(final LockModeType lockMode, final boolean clear, @NonNull SessionFactoryScope scope) {
		scope.inTransaction(session -> {
			session.persist(new TestBaseEntity("test"));
			session.flush();

			if (clear) {
				session.clear();
			}

			var namedQuery = session.createNamedQuery("TestBaseEntity.findByName", TestBaseEntity.class);
			var parameterSet = namedQuery.setParameter("name", "test");
			var lockSet = parameterSet.setLockMode(lockMode);
			var result = lockSet.getSingleResult();
			var lockModeTest = lockSet.getLockMode();
			var se = session.getCurrentLockMode(result);
			var sessionLockMode = session.getLockMode(result);

/*
	
			 * TestBaseEntity result = session.createNamedQuery("TestBaseEntity.findByName",
			 * TestBaseEntity.class)
			 * .setParameter("name", "test")
			 * .setLockMode(lockMode)
			 * .getSingleResult();
			 */

			Assertions.assertEquals(lockMode, session.getLockMode(result));
		});
	}

	@Entity(name = "TestVersionedEntity")
	@NamedQuery(name = "TestVersionedEntity.findByName", query = "select e from TestVersionedEntity e where e.name = :name")
	public static class TestVersionedEntity {

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

	@Entity(name = "TestBaseEntity")
	@NamedQuery(name = "TestBaseEntity.findByName", query = "select e from TestBaseEntity e where e.name = :name")
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

	public static class VersionedArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Stream.of(
					Arguments.of(LockModeType.NONE, true),
					Arguments.of(LockModeType.OPTIMISTIC, true),
					Arguments.of(LockModeType.OPTIMISTIC_FORCE_INCREMENT, true),
					Arguments.of(LockModeType.PESSIMISTIC_READ, true),
					Arguments.of(LockModeType.PESSIMISTIC_WRITE, true),
					Arguments.of(LockModeType.PESSIMISTIC_FORCE_INCREMENT, true),

					Arguments.of(LockModeType.NONE, false),
					Arguments.of(LockModeType.OPTIMISTIC, false),
					Arguments.of(LockModeType.OPTIMISTIC_FORCE_INCREMENT, false),
					Arguments.of(LockModeType.PESSIMISTIC_READ, false),
					Arguments.of(LockModeType.PESSIMISTIC_WRITE, false),
					Arguments.of(LockModeType.PESSIMISTIC_FORCE_INCREMENT, false));
		}
	}

	public static class UnversionedArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Stream.of(
					Arguments.of(LockModeType.NONE, true),
					// Arguments.of(LockModeType.PESSIMISTIC_READ, true),
					// Arguments.of(LockModeType.PESSIMISTIC_WRITE, true),

					Arguments.of(LockModeType.NONE, false),
					Arguments.of(LockModeType.PESSIMISTIC_READ, false),
					Arguments.of(LockModeType.PESSIMISTIC_WRITE, false));
		}
	}
}
