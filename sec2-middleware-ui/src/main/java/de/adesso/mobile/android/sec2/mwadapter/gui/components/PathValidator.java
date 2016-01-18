package de.adesso.mobile.android.sec2.mwadapter.gui.components;

import java.util.regex.Pattern;

/**
 * Validator for an EditText for entering a path. The class checks, whether a valid path was entered. A path
 * is valid if it is either empty or of the form "/[word]/[word].
 * @author nike
 *
 */
public class PathValidator implements IEditTextValidator
{
    private static final Pattern PATH = Pattern.compile("(/\\w+)+/?");
    /* (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.mwadapter.gui.components.IEditTextValidator#validate(java.lang.String)
     */
    @Override
    public boolean validate(final String text)
    {
        return text.isEmpty() || text.equals("/") || PATH.matcher(text).matches();
    }

    /* (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.mwadapter.gui.components.IEditTextValidator#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return "Pfad ist nicht gültig. Pfade dürfen nur a-z, A-Z, 0-9, \"_\" und \"/\" enthalten.";
    }

}
