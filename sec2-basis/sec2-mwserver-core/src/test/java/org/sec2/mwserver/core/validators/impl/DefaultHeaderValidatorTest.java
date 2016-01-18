package org.sec2.mwserver.core.validators.impl;

import junit.framework.TestCase;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * This JUnit-Class tests the methods of class DefaultHeaderValidator.
 *
 * @author nike
 */
public final class DefaultHeaderValidatorTest extends TestCase
{
    private static final String KEY = "test";
    private final DefaultHeaderValidator validator =
            new DefaultHeaderValidator();

    /**
     * This method tests the validate()-method, if it returns no exception
     * for key = KEY and value = test
     */
    public void testValidateWithValidValue()
    {
        try
        {
            validator.validate(KEY, "test");
        }
        catch(HandleRequestException hre)
        {
            fail();
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = ""
     */
    public void testValidateWithEmptyValue()
    {
        try
        {
            validator.validate(KEY, "");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = NULL
     */
    public void testValidateWithNullValue()
    {
        try
        {
            validator.validate(KEY, null);
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }
}
