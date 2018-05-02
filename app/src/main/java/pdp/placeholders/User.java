package pdp.placeholders;

import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

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
        public Box(@Nullable String item,@Nullable String expir,@Nullable String update){
            if(item ==null ||expir ==null ||update ==null){
                this.itemname = "empty";
                this.expiration = "2094-09-30";
                this.updatevalue = "update";
            }else {
                this.itemname = item;
                this.expiration = expir;
                this.updatevalue = update;
            }
        }
    }
}
