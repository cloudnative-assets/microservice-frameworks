package com.ibm.epricer.data.pref;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.ibm.epricer.svclib.data.EpricerCustomJpaRepository;

public interface UserRepo extends EpricerCustomJpaRepository<Ctmausr, Long> {

    public Ctmausr getUserByUserid(String userId);

    @Modifying
    @Query("UPDATE Ctmausr u SET u.lastname=?2 WHERE u.userid=?1")
    public void updateUserLastname(String userId, String newLastname);

}

