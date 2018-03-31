package pdp.placeholders;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.swipeactionadapter.SwipeActionAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 This file helps create the ExpandableList in the main activity.
 Most of the functions are from other examples.
 The parts that are special for this app are: getGroupView  and  getChildView
 Further reading: https://developer.android.com/reference/android/widget/ExpandableListView.html
 I followed this video: https://www.youtube.com/watch?v=jZxZIFnJ9jE

 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    public List<String> listDataHeader;
    public HashMap<String,List<String>> listHashMap;

    int i = 0;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap){
        this.context =context;
        if (listDataHeader != null && listHashMap != null){
            this.listDataHeader = listDataHeader;
            this.listHashMap = listHashMap;
        }else{
            this.listDataHeader=new ArrayList<String>();
            this.listHashMap = new  HashMap<String,List<String>>();
        }
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
        String[] txt1 =childText.replace(UserItems.DELIMBOX," - ").split(UserItems.DELIMLIST);
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
        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        final Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        final Button btnDispose = (Button)convertView.findViewById(R.id.btnItemDispose);
        final Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
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
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnDelete.getVisibility() == View.GONE || btnDelete.getVisibility() == View.INVISIBLE) {
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDispose.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDispose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //btnItemDispose.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                        }
                    });
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserItems.removeItem(childText);

                            notifyDataSetChanged();
                            btnDelete.setText("Gone");
                            btnDelete.setBackgroundColor(context.getResources().getColor(R.color.colorOGreen));
                            btnDelete.setVisibility(View.GONE);
                            btnDispose.setVisibility(View.GONE);
                            btnEdit.setVisibility(View.GONE);
                            txtListChild.setVisibility(View.GONE);
                            txtChildDate.setVisibility(View.GONE);
                        }
                    });
                }  else {
                btnDelete.setVisibility(View.GONE);
                btnDispose.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
            }
        });
        a=0; txt1=null;
        final int DEFAULT_THRESHOLD = 128;
        convertView.setOnTouchListener(new View.OnTouchListener() {
            int initialX = 0;
            boolean removed = false;
            final float slop = ViewConfiguration.get(context).getScaledTouchSlop();
            public boolean onTouch(final View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialX = (int) event.getX();
                    view.setPadding(0, 0, 0, 0);
                }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int currentX = (int) event.getX();
                    int offset = currentX - initialX;
                    if (offset > slop) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);

                        view.setPadding(offset, 0, 0, 0);
                        view.setBackgroundColor(Color.RED);

                        if (offset > DEFAULT_THRESHOLD) {
                            // TODO :: Do Right to Left action! And do nothing on action_up.
                            removed = true;

                        }else{
                            removed = false;
                        }

                    }else if(offset<-slop){
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        view.setPadding(0,0,-offset,0);
                        view.setBackgroundColor(Color.GREEN);
                        if (offset < -DEFAULT_THRESHOLD) {
                            // TODO :: Do Left to Right action! And do nothing on action_up.
                         }

                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Animate back if no action was performed.
                    if (!removed){
                        ValueAnimator animator = ValueAnimator.ofInt(view.getPaddingLeft(), 0);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                view.setPadding((Integer) valueAnimator.getAnimatedValue(), 0, 0, 0);
                            }
                        });
                        animator.setDuration(500);
                        animator.start();
                    }else {
                        //view.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                        ValueAnimator animator = ValueAnimator.ofInt(view.getPaddingLeft(), view.getWidth());
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                view.setPadding((Integer) valueAnimator.getAnimatedValue(), 0, 0, 0);
                                if ((Integer)valueAnimator.getAnimatedValue()==view.getWidth()){
                                    view.setVisibility(View.GONE);
                                    txtListChild.setVisibility(View.GONE);
                                    txtChildDate.setVisibility(View.GONE);
                                }
                            }
                        });
                        animator.setDuration(1000);
                        animator.start();


                    }

                }

                return false;
            }

        });
        return convertView;
    }
    /** This is used to make OnChildClick possible */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }



    public void initData(List<String> itmlist){

        listDataHeader.add("Expired!");
        listDataHeader.add("Short term products");
        listDataHeader.add("Medium term products");
        listDataHeader.add("Long term products");
        listDataHeader.add("Boxes");
        List<String> expired = new ArrayList<>();
        List<String> shrtTerm = new ArrayList<>(); //creates different sections
        List<String> mdmTerm = new ArrayList<>();
        List<String> lngTerm = new ArrayList<>();
        List<String> boxTerm = new ArrayList<>();

        HashMap<String,User.Box> BoxesSorted =UserItems.getBoxes();
        Object[] array = BoxesSorted.keySet().toArray();
        for(Object item:array){boxTerm.add(item.toString()+UserItems.DASH
                +BoxesSorted.get(item).itemname+UserItems.DELIMLIST +BoxesSorted.get(item).expiration);}
        try{
            for(String item : itmlist){  //goes through list and adds them to appropriate section
                String[] txt1 = item.replace(UserItems.DELIMBOX,UserItems.DELIMLIST).split(UserItems.DELIMLIST);
                String txtdate = txt1[1];
                try {
                    DateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                    Date txtDate1 = formatter.parse(txtdate);
                    Date currentdate = Calendar.getInstance().getTime();
                    long diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                    if (diff<1){expired.add(item);                }
                    else{
                        if (diff < 7) {shrtTerm.add(item);
                        } else {
                            if (diff < 30) {mdmTerm.add(item);
                            } else {lngTerm.add(item);}
                        }
                    }
                } catch (ParseException e) { e.printStackTrace();}
            }
        }catch (Exception e){}

/*        if (expired.size()==0)expired.add("No expired items, Good Job!");
        if (shrtTerm.size()==0)shrtTerm.add("No Items Yet");
        if (mdmTerm.size()==0)mdmTerm.add("No Items Yet");
        if (lngTerm.size()==0)lngTerm.add("No Items Yet");
        if (boxTerm.size()==0)boxTerm.add("No Items Yet");
        if(expired.size()>0){}*/

        listHashMap.put(listDataHeader.get(0),expired);
        listHashMap.put(listDataHeader.get(1),shrtTerm);
        listHashMap.put(listDataHeader.get(2),mdmTerm);
        listHashMap.put(listDataHeader.get(3),lngTerm);
        listHashMap.put(listDataHeader.get(4),boxTerm);
    }

    public HashMap<String,List<String>> getListHashMap(){
        return listHashMap;
    }
}
