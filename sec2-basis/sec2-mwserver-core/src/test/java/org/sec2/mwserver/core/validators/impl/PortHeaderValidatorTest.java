package org.sec2.mwserver.core.validators.impl;

import junit.framework.TestCase;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * This JUnit-Class tests the methods of class PortHeaderValidator.
 *
 * @author nike
 */
public final class PortHeaderValidatorTest extends TestCase
{
    private static final String KEY = "x-sec2-socketport";
    private final PortHeaderValidator validator = new PortHeaderValidator();

    /**
     * This method tests the validate()-method, if it returns no exception
     * for key = KEY and value = 1
     */
    public void testValidateWithValidValue1()
    {
        try
        {
            validator.validate(KEY, "1");
        }
        catch(HandleRequestException hre)
        {
            fail();
        }
    }

    /**
     * This method tests the validate()-method, if it returns no exception
     * for key = KEY and value = 65535
     */
    public void testValidateWithValidValue65535()
    {
        try
        {
            validator.validate(KEY, "65535");
        }
        catch(HandleRequestException hre)
        {
            fail();
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = 0
     */
    public void testValidateWithWrongValue0()
    {
        try
        {
            validator.validate(KEY, "0");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = 65536
     */
    public void testValidateWithWrongValue65536()
    {
        try
        {
            validator.validate(KEY, "65536");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = -1
     */
    public void testValidateWithWrongValueMinus1()
    {
        try
        {
            validator.validate(KEY, "-1");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = KEY and value = 0
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
