package pdp.placeholders;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.support.constraint.ConstraintLayout;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
        try {
            if(getChildrenCount(groupPosition)>0);

        }catch (Exception e){
            Log.d(TAG, "getGroupView: ");
        }
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
        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        final ImageView itemStatus = (ImageView) convertView.findViewById(R.id.itemstatus);
        final Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
        final Button btnEdit = (Button)convertView.findViewById(R.id.btnItemEdit);
        final FrameLayout rightside = convertView.findViewById(R.id.rigside);
        final FrameLayout leftside = convertView.findViewById(R.id.leftside);
        final LinearLayout linearLayout = convertView.findViewById(R.id.itemlayout);
        //final ImageView logoRecipe = (ImageView) convertView.findViewById(R.id.logoRecipe);
        //final ImageView logoEdit = (ImageView)convertView.findViewById(R.id.logoEditItem);

        txtListChild.setText(txt1[0]);
        txtChildDate.setText("");
        final View midpart = convertView.findViewById(R.id.itemvisible);

        final View finalConvertView = convertView;
        long diff = 0;
        //The following counts the amount of days left for each product.
        if(a >=2){
            String txtdate = txt1[1];
            try {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date txtDate1 = formatter.parse(txtdate);
                Date currentdate = Calendar.getInstance().getTime();
                diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                String strDays = "Days left: " + Long.toString(diff);
                txtChildDate.setText(strDays);
                if (diff < 0) {itemStatus.setImageResource(R.drawable.ic_icon_status_red_warning);
                    UserItems.expired+=1;
                } else if (diff < 3) {itemStatus.setImageResource(R.drawable.ic_icon_status_yellow_warning);
                    UserItems.expired+=1;
                    UserItems.addExpiringList(txt1[0]);
                }else if(diff<20){
                    itemStatus.setImageResource(R.drawable.ic_icon_status_ok);
                    if(UserItems.expiring){
                        removeAnimation(leftside,convertView);
                    }else {resetAnimation(leftside,convertView);}
                }else {
                    if(UserItems.expiring){
                        removeAnimation(leftside,convertView);
                    }else resetAnimation(leftside,convertView);

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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
                    //logoEdit.setVisibility(View.VISIBLE);
                    //logoRecipe.setVisibility(View.VISIBLE);
                    //finalConvertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    midpart.setLayoutParams(params);
                }  else {
                    finalConvertView.setBackgroundResource(R.color.colorPrimaryDark);
                    btnDelete.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.GONE);

                    //logoEdit.setVisibility(View.GONE);
                    //logoRecipe.setVisibility(View.GONE);
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

        return convertView;
    }
    private void removeAnimation(final View sideview, final View parentview){
        notifyDataSetChanged();
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
                    leftside.setVisibility(View.GONE);}
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
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition,childPosition);
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
        /* Each view item is given a tag
        *  This is because we want to change their individual attributes later
        */
        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        final TextView txtChildDate = (TextView) convertView.findViewById(R.id.lblListItemDate);
        final Button btnDelete = (Button)convertView.findViewById(R.id.btnItemDelete);
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
               /* if (btnDelete.getVisibility() == View.GONE || btnDelete.getVisibility() == View.INVISIBLE) {
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDispose.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDispose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//btnItemDispose.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                        }
                    });
                }  else {
                btnDelete.setVisibility(View.GONE);
                btnDispose.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                }*/
            }
        });
        final int DEFAULT_THRESHOLD = 400;

        final FrameLayout rightside = convertView.findViewById(R.id.rigside);
        final FrameLayout leftside = convertView.findViewById(R.id.leftside);
        final View midpart = convertView.findViewById(R.id.itemvisible);
        final LinearLayout linearLayout = convertView.findViewById(R.id.itemlayout);
        final View finalConvertView = convertView;

        convertView.setOnTouchListener(new View.OnTouchListener() {
            int initialX = 0;
            int offset;
            final float slop = ViewConfiguration.get(context).getScaledTouchSlop();
            public boolean onTouch(final View view, MotionEvent event) {
                midpart.setLayoutParams(new LinearLayout.LayoutParams(finalConvertView.getWidth(), ViewGroup.LayoutParams.MATCH_PARENT));
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialX = (int) event.getX();
                }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int currentX = (int) event.getX();
                    offset = currentX - initialX;
                    if (offset > slop) {
                        linearLayout.setGravity(Gravity.START);
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        leftside.setLayoutParams(new LinearLayout.LayoutParams(offset, ViewGroup.LayoutParams.MATCH_PARENT));
                    }else if(offset<-slop){
                        linearLayout.setGravity(Gravity.END);
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        rightside.setLayoutParams(new LinearLayout.LayoutParams(-offset, ViewGroup.LayoutParams.MATCH_PARENT));
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    // Animate back if no action was performed.
                    if(Math.abs(offset)>DEFAULT_THRESHOLD){
                        ValueAnimator animator = null;
                        if(offset>DEFAULT_THRESHOLD){
                            UserItems.eaten+=1;
                            removeAnimation(leftside,finalConvertView);
                        }else if (offset<-DEFAULT_THRESHOLD){
                            UserItems.removeItem(childText);
                            //UserItems.addThrownout(txt1[0]);
                            removeAnimation(rightside,finalConvertView);
                        }
                    }
                    else if(offset>0){
                       resetAnimation(rightside,finalConvertView);

                    }else if(offset<0){
                        resetAnimation(leftside,finalConvertView);
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

        List<String> boxTerm = new ArrayList<>();


        HashMap<String,User.Box> BoxesSorted =UserItems.getBoxes();
        Object[] array = BoxesSorted.keySet().toArray();
        for(Object item:array){boxTerm.add(item.toString()+UserItems.DASH
                +BoxesSorted.get(item).itemname+UserItems.DELIMLIST +BoxesSorted.get(item).expiration);}
        try{
            for(String item : itmlist){  //goes through list and adds them to appropriate section
                try {
                    String[] txt1 = item.replace(UserItems.DELIMBOX,UserItems.DELIMLIST).split(UserItems.DELIMLIST);
                    String txtdate = txt1[1];
                    DateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                    Date txtDate1 = formatter.parse(txtdate);
                    Date currentdate = Calendar.getInstance().getTime();
                    long diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                    listDataHeader.add(item);
                } catch (Exception e) { e.printStackTrace();}
            }
        }catch (Exception e){}

    }

    public HashMap<String,List<String>> getListHashMap(){
        return listHashMap;
    }
}
