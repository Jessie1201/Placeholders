package pdp.placeholders;

import android.content.Context;
import android.graphics.Typeface;
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

/**
 This file helps create the ExpandableList in the main activity.
 Most of the functions are from other examples.
 The parts that are special for this app are: getGroupView  and  getChildView
 */

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
        // This function creates the group headers in the expandableListView.
        String headerTitle = (String)getGroup(groupPosition);
        // First it checks if an existing view template can be found.
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Looks up the list group that is defined in the layout resources.
            convertView = inflater.inflate(R.layout.list_group,null);
        }
        TextView listHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        listHeader.setTypeface(null, Typeface.BOLD);
        listHeader.setText(headerTitle);
        // Selects the resource for the colored ball next to each header
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
    /**This function creates the views for the children of each group.*/
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition,childPosition);
        /*     Each stored item is saved as a string in the following format:
         *      item name;/; date expires ;/; date added
         *      txt1 is the array that you get when you split the data with ;/; */
        String[] txt1 = YourSingleton.getItemInfo(childText);
        int a = txt1.length;
        // Gets the layout resource for the child item
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,null);
        }
        /* Each view item is given a tag
        *  This is because we want to change their individual attributes later
        */
        int id1 = 1000*groupPosition+childPosition+1000;
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        Button btnDispose = (Button)convertView.findViewById(R.id.btnItemDispose);
        Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
        txtChildDate.setTag(id1+TAG_DATE);
        txtListChild.setTag(id1+TAG_NAME);
        btnDelete.setTag(id1+TAG_DELETE);
        btnDispose.setTag(id1+TAG_DISPOSE);
        btnEdit.setTag(id1+TAG_EDIT);
        txtListChild.setText(txt1[0]);
        txtChildDate.setText("");
        //The following counts the amount of days left for each product.
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
    /** This is used to make OnChildClick possible */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
