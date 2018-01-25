package com.pdp.orthex.fragmentedapplication;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ExpandableListView itemlist;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    public static final int TAG_NAME = 0;
    public static final int TAG_DATE = 300;
    public static final int TAG_DELETE = 600;
    public static final int TAG_DISPOSE = 10000;
    public static final int TAG_EDIT = 20000;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        itemlist = (ExpandableListView)rootView.findViewById(R.id.expndlist);
        itemlist.setChildDivider(getResources().getDrawable(R.color.colorPrimaryDark));
        initData();
        listAdapter = new ExpandableListAdapter(getActivity().getApplicationContext(), listDataHeader,listHash);
        itemlist.setAdapter(listAdapter);
        itemlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {
                int id1 = 1000*groupPosition+childPosition+1000;
                final Button btnItemDelete = (Button)rootView.findViewWithTag(id1+TAG_DELETE);
                final Button btnItemDispose = (Button)rootView.findViewWithTag(id1+TAG_DISPOSE);
                final Button btnItemEdit = (Button)rootView.findViewWithTag(id1+TAG_EDIT);
                final TextView itemName = (TextView)rootView.findViewWithTag(id1+TAG_NAME);
                final TextView itemDate = (TextView)rootView.findViewWithTag(id1+TAG_DATE);
                ViewGroup.LayoutParams params =itemlist.getLayoutParams();
                ViewGroup.LayoutParams params2 = btnItemDelete.getLayoutParams();
                if (btnItemDelete.getVisibility()==View.GONE || btnItemDelete.getVisibility()==View.INVISIBLE){
                    btnItemDelete.setVisibility(View.VISIBLE);
                    btnItemDispose.setVisibility(View.VISIBLE);
                    btnItemEdit.setVisibility(View.VISIBLE);
                    btnItemDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object item = listAdapter.getChild(groupPosition,childPosition);
                            YourSingleton.removeFromArray(item);
                            btnItemDelete.setText("Gone");
                            btnItemDelete.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                            btnItemDelete.setVisibility(View.GONE);
                            btnItemDispose.setVisibility(View.GONE);
                            btnItemEdit.setVisibility(View.GONE);
                            itemName.setVisibility(View.GONE);
                            itemDate.setVisibility(View.GONE);

                            //refreshFragment();
                        }
                    });
                    //params.height = a + params2.height;
                } else {btnItemDelete.setVisibility(View.GONE);
                    btnItemDispose.setVisibility(View.GONE);
                    btnItemEdit.setVisibility(View.GONE);
                }
                params.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                itemlist.setLayoutParams(params);
                //itemframe.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                return true;
            }

        });

        listHash.remove(1);

        return rootView;
    }

    private void initData(){
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listDataHeader.add("Short term products");
        listDataHeader.add("Medium term products");
        listDataHeader.add("Long term products");
        List<String> itmlist = YourSingleton.getInstance().getArray(); //creates list of items
        List<String> shrtTerm = new ArrayList<>(); //creates different sections
        List<String> mdmTerm = new ArrayList<>();
        List<String> lngTerm = new ArrayList<>();
        for(String item : itmlist){  //goes through list and adds them to appropriate section
            String[] txt1 =YourSingleton.getItemInfo(item);
            String txtdate = txt1[1];
            try {
                DateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                Date txtDate1 = formatter.parse(txtdate);
                Date currentdate = Calendar.getInstance().getTime();
                long diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                if (diff < 7) {shrtTerm.add(item);
                } else {
                    if (diff < 30) {mdmTerm.add(item);
                    } else {lngTerm.add(item);}
                }
            } catch (ParseException e) { e.printStackTrace();}
        }
        if (shrtTerm.size()==0)shrtTerm.add("No Items Yet");
        if (mdmTerm.size()==0)mdmTerm.add("No Items Yet");
        if (lngTerm.size()==0)lngTerm.add("No Items Yet");

        listHash.put(listDataHeader.get(0),shrtTerm);
        listHash.put(listDataHeader.get(1),mdmTerm);
        listHash.put(listDataHeader.get(2),lngTerm);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //itemlist.expandGroup(1);
        //itemlist.expandGroup(0);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Toast.makeText(context, "home fragment attached",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void refreshFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, new HomeFragment());
        fragmentTransaction.commit();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
