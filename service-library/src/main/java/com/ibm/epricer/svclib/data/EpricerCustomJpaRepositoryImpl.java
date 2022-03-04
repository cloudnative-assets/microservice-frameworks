package com.ibm.epricer.svclib.data;

import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

class EpricerCustomJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements EpricerCustomJpaRepository<T, ID> {

    private final EntityManager em;

    EpricerCustomJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager; // can be used by the newly introduced methods
    }

    @Override
    public void detach(T entity) {
        em.detach(entity);
    }

    @Override
    public boolean isManaged(T entity) {
        return em.contains(entity);
    }
    
}
