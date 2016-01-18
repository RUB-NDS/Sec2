package de.adesso.mobile.android.sec2.mwadapter.gui.components;

import java.util.regex.Pattern;

/**
 * Validator for an EditText for entering a host name. The class checks, whether a valid host name was entered. A host name
 * is either an IP address or a name like "example.com".
 * @author nike
 *
 */
public class HostNameValidator implements IEditTextValidator
{
	private static final Pattern HOST_NAME = Pattern.compile("(localhost)|([a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,4})");
	private static final Pattern IP = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

	/* (non-Javadoc)
	 * @see de.adesso.mobile.android.sec2.mwadapter.gui.components.IEditTextValidator#validate(java.lang.String)
	 */
	@Override
	public boolean validate(String text)
	{
		String[] ipNumbers = null;

		try
		{
			if(HOST_NAME.matcher(text).matches()) return true;
			if(IP.matcher(text).matches())
			{
				ipNumbers = text.split("\\.");
				for(int i = 0; i < 4; i++)
				{
					if(Integer.parseInt(ipNumbers[i]) > 255) return false;
				}
				return true;
			}
			return false;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see de.adesso.mobile.android.sec2.mwadapter.gui.components.IEditTextValidator#getErrorMsg()
	 */
	@Override
	public String getErrorMsg()
	{
		return "Hostname nicht gültig - muß eine IP-Adresse sein oder in der Form \"example.com\".";
	}

}
