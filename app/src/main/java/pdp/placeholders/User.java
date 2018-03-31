package pdp.placeholders;

import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class User {
    String userid;
    String username;
    ArrayList<String> userlist;
    ArrayList<String> thrown_out;
    int eaten=0;
    long lastupdate;
    HashMap<String,Box> Boxes;
    public User(){

    }

    public User(String userid, String username, ArrayList<String> userlist1, long lastupdate, HashMap<String,Box> boxes){
        this.userid = userid;
        this.username = username;
        this.userlist = userlist1;
        this.lastupdate = lastupdate;
        this.Boxes = boxes;

    }
    public void thrownOut(String item){
        thrown_out.add(item);
    }
    public void Eaten(){
        eaten+=1;
    }

    public String getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getUserlist() {
        return userlist;
    }

    public static class Box {
        public String itemname;
        public String expiration;
        public String updatevalue;

        public Box(){
        }
        public Box(String item, String expir, String update){
            this.itemname = item;
            this.expiration = expir;
            this.updatevalue = update;
        }
    }
}
