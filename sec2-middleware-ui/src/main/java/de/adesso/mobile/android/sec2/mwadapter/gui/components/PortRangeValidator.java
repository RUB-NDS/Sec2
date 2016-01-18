package de.adesso.mobile.android.sec2.mwadapter.gui.components;

/**
 * Validator for an EditText for entering a port. The class checks, whether a valid port number was entered. A port
 * number is valid, if it is an integer value between 1 and 65535.
 * @author nike
 *
 */
public class PortRangeValidator implements IEditTextValidator
{
	/* (non-Javadoc)
	 * @see de.adesso.mobile.android.sec2.mwadapter.gui.components.IEditTextValidator#validate(java.lang.String)
	 */
	@Override
	public boolean validate(String text)
	{
		int port = 0;

		try
		{
			port = Integer.parseInt(text);
			if(port < 1 || port > 65535) return false;
			else return true;
		}
		catch(final NumberFormatException nfe)
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
		return "Port muß im Bereich 1 bis 65535 liegen.";
	}

}
