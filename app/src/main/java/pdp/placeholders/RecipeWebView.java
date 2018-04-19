package pdp.placeholders;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

//import com.pdp.orthex.fragmentedapplication.R;

import java.util.ArrayList;

public class RecipeWebView extends Activity {
    TextView usernameText;
    ImageView editbtn;
    WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_web_view);
        String searchterm=getIntent().getStringExtra("Searchterm");

        usernameText=findViewById(R.id.UserNameText1);
        usernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        wv = findViewById(R.id.webView);
        ArrayList<String> expiring = UserItems.getExpiringlist();
        String urlBase = "https://www.allrecipes.com/search/results/?wt="+searchterm;

        wv.loadUrl(urlBase);
    }


}
