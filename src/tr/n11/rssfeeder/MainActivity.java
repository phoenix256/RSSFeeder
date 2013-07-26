package tr.n11.rssfeeder;

import java.util.ArrayList;
import java.util.List;

import tr.n11.rssfeeder.model.RSSFeed;
import tr.n11.rssfeeder.parser.NewsFeedParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {

    private ListView mRssListView;
    private NewsFeedParser mNewsFeeder;
    private List<RSSFeed> mRssFeedList;
    private RssAdapter mRssAdap;
    private float startY = 0, deltaY=0;
    private boolean isLoading=false;
    RelativeLayout headerRelativeLayout;
    private ProgressBar progressBar;
    private TextView tv1;
    private ImageView arrow;
    private int firstvisible=0;
    private int HEADER_HEIGHT=70, HEADER_TOP = 0; 
    private static final String LINK="link";
    private static int exit=0, limit=15;
    public static int scrolly=0,kontrol_scroll=10,topOffset,kontroller;

    private static final String TOPSTORIES = 
    					"http://news.yahoo.com/rss/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rss_feed_view);

        mRssListView = (ListView) findViewById(R.id.rss_list_view);
        headerRelativeLayout = (RelativeLayout) findViewById(R.id.headerRelativeLayoutt);
        progressBar = (ProgressBar) findViewById(R.id.head_progressBar);
        tv1 = (TextView) findViewById(R.id.ndtv);
        arrow = (ImageView) findViewById (R.id.imageView_arrow);
                
        mRssFeedList = new ArrayList<RSSFeed>();
        new DoRssFeedTask().execute(TOPSTORIES);
                       
       mRssListView.setOnTouchListener(new OnTouchListener() {
        	
        	  @Override
              public boolean onTouch(View arg0, MotionEvent evt) {
                  // TODO Auto-generated method stub
                  //
                       
                  switch (evt.getAction()) {

                  case MotionEvent.ACTION_DOWN: 
                  	
                  { 	startY  = evt.getY();
                  
                  
          	      firstvisible=mRssListView.getFirstVisiblePosition();
          	      	          
                  }
                  	break;      
                  
                      case MotionEvent.ACTION_MOVE:
                    
                      { 	 if (!isLoading&&firstvisible==0) {
                      		 
          		             deltaY = evt.getY() - startY;         		  
          		 
          		             //Log.d("debug", String.valueOf(deltaY));
          		             
          		             if(deltaY>10) {
          		      	          		 
								headerRelativeLayout.setPadding(
								 
								headerRelativeLayout.getPaddingLeft(), -1
								
								                             * HEADER_HEIGHT + ((int) deltaY/2), 0,
								
								headerRelativeLayout.getPaddingBottom());
								 
								headerRelativeLayout.getLayoutParams().height= ((int) deltaY/2);
          		
          		             if(headerRelativeLayout.getPaddingTop() >= HEADER_HEIGHT) {
          		
          		                 //change situation      		        		                 

          		            	progressBar.setVisibility(View.VISIBLE); 
          		            	
          		            	 arrow.setImageResource(R.drawable.arrow_up);
          		            	          		            	
          		            	tv1.setText("Yenilemek için býrak");
               		           
          		
          		             } else if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT) {        		                     		                 
          		               
          		               arrow.setImageResource(R.drawable.arrow_down);
          		                 
          		                 tv1.setText("Yenilemek için aþaðý çek");
          	
          		             }
          		
          		         }
          		             
                      }

                      }
                      break;
                      
                      case MotionEvent.ACTION_UP:
                    	  
                      {
                      	        if (!isLoading&&firstvisible==0) {
                      	        	
                      	        	 if(deltaY>10) {
                      	
                      	            if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT) {
                      
                      	                // coming back
                      	
                      	               headerRelativeLayout.setPadding(
                      	
                      	                        headerRelativeLayout.getPaddingLeft(), -1
                      	
                      	                                * HEADER_HEIGHT, 0,
                      	
                      	                        headerRelativeLayout.getPaddingBottom()); 
                      	                              		            	
                 		            	tv1.setText("Yükleniyor...");
                      	
                      	            } else {
                      	
                      	                // come and start the refreshing
                      	
                      	              headerRelativeLayout.setPadding(
                      
                      	                        headerRelativeLayout.getPaddingLeft(),  HEADER_TOP, 0,
                      	
                      	                        headerRelativeLayout.getPaddingBottom());                        	                    	              
               		            	
                      	              tv1.setText("Yükleniyor...");                        	                 
                      	
                      	                //START LOADING
                      	
                      	                isLoading = true;
                      	                                      	                
                      	                refreshing();          
                      	
                      	            }                      	            

                      	      	headerRelativeLayout.getLayoutParams().height= 1;     
                      	      	                      	
                      	        }
                      	     }

                      }
                      
                      break;
                      
                      default:
                        break;
                  }
                                 
                  return false;
              }

          });   
            
        
        mRssListView.setOnItemClickListener(this);
        
        mRssListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, 
                int visibleItemCount, int totalItemCount) {
                //Check if the last view is visible
            
            	int lastVisibleElement = firstVisibleItem + visibleItemCount;
            	
                if(lastVisibleElement == totalItemCount)
                {
                    //Load elements
                	                	
                	if(limit<kontroller+1)
                	{
                		limit=limit+7;
                		
                		//get scroll position
                	    scrolly = mRssListView.getFirstVisiblePosition();
                	    //get offset
                	    View v = mRssListView.getChildAt(0);
                	    topOffset = (v == null) ? 0 : v.getTop();
                	                    		
                		refreshing();          		

                	} 
                }
            }

        	@Override
        	public void onScrollStateChanged(AbsListView view, int scrollState) {
        		// TODO Auto-generated method stub
        	}
        });
    }      
	
    @Override
	protected void onResume() {
		
		 super.onResume();		
		
		 mRssListView.setSelectionFromTop(scrolly, topOffset);
		
		 scrolly=-1;	

	}	
	
	@Override
	protected void onPause() {
		 
		super.onPause();
		
		//get scroll position
	    scrolly = mRssListView.getFirstVisiblePosition();
	    //get offset
	    View v = mRssListView.getChildAt(0);
	    topOffset = (v == null) ? 0 : v.getTop();
	    	    
	} 
	
    private class RssAdapter extends ArrayAdapter<RSSFeed> {
        private List<RSSFeed> rssFeedLst;

        public RssAdapter(Context context, int textViewResourceId, List<RSSFeed> rssFeedLst) {
            super(context, textViewResourceId, rssFeedLst);
            this.rssFeedLst = rssFeedLst;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            RssHolder rssHolder = null;
            if (convertView == null) {
                view = View.inflate(MainActivity.this, R.layout.rss_list_item, null);
                rssHolder = new RssHolder();
                rssHolder.rssTitleView = (TextView) view.findViewById(R.id.rss_title_view);
              rssHolder.web_engine = (WebView) view.findViewById(R.id.web_engine);
              //  rssHolder.imageView11 = (ImageView) view.findViewById(R.id.imageView1);
                view.setTag(rssHolder);
            } else {
                rssHolder = (RssHolder) view.getTag();
            }
            RSSFeed rssFeed = rssFeedLst.get(position);
            rssHolder.rssTitleView.setText(rssFeed.getTitle());

            rssHolder.rssTitleView.setTypeface(null, Typeface.ITALIC);
            rssHolder.web_engine.loadData(rssFeed.getDescription(), "text/html", "UTF-8");  
            rssHolder.web_engine.setFocusable(false);
            rssHolder.web_engine.setClickable(false);
            rssHolder.web_engine.setLongClickable(false);
            rssHolder.web_engine.setFocusableInTouchMode(false);
            rssHolder.web_engine.setVerticalScrollBarEnabled(false);

          /*  new DownloadImageTask(rssHolder.imageView11)
            .execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");*/
            return view;
        }
    }

    static class RssHolder {
        public TextView rssTitleView;
       public WebView web_engine;
       // public ImageView imageView11;
    }

    public class DoRssFeedTask extends AsyncTask<String, Void, List<RSSFeed>> {
        ProgressDialog prog;
        String jsonStr = null;
        Handler innerHandler;

        @Override
        protected void onPreExecute() {
            prog = new ProgressDialog(MainActivity.this);
            prog.setMessage("Loading....");
            prog.show();
            NewsFeedParser.limit=limit;
        }

        @Override
        protected List<RSSFeed> doInBackground(String... params) {
            for (String urlVal : params) {
                mNewsFeeder = new NewsFeedParser(urlVal);
            }
            mRssFeedList = mNewsFeeder.parse();
            return mRssFeedList;
        }        
        

        @Override
        protected void onPostExecute(List<RSSFeed> result) {
            prog.dismiss();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mRssAdap = new RssAdapter(MainActivity.this, R.layout.rss_list_item,
                            mRssFeedList);
                    int count = mRssAdap.getCount();
                    kontroller=count;
                    if (count != 0 && mRssAdap != null) {
                    	
                        mRssListView.setAdapter(mRssAdap);                        
                    	
               		 mRssListView.setSelectionFromTop(scrolly, topOffset);               		
               		 
               		 scrolly=-1;
                    }
                }
            });
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    
    //Image Downloader AsyncTask
    
   /* private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    } */

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
    	    	
    	String link = mRssFeedList.get(position).getLink().toString();
    	
    	 Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
         in.putExtra(LINK, link);
         startActivity(in);
    }
   
   

private void refreshing()
{
	isLoading = false;
	progressBar.setVisibility(View.INVISIBLE);
	 new DoRssFeedTask().execute(TOPSTORIES);
}


@Override
public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
{
	
	if(pKeyCode==KeyEvent.KEYCODE_BACK && pEvent.getAction()==KeyEvent.ACTION_DOWN)
	{		
		if(exit==0)
		{
			Toast.makeText(getApplicationContext(), "Çýkmak için lütfen tekrar basýn.", Toast.LENGTH_SHORT).show();
			exit=exit+1;
			
		}else{
		
		finish();
		
		android.os.Process.killProcess(android.os.Process.myPid());
		
		}
		
		return true;
	}
	/* else if(pKeyCode==KeyEvent.KEYCODE_MENU && pEvent.getAction()==KeyEvent.ACTION_DOWN)
	{
		
		return true;
	} */
	else
	{
		
		return super.onKeyDown(pKeyCode, pEvent);
	}
	
}


  
}
