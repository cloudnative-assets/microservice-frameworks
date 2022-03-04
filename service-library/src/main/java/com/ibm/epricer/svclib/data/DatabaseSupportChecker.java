package com.ibm.epricer.svclib.data;

import javax.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Check data packages to find out if database-related classes exists.
 * 
 * @author Kiran Chowdhury
 */

public class DatabaseSupportChecker {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSupportChecker.class);

    public static boolean isTxdbEnabled() {
        return FieldHolder.TXDB_ENABLED;
    }

    public static boolean isPrefEnabled() {
        return FieldHolder.PREF_ENABLED;
    }

    public static boolean isPricEnabled() {
        return FieldHolder.PRIC_ENABLED;
    }

    public static boolean isCustEnabled() {
        return FieldHolder.CUST_ENABLED;
    }

    public static boolean isDatabaseEnabled() {
        return FieldHolder.TXDB_ENABLED || FieldHolder.PREF_ENABLED || 
                FieldHolder.PRIC_ENABLED || FieldHolder.CUST_ENABLED;
    }

    /*
     * Lazy initialization holder class
     */
    private static class FieldHolder {
        static final boolean TXDB_ENABLED = checkIfEntityClassesExist(TxdbDataConfig.BASE_PACKAGE);
        static final boolean PREF_ENABLED = checkIfEntityClassesExist(PrefDataConfig.BASE_PACKAGE);
        static final boolean PRIC_ENABLED = checkIfEntityClassesExist(PricDataConfig.BASE_PACKAGE);
        static final boolean CUST_ENABLED = checkIfEntityClassesExist(CustDataConfig.BASE_PACKAGE);

        private static boolean checkIfEntityClassesExist(String dataPackageName) {
            /*
             * First look if any entity classes exist
             */
            try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(dataPackageName).scan()) {
                ClassInfoList entityList = scanResult.getClassesWithAnnotation(Entity.class.getName());
                long found = entityList.stream().filter(ci -> ci.getPackageName().startsWith(dataPackageName)).count();
                if (found > 0) {
                    LOG.info("Database transactions for {} ENABLED (found {} entities)", dataPackageName, found);
                    return true;
                }
            }
            /*
             * Next look if any JPA repositories exist
             */
            try (ScanResult scanResult = new ClassGraph().acceptPackages(dataPackageName).scan()) {
                ClassInfoList repoClasses = scanResult.getClassesImplementing(JpaRepository.class.getName());
                long found = repoClasses.stream().filter(ci -> ci.getPackageName().startsWith(dataPackageName)).count();
                if (found > 0) {
                    LOG.info("Database transactions for {} ENABLED (found {} repositories)", dataPackageName, found);
                    return true;
                }
            }
            LOG.info("Database transactions for {} DISABLED", dataPackageName);
            return false; // neither entities nor repositories are found
        }
    }
}
