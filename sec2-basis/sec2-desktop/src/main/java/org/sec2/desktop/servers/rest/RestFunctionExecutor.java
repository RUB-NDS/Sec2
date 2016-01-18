package org.sec2.desktop.servers.rest;

import org.sec2.desktop.util.CryptoUtils;
import org.sec2.mwserver.core.rest.AbstractRestFunctionExecutor;
import org.sec2.mwserver.core.util.ICryptoUtils;

/**
 * This class implements the abstract methods of class "AbstractRestFunctionExecutor" for desktop platforms.
 * 
 * @author schuessler
 */
public final class RestFunctionExecutor extends AbstractRestFunctionExecutor
{
    private ICryptoUtils cryptoUtils = null;

    @Override
    protected boolean allowAppToRegister(final String appName)
    {
        if (appName != null && !appName.isEmpty())
        {
            System.out.println("Die App mit dem Namen \"" + appName
                    + "\" möchte sich registrieren. Erlauben?");
            System.out.println("*Benutzer hat \"Ja\" gedrückt...*");
            return true;
        }
        else
        {
            System.out.println("Fehler bei der Registrierung: Appname darf nicht leer sein!");
            return false;
        }
    }

    @Override
    protected ICryptoUtils getCryptoUtils()
    {
        if(cryptoUtils == null) cryptoUtils = new CryptoUtils();

        return cryptoUtils;
    }
}
