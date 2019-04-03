/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal.entity;

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.boot.model.domain.EmbeddedValueMapping;
import org.hibernate.engine.spi.IdentifierValue;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.EmbeddedContainer;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifierCompositeNonAggregated;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.produce.SqmPathRegistry;
import org.hibernate.query.sqm.produce.spi.SqmCreationState;
import org.hibernate.query.sqm.tree.domain.SqmEmbeddedValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.domain.SqmNavigableReference;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.produce.metamodel.spi.Fetchable;
import org.hibernate.type.descriptor.java.spi.EmbeddableJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public class EntityIdentifierCompositeNonAggregatedImpl<O,J>
		implements EntityIdentifierCompositeNonAggregated<O,J> {
	// todo : IdClass handling eventually

	private final EntityHierarchy runtimeModelHierarchy;
	private final EmbeddedTypeDescriptor<J> embeddedDescriptor;

	@SuppressWarnings("unchecked")
	public EntityIdentifierCompositeNonAggregatedImpl(
			EntityHierarchy runtimeModelHierarchy,
			EmbeddedTypeDescriptor<J> embeddedDescriptor,
			EmbeddedValueMapping bootMapping) {
		this.runtimeModelHierarchy = runtimeModelHierarchy;
		this.embeddedDescriptor = embeddedDescriptor;
	}

	@Override
	public EmbeddedTypeDescriptor<J> getEmbeddedDescriptor() {
		return embeddedDescriptor;
	}

	@Override
	public EmbeddedContainer getContainer() {
		return runtimeModelHierarchy.getRootEntityType();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return getEmbeddedDescriptor().getNavigableRole();
	}

	@Override
	public EmbeddableJavaDescriptor<J> getJavaTypeDescriptor() {
		return getEmbeddedDescriptor().getJavaTypeDescriptor();
	}

	@Override
	public String asLoggableText() {
		return "IdentifierCompositeNonAggregated(" + getContainer().asLoggableText() + ")";
	}

	@Override
	public List<Column> getColumns() {
		return getEmbeddedDescriptor().collectColumns();
	}

	@Override
	public boolean canContainSubGraphs() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SingularPersistentAttribute asAttribute(Class javaType) {
		// todo (6.0) : see not on super.
		//		for now throw the exception as JPA defines
		throw new IllegalArgumentException(
				"Non-aggregated composite ID cannot be represented as a single attribute"
		);
	}

	@Override
	public <N> Navigable<N> findNavigable(String navigableName) {
		return getEmbeddedDescriptor().findNavigable( navigableName );
	}

	@Override
	public void visitNavigables(NavigableVisitationStrategy visitor) {
		getEmbeddedDescriptor().visitNavigables( visitor );
	}

	@Override
	public void visitFetchables(Consumer<Fetchable> fetchableConsumer) {
		getEmbeddedDescriptor().visitFetchables( fetchableConsumer );
	}

	@Override
	public EmbeddedTypeDescriptor<J> getNavigableType() {
		return getEmbeddedDescriptor();
	}

	@Override
	public IdentifierGenerator getIdentifierValueGenerator() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			Clause clause,
			SharedSessionContractImplementor session) {
		final Object[] values = (Object[]) value;
		getEmbeddedDescriptor().visitStateArrayContributors(
				contributor -> contributor.dehydrate(
						values[contributor.getStateArrayPosition()],
						jdbcValueCollector,
						clause,
						session
				)
		);
	}

	@Override
	public SqmNavigableReference createSqmExpression(
			SqmPath lhs,
			SqmCreationState creationState) {
		final NavigablePath navigablePath = lhs.getNavigablePath().append( getNavigableName() );
		final SqmPathRegistry pathRegistry = creationState.getProcessingStateStack().getCurrent().getPathRegistry();
		return (SqmNavigableReference) pathRegistry.resolvePath(
				navigablePath,
				np -> new SqmEmbeddedValuedSimplePath(
						navigablePath,
						this,
						lhs
				)
		);
	}

	@Override
	public IdentifierValue getUnsavedValue() {
		return null;
	}
}