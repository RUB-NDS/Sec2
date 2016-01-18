package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.util.List;

import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;

/**
 * Interface for XmlHandler. The implementing classes should extend DefaultXmlHandler. This interface adds
 * two methods for better error handling.
 * 
 * @author schuessler
 *
 */
public interface IXmlHandler
{
	/**
	 * Returns true, if an error has occured during the parsing process. Otherwise false.
	 * 
	 * @return true, if an error has occured, otherwise false.
	 */
	public boolean isErrorOccured();

	/**
	 * Returns a list containing all exception which have occured during the parsing process.
	 * 
	 * @return A list with all occured exceptions.
	 */
	public List<XMLParseException> getErrors();
}
