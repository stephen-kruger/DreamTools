package com.madibasoft.dreamtools.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.madibasoft.dreamtools.R;
import com.webhiker.enigma2.api.MovieObject;
import com.webhiker.enigma2.api.ServiceObject;


public class ExpandableListAdapter extends BaseExpandableListAdapter {

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private Context context;

	private ArrayList<ServiceObject> groups;

	private ArrayList<ArrayList<ServiceObject>> children;

	public ExpandableListAdapter(Context context, ArrayList<ServiceObject> groups,ArrayList<ArrayList<ServiceObject>> children) {
		this.context = context;
		this.groups = groups;
		this.children = children;
	}

	/**
	 * A general add method, that allows you to add a Service to this list
	 * 
	 * Depending on if the bouquet of the service is present or not,
	 * the corresponding item will either be added to an existing group if it 
	 * exists, else the group will be created and then the item will be added
	 * @param service
	 */
	public void addItem(ServiceObject bouquet, ServiceObject service) {
		addItem(bouquet);
		int index = groups.indexOf(bouquet);
		if (index>=0) {
			children.get(index).add(service);
		}
		else {
			Log.e("expandablelistadapter", "tried to add service to non-existent bouqet");
		}

	}

	public void addItem(ServiceObject bouquet) {
		//		Log.d("explistadapter","Adding bouquet "+bouquet.getName());
		if (!groups.contains(bouquet)) {
			groups.add(bouquet);
		}
		int index = groups.indexOf(bouquet);
		if (children.size() < index + 1) {
			children.add(new ArrayList<ServiceObject>());
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return children.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// Return a child view. You can load your custom layout here.
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ServiceObject service = (ServiceObject) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.ex_list_child, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.serviceItem);
		if (service instanceof MovieObject) {
			tv.setText(((MovieObject)service).getTitle()+" ("+service.getName()+")");
		}
		else {
			tv.setText(service.getName());
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// Return a group view. You can load your custom layout here.
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		ServiceObject group = (ServiceObject) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.ex_list_group, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.bouquetItem);
		tv.setText(group.getName());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

}
