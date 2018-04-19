package pdp.placeholders;

import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class User {
    String userid;
    String username;
    ArrayList<String> userlist;
    ArrayList<String> thrownout;
    int eaten=0;
    int lastupdate;
    HashMap<String,Box> Boxes;
    public User(){

    }

    public User(String userid, String username, ArrayList<String> userlist1, int lastupdate, HashMap<String,Box> boxes){
        this.userid = userid;
        this.username = username;
        if(userlist1.size()<1){userlist1.add("no items");}
        else {this.userlist = userlist1;}
        this.lastupdate = lastupdate;
        this.Boxes = boxes;
        this.thrownout = UserItems.thrownout;
        this.eaten = UserItems.eaten;

    }
    public void thrownOut(String item){
        thrownout.add(item);
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
