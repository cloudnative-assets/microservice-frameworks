package com.ibm.epricer.svclib.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EpricerCustomJpaRepository<T, ID> extends JpaRepository<T, ID> {

	public void detach(T entity);
	
	public boolean isManaged(T entity);
	
}
