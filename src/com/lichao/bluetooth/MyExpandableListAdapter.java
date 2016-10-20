package com.lichao.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.webkit.WebView.FindListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

public class MyExpandableListAdapter extends BaseExpandableListAdapter{
    private int[] parentTo;
    private String[] parentFrom;
    private int[] childrenTo;
    private String[] childrenFrom;

    private List<? extends Map<String, ?>> parentData;
    private ArrayList<ArrayList<HashMap<String, Object>>> childrenData;

    private int parentResource;
    private int childrenResource;
    private LayoutInflater mInflater;
    
    public MyExpandableListAdapter(Context context, 
    		List<? extends Map<String, ?>> data_parent,int resource_parent, String[] from_parent, int[] to_parent,
    				ArrayList<ArrayList<HashMap<String, Object>>> data_children,int resource_children, String[] from_children, int[] to_children) {
    	parentData = data_parent;
    	parentResource = resource_parent;
    	parentFrom = from_parent;
    	parentTo = to_parent;
    	
    	childrenData = data_children;
    	childrenResource = resource_children;
    	childrenFrom = from_children;
    	childrenTo = to_children;
    	
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

	@Override
	public int getGroupCount() {
		return parentData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(childrenData.size()<=groupPosition){
			return 0;
		}
		return childrenData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return childrenData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childrenData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view = null;
		HashMap<String, Object> map = (HashMap<String, Object>) parentData.get(groupPosition);
		view = mInflater.inflate(parentResource, null);//生成List用户条目布局对象
		TextView name = (TextView)view.findViewById(parentTo[0]);//昵称
		TextView count = (TextView)view.findViewById(parentTo[1]);//登录时间
		name.setText((String) map.get(parentFrom[0]));
		count.setText(getChildrenCount(groupPosition)+"");
        return view;
	}
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View view = null;
		HashMap<String, Object> map = childrenData.get(groupPosition).get(childPosition);
		view = mInflater.inflate(childrenResource, null);//生成List用户条目布局对象
		ImageView IconView = (ImageView)view.findViewById(childrenTo[0]);//头像
		TextView name = (TextView)view.findViewById(childrenTo[1]);//昵称
		TextView address = (TextView)view.findViewById(childrenTo[2]);//登录时间
		IconView.setImageResource((Integer) map.get(childrenFrom[0]));
		name.setText((String) map.get(childrenFrom[1]));
		address.setText((String) map.get(childrenFrom[2]));
        return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}


}
