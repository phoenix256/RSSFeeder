package tr.n11.rssfeeder;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class SingleMenuItemActivity extends Activity {
	

	static final String LINK = "link";
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.singleitemwebview);
        
        // getting intent data
        Intent in = getIntent();
        
        // Get XML values from previous intent
        String link = in.getStringExtra(LINK);
             
        // Displaying all values on the screen
        WebView engine = (WebView) findViewById(R.id.web_single);  
        engine.loadUrl(link);
    }

}
