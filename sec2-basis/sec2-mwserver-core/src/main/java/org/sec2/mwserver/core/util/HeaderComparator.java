package org.sec2.mwserver.core.util;

import java.util.Comparator;

/**
 * This class implements the Comparator-interface. It compares, if two
 * HTTP-header-values are lexicographically equal.
 *
 * @author nike
 */
public class HeaderComparator implements Comparator<String>
{
	@Override
	public int compare(final String o1, final String o2)
	{
		if (o1 == null || o1.isEmpty())
		{
			if (o2 == null || o2.isEmpty())
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			if (o2 == null || o2.isEmpty())
			{
				return 1;
			}
			else
			{
				return o1.compareTo(o2);
			}
		}
	}
}
