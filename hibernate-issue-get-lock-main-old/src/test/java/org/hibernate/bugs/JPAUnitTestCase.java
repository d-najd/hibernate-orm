package org.hibernate.bugs;

import java.util.stream.Stream;

import org.hibernate.entity.TestBaseEntity;
import org.hibernate.entity.TestVersionedEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Persistence;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@BeforeEach
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
	}

	@AfterEach
	public void destroy() {
		entityManagerFactory.close();
	}

	/**
	 * Returned lock mode is PESSIMISTIC_FORCE_INCREMENT in 6.6.x in test cases:
	 * [7] NONE, false
	 * [8] OPTIMISTIC, false
	 * [9] OPTIMISTIC_FORCE_INCREMENT, false
	 * [10] PESSIMISTIC_READ, false
	 * [11] PESSIMISTIC_WRITE, false
	 * while it is correct w.r.t. requested one when clearing entity manager
	 * ---> it is necessary to clear entity manager to get requested lock mode
	 *
	 * When requesting PESSIMISTIC_WRITE or PESSIMISTIC_READ, query for update is executed (correct):
	 * Hibernate:
	 *     select
	 *         tve1_0.id,
	 *         tve1_0.name,
	 *         tve1_0.version
	 *     from
	 *         TestVersionedEntity tve1_0
	 *     where
	 *         tve1_0.name=? for update
	 *
	 * When requesting NONE, OPTIMISTIC, OPTIMISTIC_FORCE_INCREMENT, query for update is not executed (correct):
	 * Hibernate:
	 *     select
	 *         tve1_0.id,
	 *         tve1_0.name,
	 *         tve1_0.version
	 *     from
	 *         TestVersionedEntity tve1_0
	 *     where
	 *         tve1_0.name=?
	 */
	@ParameterizedTest
	@ArgumentsSource(VersionedArgumentsProvider.class)
	public void testVersioned(final LockModeType lockMode, final boolean clear) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		try {
			entityManager.persist(new TestVersionedEntity("test"));
			entityManager.flush();

			if (clear) {
				entityManager.clear();
			}

			TestVersionedEntity result = entityManager.createNamedQuery("TestVersionedEntity.findByName", TestVersionedEntity.class)
				.setParameter("name", "test")
				.setLockMode(lockMode)
				.getSingleResult();

			Assertions.assertEquals(lockMode, entityManager.getLockMode(result));
		} finally {
			entityManager.getTransaction().rollback();
			entityManager.close();
		}
	}

	/**
	 * Returned lock mode is PESSIMISTIC_FORCE_INCREMENT in 6.6.x in test cases:
	 * [4] NONE, false
	 * [5] PESSIMISTIC_READ, false
	 * [6] PESSIMISTIC_WRITE, false
	 * while it is correct w.r.t. requested one when clearing entity manager
	 * ---> it is necessary to clear entity manager to get requested lock mode
	 *
	 * Note that PESSIMISTIC_FORCE_INCREMENT for non-versioned entity is neither not supported.
	 *
	 * When requesting PESSIMISTIC_WRITE or PESSIMISTIC_READ, query for update is executed (correct):
	 * Hibernate:
	 *     select
	 *         tbe1_0.id,
	 *         tbe1_0.name
	 *     from
	 *         TestBaseEntity tbe1_0
	 *     where
	 *         tbe1_0.name=? for update
	 *
	 * When requesting NONE, query for update is not executed (correct):
	 * Hibernate:
	 *     select
	 *         tbe1_0.id,
	 *         tbe1_0.name
	 *     from
	 *         TestBaseEntity tbe1_0
	 *     where
	 *         tbe1_0.name=?
	 */
	@ParameterizedTest
	@ArgumentsSource(UnversionedArgumentsProvider.class)
	public void testUnversioned(final LockModeType lockMode, final boolean clear) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		try {
			entityManager.persist(new TestBaseEntity("test"));
			entityManager.flush();

			if (clear) {
				entityManager.clear();
			}

			TestBaseEntity result = entityManager.createNamedQuery("TestBaseEntity.findByName", TestBaseEntity.class)
				.setParameter("name", "test")
				.setLockMode(lockMode)
				.getSingleResult();

			Assertions.assertEquals(lockMode, entityManager.getLockMode(result));
		} finally {
			entityManager.getTransaction().rollback();
			entityManager.close();
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
				Arguments.of(LockModeType.PESSIMISTIC_FORCE_INCREMENT, false)
			);
		}
	}

	public static class UnversionedArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Stream.of(
				Arguments.of(LockModeType.NONE, true),
				Arguments.of(LockModeType.PESSIMISTIC_READ, true),
				Arguments.of(LockModeType.PESSIMISTIC_WRITE, true),

				Arguments.of(LockModeType.NONE, false),
				Arguments.of(LockModeType.PESSIMISTIC_READ, false),
				Arguments.of(LockModeType.PESSIMISTIC_WRITE, false)
			);
		}
	}

}
