package com.group18.xantrex_calculator.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void roleEnumHasThreeValues() {
        assertEquals(3, Role.values().length,
                "Role enum must contain exactly three values: CLIENT, INTERN, USER");
    }

    @Test
    void roleEnumContainsExpectedValues() {
        assertNotNull(Role.CLIENT);
        assertNotNull(Role.INTERN);
        assertNotNull(Role.USER);
    }
}
