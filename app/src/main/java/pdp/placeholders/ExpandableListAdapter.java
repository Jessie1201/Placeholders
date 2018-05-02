package pdp.placeholders;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 This file helps create the ExpandableList in the main activity.
 Most of the functions are from other examples.
 The parts that are special for this app are: getGroupView  and  getChildView
 Further reading: https://developer.android.com/reference/android/widget/ExpandableListView.html
 I followed this video: https://www.youtube.com/watch?v=jZxZIFnJ9jE

 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    TextView txtListChild;
    TextView txtChildDate;
    ImageView itemStatus;
    Button btnDelete;
    Button btnEdit;
    FrameLayout rightside;
    FrameLayout leftside;
    LinearLayout linearLayout;
    View midpart;



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
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
        final String childText = (String)getGroup(groupPosition);
        boolean isBox = false;
        try {
            if(getChildrenCount(groupPosition)>0){
                isBox = true;
            };

        }catch (Exception e){
            Log.d(TAG, "getGroupView: ");
        }
        /*     Each stored item is saved as a string in the following format:
         *      item name;/; date expires ;/; date added
         *      txt1 is the array that you get when you split the data with ;/; */
        final String[] txt1 =childText.split(UserItems.DELIMLIST);//.replace(UserItems.DELIMBOX," - ")
        int a = txt1.length;
        // Gets the layout resource for the child item
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group,null);
        }
        final View finalConvertView = convertView;
        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        final ImageView itemStatus = (ImageView) convertView.findViewById(R.id.itemstatus);
        final Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        final Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
        final FrameLayout rightside = convertView.findViewById(R.id.rigside);
        final FrameLayout leftside = convertView.findViewById(R.id.leftside);
        final LinearLayout linearLayout = convertView.findViewById(R.id.itemlayout);
        final View midpart = convertView.findViewById(R.id.itemvisible);

        txtListChild.setText(txt1[0]);
        txtChildDate.setText("");

        long diff = 0;
        boolean isexpiring = false;
        //The following counts the amount of days left for each product.
        if(a >=2){
            String txtdate = txt1[1];
            diff = UserItems.countDays(txtdate);
            String strDays = "Days left: " + Long.toString(diff);
            txtChildDate.setText(strDays);
            if (diff < 0) {
                if(isBox){ itemStatus.setImageResource(R.drawable.ic_icon_box_issue_red);
                }else{ itemStatus.setImageResource(R.drawable.ic_icon_status_red_warning);
                }
               UserItems.expired+=1;
                isexpiring = true;
            } else if (diff < 4) {
                if(isBox){ itemStatus.setImageResource(R.drawable.ic_icon_box_issue);
                }else{itemStatus.setImageResource(R.drawable.ic_icon_status_yellow_warning);
                }
                UserItems.expired=UserItems.expired+1;
                UserItems.addExpiringList(txt1[0]);
                isexpiring=true;
            }else{
                if(isBox){ itemStatus.setImageResource(R.drawable.ic_icon_box);
                }else{ itemStatus.setImageResource(R.drawable.ic_icon_status_ok);
                }
            }
        }
        if (!isexpiring && UserItems.expiring){
            removeAnimation(leftside,convertView);
        }else {resetAnimation(leftside,convertView);}
        if(isBox){
            isExpanded=true;
        }else {
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RecipeWebView.class);
                    intent.putExtra("Searchterm",txtListChild.getText().toString());
                    context.startActivity(intent);
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context.getApplicationContext(), ItemadditionActivity.class);
                    intent.putExtra("item",childText);
                    context.startActivity(intent);
                }
            });
            convertView.isClickable();
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnDelete.getVisibility() == View.GONE || btnDelete.getVisibility() == View.INVISIBLE) {
                        btnDelete.setVisibility(View.VISIBLE);
                        btnEdit.setVisibility(View.VISIBLE);
                        midpart.setBackgroundResource(R.color.colorOOrange);
                        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        midpart.setLayoutParams(params);
                    }  else {
                        finalConvertView.setBackgroundResource(R.color.colorPrimaryDark);
                        btnDelete.setVisibility(View.GONE);
                        btnEdit.setVisibility(View.GONE);
                        midpart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        midpart.setBackgroundResource(R.color.colorPrimaryDark);

                    }

                }
            });

            final int DEFAULT_THRESHOLD = 400;

            convertView.setOnTouchListener(new View.OnTouchListener() {
                int initialX = 0;
                int offset;
                final float slop = ViewConfiguration.get(context).getScaledTouchSlop();
                public boolean onTouch(final View view, MotionEvent event) {
                    midpart.setLayoutParams(new LinearLayout.LayoutParams(finalConvertView.getWidth(),finalConvertView.getHeight()));
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        initialX = (int) event.getX();
                    }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        int currentX = (int) event.getX();
                        offset = currentX - initialX;
                        if(Math.abs(offset)>slop)view.getParent().requestDisallowInterceptTouchEvent(true);
                        if (offset > slop) {
                            linearLayout.setGravity(Gravity.START);
                            leftside.setLayoutParams(new LinearLayout.LayoutParams(offset, ViewGroup.LayoutParams.MATCH_PARENT));
                        }else if(offset<-slop){
                            linearLayout.setGravity(Gravity.END);
                            rightside.setLayoutParams(new LinearLayout.LayoutParams(-offset, ViewGroup.LayoutParams.MATCH_PARENT));
                        }

                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        if(offset>DEFAULT_THRESHOLD){
                            removeAnimation(leftside,finalConvertView);
                            UserItems.eaten+=1;
                            UserItems.removeItem(childText);
                        }else if (offset<-DEFAULT_THRESHOLD){
                            removeAnimation(rightside,finalConvertView);
                            UserItems.removeItem(childText);
                            //UserItems.addThrownout(txt1[0]);
                        }else if(offset>0){
                            resetAnimation(leftside,finalConvertView);
                        }else if(offset<0){
                            resetAnimation(rightside,finalConvertView);
                        }
                    }
                    return false;
                }
            });
        }
        return convertView;
    }

    private void removeAnimation(final View sideview, final View parentview){

        final TextView txtListChild = (TextView) parentview.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) parentview.findViewById(R.id.lblListItemDate);
        final Button btnDelete = (Button)parentview.findViewById(R.id.btnItemDelete);
        final Button btnEdit = (Button)parentview.findViewById(R.id.btnItemEdit);
        final FrameLayout rightside = parentview.findViewById(R.id.rigside);
        final FrameLayout leftside = parentview.findViewById(R.id.leftside);
        final View midpart = parentview.findViewById(R.id.itemvisible);
        ValueAnimator animator = ValueAnimator.ofInt(sideview.getWidth(), parentview.getWidth());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                sideview.setLayoutParams(new LinearLayout.LayoutParams((Integer) valueAnimator.getAnimatedValue(), ViewGroup.LayoutParams.MATCH_PARENT));
                if((Integer) valueAnimator.getAnimatedValue() >=parentview.getWidth()-1){
                    btnDelete.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.GONE);
                    txtListChild.setVisibility(View.GONE);
                    txtChildDate.setVisibility(View.GONE);
                    parentview.setVisibility(View.GONE);
                    midpart.setVisibility(View.GONE);
                    rightside.setVisibility(View.GONE);
                    leftside.setVisibility(View.GONE);
                }
            }
        });
        animator.setDuration(500);
        animator.start();
    }


    private void resetAnimation(final View sideview, View parentview){
        final TextView txtListChild = (TextView) parentview.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) parentview.findViewById(R.id.lblListItemDate);
        final FrameLayout rightside = parentview.findViewById(R.id.rigside);
        final FrameLayout leftside = parentview.findViewById(R.id.leftside);
        final View midpart = parentview.findViewById(R.id.itemvisible);
        txtListChild.setVisibility(View.VISIBLE);
        txtChildDate.setVisibility(View.VISIBLE);
        parentview.setVisibility(View.VISIBLE);
        midpart.setVisibility(View.VISIBLE);
        rightside.setVisibility(View.VISIBLE);
        leftside.setVisibility(View.VISIBLE);
        sideview.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        ValueAnimator animator = ValueAnimator.ofInt(sideview.getWidth(), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                sideview.setLayoutParams(new LinearLayout.LayoutParams((Integer)valueAnimator.getAnimatedValue(), ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });
        animator.setDuration(500);
        animator.start();

        }



    /**This function creates the views for the children of each group.*/
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,null);
        }
        final String childText = (String)getChild(groupPosition,childPosition);

        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        final ImageView itemStatus = (ImageView) convertView.findViewById(R.id.itemstatus);
        final Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        final Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
        final FrameLayout rightside = convertView.findViewById(R.id.rigside);
        final FrameLayout leftside = convertView.findViewById(R.id.leftside);
        final LinearLayout linearLayout = convertView.findViewById(R.id.itemlayout);
        final View midpart = convertView.findViewById(R.id.itemvisible);



        /*     Each stored item is saved as a string in the following format:
         *      item name;/; date expires ;/; date added
         *      txt1 is the array that you get when you split the data with ;/; */
        final String[] txt1 =childText.replace(UserItems.DELIMBOX," - ").split(UserItems.DELIMLIST);
        int a = txt1.length;
        // Gets the layout resource for the child item
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,null);
        }
        final View finalConvertView = convertView;
        /* Each view item is given a tag
        *  This is because we want to change their individual attributes later
        */
        txtListChild.setText(txt1[0]);
        txtChildDate.setText("");
        //The following counts the amount of days left for each product.
        if(a >=2){
            String txtdate = txt1[1];
            long diff = UserItems.countDays(txtdate);
            String strDays = "Days left: " + Long.toString(diff);
            txtChildDate.setText(strDays);
            if (diff < 0) {
                itemStatus.setImageResource(R.drawable.ic_icon_status_red_warning);
                UserItems.expired+=1;
            } else if (diff < 4) {
                itemStatus.setImageResource(R.drawable.ic_icon_status_yellow_warning);
                UserItems.expired=UserItems.expired+1;
                UserItems.addExpiringList(txt1[0]);
            }else{ itemStatus.setImageResource(R.drawable.ic_icon_status_ok);
            }
        }

        final String[] boxname = listDataHeader.get(groupPosition).split(UserItems.DELIMLIST);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeWebView.class);
                intent.putExtra("Searchterm",txtListChild.getText().toString());
                context.startActivity(intent);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), ItemadditionActivity.class);
                intent.putExtra("item",childText);
                intent.putExtra("box",boxname[0]);
                context.startActivity(intent);
            }
        });
        convertView.isClickable();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnDelete.getVisibility() == View.GONE || btnDelete.getVisibility() == View.INVISIBLE) {
                    btnDelete.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);


                    midpart.setBackgroundResource(R.color.colorOOrange);
                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    midpart.setLayoutParams(params);
                }  else {
                    finalConvertView.setBackgroundResource(R.color.colorPrimaryDark);
                    btnDelete.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.GONE);
                    midpart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    midpart.setBackgroundResource(R.color.colorPrimaryDark);

                }

            }
        });
        final int DEFAULT_THRESHOLD = 400;

        convertView.setOnTouchListener(new View.OnTouchListener() {
            int initialX = 0;
            int offset;
            final float slop = ViewConfiguration.get(context).getScaledTouchSlop();
            public boolean onTouch(final View view, MotionEvent event) {
                midpart.setLayoutParams(new LinearLayout.LayoutParams(finalConvertView.getWidth(),finalConvertView.getHeight()));
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialX = (int) event.getX();
                }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int currentX = (int) event.getX();
                    offset = currentX - initialX;
                    if(Math.abs(offset)>slop)view.getParent().requestDisallowInterceptTouchEvent(true);
                    if (offset > slop) {
                        linearLayout.setGravity(Gravity.START);
                        leftside.setLayoutParams(new LinearLayout.LayoutParams(offset, ViewGroup.LayoutParams.MATCH_PARENT));
                    }else if(offset<-slop){
                        linearLayout.setGravity(Gravity.END);
                        rightside.setLayoutParams(new LinearLayout.LayoutParams(-offset, ViewGroup.LayoutParams.MATCH_PARENT));
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if(offset>DEFAULT_THRESHOLD){
                        removeAnimation(leftside,finalConvertView);
                        UserItems.eaten+=1;
                        UserItems.addBox(boxname[0],new User.Box(null,null,null));

                    }else if (offset<-DEFAULT_THRESHOLD){
                        removeAnimation(rightside,finalConvertView);
                        UserItems.addBox(boxname[0],new User.Box(null,null,null));
                        //UserItems.addThrownout(txt1[0]);

                    }else if(offset>0){
                        resetAnimation(leftside,finalConvertView);
                    }else if(offset<0){
                        resetAnimation(rightside,finalConvertView);
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
        HashMap<String,User.Box> BoxesSorted =UserItems.getBoxes();
        Object[] array = BoxesSorted.keySet().toArray();
        if(array.length>0){
            for(Object item:array){
                String boxitem = (item.toString()+UserItems.DELIMLIST +BoxesSorted.get(item).expiration);
                listDataHeader.add(boxitem);
                ArrayList<String> itemsInBox=new ArrayList<String>();
                itemsInBox.add(BoxesSorted.get(item).itemname+UserItems.DELIMLIST+BoxesSorted.get(item).expiration);
                listHashMap.put(boxitem,itemsInBox);
            }
        }
        for(String item:itmlist){
            listDataHeader.add(item);
        }
    }

    public void customexpand(){

    }

    public void customTouchEvents(){

    }



    public HashMap<String,List<String>> getListHashMap(){
        return listHashMap;
    }
}
