package com.pdp.orthex.fragmentedapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHashMap;
    public static final int TAG_NAME = 0;
    public static final int TAG_DATE = 300;
    public static final int TAG_DELETE = 600;
    public static final int TAG_DISPOSE = 10000;
    public static final int TAG_EDIT = 20000;


    int i = 0;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap){
        this.context =context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;

    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).get(childPosition);
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
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String)getGroup(groupPosition);
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group,null);
        }

        TextView listHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        listHeader.setTypeface(null, Typeface.BOLD);
        listHeader.setText(headerTitle);
        ImageView imgListGroup = (ImageView)convertView.findViewById(R.id.lblHeaderColor);
        if (groupPosition==0){
            imgListGroup.setBackgroundResource(R.drawable.orthex_orange_box);
        }
        if (groupPosition==1){
            imgListGroup.setBackgroundResource(R.drawable.orthex_green_box);
        }
        if (groupPosition==2){
            imgListGroup.setBackgroundResource(R.drawable.orthex_blue_box);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition,childPosition);
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,null);
        }
        String[] txt1 = YourSingleton.getItemInfo(childText);
        int a = txt1.length;
        int id1 = 1000*groupPosition+childPosition+1000;
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        Button btnDispose = (Button)convertView.findViewById(R.id.btnItemDispose);
        Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
        btnDelete.setTag(id1+TAG_DELETE); txtListChild.setTag(id1+TAG_NAME); txtChildDate.setTag(id1+TAG_DATE);
        btnDispose.setTag(id1+TAG_DISPOSE);btnEdit.setTag(id1+TAG_EDIT);
        txtListChild.setText(txt1[0]);
        txtChildDate.setText("");
        if(a >=2){
            String txtdate = txt1[1];
            try {
                DateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                Date txtDate1 = formatter.parse(txtdate);
                Date currentdate = Calendar.getInstance().getTime();
                long diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                String strDays = "Days left: " + Long.toString(diff);
                txtChildDate.setText(strDays);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        a=0; txt1=null;
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
