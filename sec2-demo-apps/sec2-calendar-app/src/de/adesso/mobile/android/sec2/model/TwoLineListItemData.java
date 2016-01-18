package de.adesso.mobile.android.sec2.model;

public class TwoLineListItemData
{
	private String firstLine = null;
	private String secondLine = null;
	
	public TwoLineListItemData(String firstLine, String secondLine)
	{
		this.firstLine = firstLine;
		this.secondLine = secondLine;
	}

	public String getFirstLine() {
		return firstLine;
	}

	public void setFirstLine(String firstLine) {
		this.firstLine = firstLine;
	}

	public String getSecondLine() {
		return secondLine;
	}

	public void setSecondLine(String secondLine) {
		this.secondLine = secondLine;
	}
}
