package pdp.placeholders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class YourSingleton {
    public static String itemListDelimiter = ";/;";

    private static YourSingleton mInstance;
    private static ArrayList<String> list = null;
    public static String userid = null;
    public static String username = null;

    public static YourSingleton getInstance() {
        if(mInstance == null)
            mInstance = new YourSingleton();

        return mInstance;
    }
    public static void setUsername(String user){
        username = user;
    }


    public static void setUserId(String user){
        userid = user;
    }

    public static String getUserid() {
        return userid;
    }
    public static String getUsername() {
        return username;
    }

    private YourSingleton() {
        list = new ArrayList<String>();
    }

    //newItemInputs gets inputs from user and converts them to string that can be added to the array
    public static String newItemInputs(String itemName, Calendar dateExpires, Calendar dateAdded){
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
        Date dateExpiresTime = dateExpires.getTime();
        Date dateAddedTime = dateAdded.getTime();
        String outputdateAdded = formatter.format(dateAddedTime);
        String outputdate = formatter.format(dateExpiresTime);
        itemName=itemName.replace(";","");
        String output = itemName + itemListDelimiter + outputdate+itemListDelimiter+outputdateAdded;
        return output;
    }
    public static String[] getItemInfo(String itemInfo){
        String[] stringarray = itemInfo.split(itemListDelimiter);
        return stringarray;
    }

    // retrieve array from anywhere
    public ArrayList<String> getArray() {
        return this.list;
    }
    //Add element to array
    public void addToArray(String value) {
        list.add(value);
    }
    public static void removeFromArray(Object item){
        list.remove(item);
    }

    public void replaceArray(ArrayList<String> newlist){
        list = newlist;
    }

}
