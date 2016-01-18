package org.sec2.mwserver.core.validators.impl;

import junit.framework.TestCase;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * This JUnit-Class tests the methods of class AppNameHeaderValidator.
 *
 * @author nike
 */
public final class AppNameHeaderValidatorTest extends TestCase
{
    private static final String KEY = "x-sec2-appName";
    private final AppNameHeaderValidator validator =
            new AppNameHeaderValidator();

    /**
     * This method tests the validate()-method, if it returns no exception
     * for key = x-sec2-appName and value = com.example.app
     */
    public void testValidateWithValidValue()
    {
        try
        {
            validator.validate(KEY, "com.example.app");
        }
        catch(HandleRequestException hre)
        {
            fail();
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = x-sec2-appName and value = com/example/app
     */
    public void testValidateWithWrongValue()
    {
        try
        {
            validator.validate(KEY, "com/example/app");
            fail();
        }
        catch(HandleRequestException hre)
        {
            assertTrue(true);
        }
    }

    /**
     * This method tests the validate()-method, if it returns exception
     * for key = x-sec2-appName and value = [empty]
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
     * for key = x-sec2-appName and value = NULL
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
