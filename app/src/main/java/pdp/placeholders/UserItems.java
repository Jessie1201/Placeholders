package pdp.placeholders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * This class stores the Users item information. It gets called in almost every Activity.
 * It functions as a singleton
 * further reading: https://android.jlelse.eu/how-to-make-the-perfect-singleton-de6b951dfdb0
 */

public class UserItems {
    //These are characters that i chose to use to split items that are saved together
    public static String itemListDelimiter = ";/;";
    public static String BoxDelimiter = ";-;";

    //These are all the variables
    private static UserItems mInstance;
    private static ArrayList<String> list = null;
    public static String userid = null;
    public static String username = null;
    public static long lastUpdate = new Date(0).getTime();
    private static HashMap<String,User.Box> Boxes = null;

    //if functions are called from outside, this converts tehm to static operators?
    public static UserItems getInstance() {
        if(mInstance == null)
            mInstance = new UserItems();
        return mInstance;
    }

    private UserItems() {
        list = new ArrayList<String>();
        Boxes = new HashMap<String, User.Box>();
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
    public static long getLastUpdate(){return lastUpdate;}
    public static HashMap<String, User.Box> getBoxes() {
        return Boxes;
    }
    public static ArrayList<String> getList() {
        return list;
    }
    public static void setList(ArrayList<String> list) {
        UserItems.list = list;
    }
    public static void setBoxes(HashMap<String, User.Box> boxes) {
        Boxes = boxes;
    }

    public static void addToList(String itemName, Calendar dateExpires, Calendar dateAdded){
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
        Date dateExpiresTime = dateExpires.getTime();
        Date dateAddedTime = dateAdded.getTime();
        String outputdateAdded = formatter.format(dateAddedTime);
        String outputdate = formatter.format(dateExpiresTime);
        itemName=itemName.replace(";","");
        String tolist = itemName + itemListDelimiter + outputdate+itemListDelimiter+outputdateAdded;
        list.add(tolist);
        lastUpdate = System.currentTimeMillis();
    }
    public static void addToList(String item){
        list.add(item);lastUpdate = System.currentTimeMillis();
    }
    public static void addBox (String key, User.Box box){
        Boxes.put(key,box);lastUpdate= System.currentTimeMillis();
    }

    public static void removeBox(String key){
        Boxes.remove(key);lastUpdate = System.currentTimeMillis();
    }
    public static void removeItem(String item){
        list.remove(item);lastUpdate = System.currentTimeMillis();
    }

    public static void clearUser(){
        list=null; userid=null; username=null; lastUpdate=0;
        Boxes = null;
    }
}