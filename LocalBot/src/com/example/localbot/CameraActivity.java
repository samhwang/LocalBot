package com.example.localbot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;


public class CameraActivity extends Activity {
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
    private Camera mCamera;
    private CameraPreview mPreview;
    LocationManager locationManager;
    int bubbleCount = 0;
    Activity context_activity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Register the listener with the Location Manager to receive location update
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        
        // Create an instance of Camera
        mCamera = getCameraInstance();
               
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);        
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			Intent intent = new Intent(this, SettingActivity.class);
			//EditText editText = (EditText) findViewById(R.id.edit_message);
            //String message = editText.getText().toString();
            startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    
    // Sub class that gets data from http
     private class RequestData extends AsyncTask <String, Void, String> {
    	protected String doInBackground(String... url) {
    		String xml = null;
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url[0]);
                HttpResponse httpResponse = httpClient.execute(httpGet);

                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);

            } catch (UnknownHostException e) {
            	e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

    		return xml;
        }
    	
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
            
        }

    }
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        	// Remove bubble
        	for (int i = 0; i<bubbleCount;i++){
        		TextBubble text_bub = (TextBubble) findViewById(i);
        		preview.removeView(text_bub);
        	}
        	bubbleCount = 0;
        	
          // Called when a new location is found by the network location provider.
        	String xml = ""; 
        	String locationProvider = LocationManager.NETWORK_PROVIDER;
        	Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            double lati = lastKnownLocation.getLatitude();
            double longi = lastKnownLocation.getLongitude();
        	Log.i("TAG", String.valueOf(lati)+ " " + String.valueOf(longi));
        	
        	// Request data from Google Places
        	StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
        	sb.append("key=AIzaSyCmEkYlCIlRQNo11E-j3LVkQu8LTiW9AGE");
        	sb.append("=&location=" +  String.valueOf(lati) + ',' + String.valueOf(longi));
        	sb.append("&radius=50");
        	String url = sb.toString();
        	 try {
				xml = new RequestData().execute(url).get();
			} catch (InterruptedException e) {
				return;
			} catch (ExecutionException e) {
				return;
			}
        	 
            // Process xml
        	 InputStream in = new ByteArrayInputStream(xml.getBytes());
        	 XmlPullParser parser = Xml.newPullParser();
        	 String title = null;
        	 String summary = null;
        	 String link = null;
        	 String ns = "";
        	 String text = "";
        	 LocationData loc = new LocationData();
        	 List<LocationData> locationList = new ArrayList<LocationData>();
        	 try {
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in, null);
				parser.nextTag();
				int event;
				event = parser.getEventType();
		         while (event != XmlPullParser.END_DOCUMENT) {
		            String name=parser.getName();
		            switch (event){
		               case XmlPullParser.START_TAG:
		               break;
		               case XmlPullParser.TEXT:
		               text = parser.getText();
		               break;

		               case XmlPullParser.END_TAG:
		                  if(name.equals("name")){
		                     loc = new LocationData();
		                     loc.name = text;
		                  }
		                  else if(name.equals("lat")){ 	
		                	  loc.lat = Double.parseDouble(text);
		                  }
		                  else if(name.equals("lng")){ 	
		                	  loc.lng = Double.parseDouble(text);
		                  }
		                  else if(name.equals("place_id")){ 	
		                	  loc.place_id = text;
		                	  locationList.add(loc);
		                  }
		                  else{
		                  }
		                  break;
		                  }		 
		                  event = parser.next(); 

		              }
        	 } catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            Log.i("XML PARSE", Integer.toString(locationList.size()));
        	 
        	 
        	//        
        	// Search event from FB
        	
        	
        	// Search public events
        	
        	// Remove old bubbles
        	// Update bubbles
            for (LocationData lo:locationList)
            {
            	final String name = lo.name;
            	TextBubble text_bub = new TextBubble(context_activity, bubbleCount, lo.name, "300m",bubbleCount*50,bubbleCount*50);
                text_bub.setOnClickListener(new OnClickListener(){
                	public void onClick(View v) { //Function for Onclick event
                		// Update sliding panel
                		TextView locationView = (TextView) findViewById(R.id.locationView);
                		locationView.setText(name);
                		
                	}
                });
                preview.addView(text_bub);
                bubbleCount++;
            }
        	
        	
        	
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

      };
}



