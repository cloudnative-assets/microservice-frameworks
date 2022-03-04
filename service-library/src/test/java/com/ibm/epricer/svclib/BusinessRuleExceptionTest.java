package com.ibm.epricer.svclib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class BusinessRuleExceptionTest {
    private static final String MSG = "Business rule broken";
    private static final String SVCID = "epricer-hello";
    private static final String CODE = SVCID + ":91";

    @Test
    void sunnyDayUnmarshall() {
        ExceptionState state = new ExceptionState();
        state.code = CODE;
        state.message = MSG;

        BusinessRuleException ex = new BusinessRuleException(state);
        assertEquals(91, ex.getCode(), "Wrong code");
    }

    @Test
    void sunnyDayMarshall() {
        BusinessRuleException ex = new BusinessRuleException(91, MSG);
        ExceptionState state = ex.export(SVCID);
        assertEquals(CODE, state.code, "Wrong serialized exception code");
    }

    @Test
    void illegalState() {
        ExceptionState state1 = new ExceptionState();
        state1.code = "epricer-hello:0";
        state1.message = MSG;
        assertThrows(IllegalArgumentException.class, () -> {
            new BusinessRuleException(state1);
        });

        ExceptionState state2 = new ExceptionState();
        state2.code = "epricer-hello";
        state2.message = MSG;
        assertThrows(IllegalArgumentException.class, () -> {
            new BusinessRuleException(state2);
        });
    }
}
