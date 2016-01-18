package org.sec2.mwserver.core.validators.impl;

import junit.framework.TestCase;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * This JUnit-Class tests the methods of class UnixTimeHeaderValidator.
 *
 * @author nike
 */
public final class UnixTimeHeaderValidatorTest extends TestCase
{
    private static final String KEY = "x-sec2-timestamp";
    private final UnixTimeHeaderValidator validator =
            new UnixTimeHeaderValidator();

    /**
     * This method tests the validate()-method, if it returns no exception
     * for key = KEY and value = 1234-1234
     */
    public void testValidateWithValidValue()
    {
        try
        {
            validator.validate(KEY, "1234-1234");
        }
        catch(HandleRequestException hre)
        {
            fail();
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = 12345678
     */
    public void testValidateWithWrongValue()
    {
        try
        {
            validator.validate(KEY, "123456789");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = abc
     */
    public void testValidateWithWrongValueChar()
    {
        try
        {
            validator.validate(KEY, "abc");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
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
