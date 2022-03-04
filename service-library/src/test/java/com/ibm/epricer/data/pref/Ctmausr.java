package com.ibm.epricer.data.pref;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the CTMAUSR database table.
 * 
 */
@Entity
@Table(schema = "epricer")
public class Ctmausr implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String ctmaUsrId;

    public String userid;

    public String firstname;

    public String lastname;
}
