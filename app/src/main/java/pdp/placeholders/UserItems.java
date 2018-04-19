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
    public static String DELIMLIST = ";/;";
    public static String DELIMBOX = ";-;";
    public static String DASH = " - ";
    public static boolean expiring = false;
    public static int expired = 0;

    //These are all the variables
    private static UserItems mInstance;
    private static ArrayList<String> list = null;
    public static String userid = null;
    public static String username = null;
    public static int lastUpdate = 0;
    private static HashMap<String,User.Box> Boxes = null;
    public static int eaten=0;
    public static ArrayList<String> thrownout=null;
    private static ArrayList<String> expiringlist = null;

    //if functions are called from outside, this converts them to static operators?
    public static UserItems getInstance() {
        if(mInstance == null)
            mInstance = new UserItems();
        return mInstance;
    }

    private UserItems() {
        list = new ArrayList<String>();
        Boxes = new HashMap<String, User.Box>();
        thrownout = new ArrayList<String>();
        expiringlist=new ArrayList<String>();
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
    public static int getLastUpdate(){return lastUpdate;}
    public static ArrayList<String> getExpiringlist(){ return expiringlist;}
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
        String tolist = itemName + DELIMLIST + outputdate+ DELIMLIST +outputdateAdded;
        list.add(tolist);
        lastUpdate =lastUpdate+1;
    }
    public static void addToList(String item){
        list.add(item);lastUpdate =lastUpdate+1;
    }
    public static void addExpiringList(String item){
        expiringlist.add(item);
    }

    public static void addThrownout(String item) {
        thrownout.add(item);
    }
    public static void addBox (String key, User.Box box){
        Boxes.put(key,box);lastUpdate=lastUpdate+1;
    }

    public static void removeBox(String key){
        Boxes.remove(key);lastUpdate =lastUpdate+1;
    }
    public static void removeItem(String item){
        list.remove(item);
        String[] box = item.split(DASH);
        if(Boxes.containsKey(box[0])){Boxes.remove(box[0]);}
        lastUpdate = lastUpdate+1;
    }

    public static void clearUser(){
        list=null; userid=null; username=null; lastUpdate=0;
        Boxes = null;
    }

}
