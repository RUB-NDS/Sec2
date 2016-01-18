
package de.adesso.mobile.android.sec2.xml;

import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * Utility class to create querys
 * 
 * @author hoppe
 *
 */
public final class QueryBuilder {

    /**
     * default contructor
     */
    private QueryBuilder() {

    }

    /**
     * method to create a query to get all unencrypted and encrypted items depeneding on the given prefix
     * 
     * @param groups the groups where a user is member
     * @param prefix f.e. "?_query=/<item>"
     * @return xPathQuery for getting a list of items
     */
    public static String createQuery(final Group[] groups, final String prefix) {
        final StringBuilder xPathQuery = new StringBuilder();
        xPathQuery.append(prefix + "[");

        if (groups != null) {
            xPathQuery.append("encryptionParts[@*[local-name()='groups']=");
            if (groups.length == 1) {
                xPathQuery.append(String.valueOf("'" + groups[0].getGroupId() + "'"));
            } else {
                xPathQuery.append(String.valueOf("'" + groups[0].getGroupId() + "'"));
                for (int i = 1; i < groups.length; i++) {
                    xPathQuery.append("or");
                    xPathQuery.append(String.valueOf("'" + groups[i].getGroupId()
                            + "'"));
                }
            }
            xPathQuery.append("]%20or%20");
        }
        xPathQuery.append("not(encryptionParts)]");

        return xPathQuery.toString();
    }

    /**
     * method to create a query to get all unencrypted items depending on the given prefix
     * 
     * @param prefix f.e. "?_query=/<item>"
     * @return xPathQuery for getting a list of items
     */
    public static String createQueryUencrypted(final String prefix) {
        final StringBuilder xPathQuery = new StringBuilder();
        xPathQuery.append(prefix + "[");
        xPathQuery.append("not(encryptionParts)]");
        return xPathQuery.toString();
    }

    /**
     * method to create a query to get all unencrypted items depending on the given prefix
     * 
     * @param groups the groups where a user is member
     * @param prefix f.e. "?_query=/<item>"
     * @return xPathQuery for getting a list of items
     */
    public static String createQueryEncrypted(final Group[] groups, final String prefix) {
        final StringBuilder xPathQuery = new StringBuilder();
        xPathQuery.append(prefix + "[");

        if (groups != null) {
            xPathQuery.append("encryptionParts[@*[local-name()='groups']=");
            if (groups.length == 1) {
                xPathQuery.append(String.valueOf("'" + groups[0].getGroupId() + "'"));
            } else {
                xPathQuery.append(String.valueOf("'" + groups[0].getGroupId() + "'"));
                for (int i = 1; i < groups.length; i++) {
                    xPathQuery.append("or");
                    xPathQuery.append(String.valueOf("'" + groups[i].getGroupId()
                            + "'"));
                }
            }
            xPathQuery.append("]]");
        }

        return xPathQuery.toString();
    }

}
