package de.adesso.mobile.android.sec2.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;

import de.adesso.mobile.android.sec2.model.TwoLineListItemData;

public class TwoLineListArrayAdapter<T extends TwoLineListItemData> extends ArrayAdapter<T>
{
	private final int resourceId;
	
	public TwoLineListArrayAdapter(Context context, int textViewResourceId, List<T> objects)
	{
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{	 
		TwoLineListItem view;
		TwoLineListItemData item = getItem(position);
		// We need the layoutinflater to pick up the view from xml
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		// if the array item is null, nothing to display, just return null
		if (item == null) return null;
 
		

		// Pick up the TwoLineListItem defined in the xml file
		if (convertView == null) view = (TwoLineListItem) inflater.inflate(resourceId, parent, false);
		else view = (TwoLineListItem) convertView;
 
		// Set value for the first text field
		if (view.getText1() != null) view.getText1().setText(item.getFirstLine());
 
		// set value for the second text field
		if (view.getText2() != null) view.getText2().setText(item.getSecondLine());
 
		return view;
	}
}
