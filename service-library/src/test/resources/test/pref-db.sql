CREATE SCHEMA EPRICER;
COMMIT;

CREATE TABLE EPRICER.CTMAUSR (
	CTMAUSRID BIGINT NOT NULL,
	USERID CHAR(10) NOT NULL,
	FIRSTNAME VARCHAR(50) CHECK (FIRSTNAME IS NOT NULL AND CHARACTER_LENGTH(FIRSTNAME) > 0),
	LASTNAME VARCHAR(50) CHECK (LASTNAME IS NOT NULL AND CHARACTER_LENGTH(LASTNAME) > 0)
);
COMMIT;

INSERT INTO EPRICER.CTMAUSR VALUES(1, 'MT','Michael', 'Topchiev');
INSERT INTO EPRICER.CTMAUSR VALUES(2, 'MB','Michael', 'Bloomberg');
INSERT INTO EPRICER.CTMAUSR VALUES(3, 'MJ','Michael', 'Jackson');
COMMIT;
