package com.example.geethanjalijeevanatham.hackathonunknownapp;

import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.widget.SendButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.geethanjalijeevanatham.hackathonunknownapp.R.id.add;
import static com.example.geethanjalijeevanatham.hackathonunknownapp.R.id.url;
import static java.lang.System.in;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LatLng mDefaultLocation;
    int PLACE_PICKER_REQUEST = 1;
    CallbackManager callbackManager;
    String userid = null;
    final HashMap<String,ArrayList<String>> placeuseridmap = new HashMap<String, ArrayList<String>>();
    final ArrayList<fbplacenode> fbplaces = new ArrayList<fbplacenode>();
    final ArrayList<fbusernode> userids = new ArrayList<fbusernode>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                //System.out.println("successful login");
                //System.out.println(loginResult.getAccessToken().toString());

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                System.out.println(object.toString());
                                try {
                                    JSONObject jsonObject = new JSONObject(object.toString());
                                    userid = jsonObject.getString("name");
                                    JSONArray jsonArray = jsonObject.getJSONObject("friends").getJSONArray("data");
                                    System.out.println(jsonArray.length());
                                    for(int i=0; i<jsonArray.length(); i++){
                                        String val = jsonArray.getJSONObject(i).getString("id");
                                        String name = jsonArray.getJSONObject(i).getString("name");
                                        fbusernode newnode = new fbusernode(name,val);
                                        userids.add(newnode);
                                    }

                                    for(final fbusernode node : userids){
                                        StringBuilder stringtwo = new StringBuilder();
                                        stringtwo.append("/");
                                        stringtwo.append(node.id);
                                        stringtwo.append("/tagged_places");
                                        System.out.println(stringtwo);
                                        GraphRequest req = GraphRequest.newGraphPathRequest(loginResult.getAccessToken(), stringtwo.toString(), new GraphRequest.Callback() {
                                            @Override
                                            public void onCompleted(GraphResponse response) {

                            try {
                                JSONObject jsonval = new JSONObject(response.getJSONObject().toString());
                                JSONArray jsonvalarr = jsonval.getJSONArray("data");
                                for(int i=0; i<jsonvalarr.length(); i++){
                                    String placeid = jsonvalarr.getJSONObject(i).getJSONObject("place").getString("id");
                                    if(!placeuseridmap.containsKey(placeid)){
                                        ArrayList<String> userid = new ArrayList<String>();
                                        userid.add(node.id);
                                        placeuseridmap.put(placeid,userid);
                                        double lat  =  Double.parseDouble(jsonvalarr.getJSONObject(i).getJSONObject("place").getJSONObject("location").getString("latitude"));
                                        double lng = Double.parseDouble(jsonvalarr.getJSONObject(i).getJSONObject("place").getJSONObject("location").getString("longitude"));
                                        LatLng newlatlng = new LatLng(lat,lng);
                                        String street = jsonvalarr.getJSONObject(i).getJSONObject("place").getJSONObject("location").getString("street");
                                        String name = jsonvalarr.getJSONObject(i).getJSONObject("place").getString("name");
                                        fbplacenode fbnode = new fbplacenode(newlatlng,placeid,name,street);
                                        fbplaces.add(fbnode);
                                    }
                                    else{
                                        ArrayList<String> userid = placeuseridmap.get(placeid);
                                        userid.add(node.id);
                                        placeuseridmap.put(placeid,userid);
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                                            }
                                        });
                                        req.executeAsync();
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,friends");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        setContentView(R.layout.activity_maps);
        Button btnfblogin = (Button) findViewById(R.id.fblogin);
        btnfblogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(MapsActivity.this, Arrays.asList("public_profile", "user_friends", "user_tagged_places", "user_location", "user_about_me"));
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
                //Log.i(TAG, "Place: " + place.getName());
                LatLng latLng = place.getLatLng();
                mDefaultLocation = latLng;
                List<android.location.Address> addresses = null;

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                try {
                    addresses = geocoder.getFromLocation(mDefaultLocation.latitude, mDefaultLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Status status) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, DEFAULT_ZOOM));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                final StringBuilder sb = new StringBuilder();
                sb.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
                System.out.println(latLng.latitude+"----"+latLng.longitude);
                sb.append(latLng.latitude+","+latLng.longitude);
                sb.append("&radius=500&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E");
                URL url = null;
                try {
                    url = new URL(sb.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                GetFiles getFiles = new GetFiles(url);
                getFiles.execute();
                String json;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ArrayList<placenode> placearr = new ArrayList<placenode>();
                double distanceval = 0.0;
                double minval = -1.0;
                int position = 0;

                try {
                    json = getFiles.getjsonString();
                    JSONObject jsonobj = new JSONObject(json);
                    JSONArray jsonarr = jsonobj.getJSONArray("results");
                    for(int i=0; i<jsonarr.length(); i++){
                        JSONObject obj = jsonarr.getJSONObject(i);
                        String lat = obj.getJSONObject("geometry").getJSONObject("location").getString("lat");
                        String lng = obj.getJSONObject("geometry").getJSONObject("location").getString("lng");
                        String placeidval = jsonarr.getJSONObject(i).get("place_id").toString();
                        //System.out.println(lat + "  " + lng + "  " + placeidval);
                        LatLng lnglat = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

                        distanceval = Math.sqrt(((lnglat.latitude - latLng.latitude)*(lnglat.latitude - latLng.latitude)) + ((lnglat.longitude - latLng.longitude)*(lnglat.longitude - latLng.longitude)));
                        if(minval == -1.0){
                            minval = distanceval;
                            position = i;
                        }
                        if(distanceval < minval){
                            minval = distanceval;
                            position = i;
                        }

                        //System.out.println(latLng.latitude + "  " + latLng.longitude + "  " + lnglat.latitude + " " + lnglat.longitude + "  " + distanceval + "---" + jsonarr.getJSONObject(i).get("name").toString());
                    }

                    StringBuilder stringResult = new StringBuilder();

                    //System.out.println(jsonarr.getJSONObject(position).get("place_id").toString());
                    stringResult.append(jsonarr.getJSONObject(position).get("place_id").toString());

                    final StringBuilder sb1 = new StringBuilder();
                    sb1.append("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
                    sb1.append(jsonarr.getJSONObject(position).get("place_id").toString());
                    sb1.append("&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E");

                    URL url1 = null;
                    try {
                        url1 = new URL(sb1.toString());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    GetFiles getFiles1 = new GetFiles(url1);
                    getFiles1.execute();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String json1 = getFiles1.getjsonString();

                    JSONObject jsonobj1 = new JSONObject(json1);
                    System.out.println(jsonobj1.getJSONObject("result").get("name"));

                    double mim = -1.0;
                    double dista = 0.0;
                    int pos = 0;

                    for(int k=0; k<fbplaces.size();k++){
                        fbplacenode node = fbplaces.get(k);
                        dista = Math.sqrt(((node.latLng.latitude - latLng.latitude)*(node.latLng.latitude - latLng.latitude)) + ((node.latLng.longitude - latLng.longitude)*(node.latLng.longitude - latLng.longitude)));
                        if(mim == -1.0){
                            mim = dista;
                            pos = k;
                        }
                        if(dista < mim){
                            mim = dista;
                            pos = k;
                        }
                    }

                    if(mim<=0.0005){
                        stringResult.append(" ");
                        stringResult.append("true");
                        System.out.println(fbplaces.get(pos).name);
                        stringResult.append(" ");
                        stringResult.append(fbplaces.get(pos).placeID);
                        stringResult.append(" ");

                        ArrayList<String> checkedinusers = new ArrayList<String>();
                        checkedinusers = placeuseridmap.get(fbplaces.get(pos).placeID);

                        for(String userid : checkedinusers){
                            stringResult.append(userid);
                            stringResult.append(" ");
                            for(fbusernode node : userids){
                                if(node.id.equals(userid)){
                                    System.out.println(node.name.toString());
                                }
                            }
                        }
                    }
                    else{
                        stringResult.append(" ");
                        stringResult.append("false");
                        stringResult.append(" ");
                    }

                    Intent newintent = new Intent(MapsActivity.this,ViewFBCheckedInFriends.class);
                    newintent.putExtra("attribs",stringResult.toString());
                    startActivity(newintent);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJG_G6wOd57ocRs0e3lt7T1kI&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E
                //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E

            }
        });
    }
}

class GetFiles extends AsyncTask {
    URL url = null;
    String jsonstring;

    public GetFiles(URL url){
        this.url = url;
    }

    public String getjsonString(){
        return jsonstring;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

            char[] buffer = new char[1024];

            jsonstring = new String();

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();

            jsonstring = sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}

class placenode{
    LatLng latlng = null;
    String placeid = null;

    public placenode(LatLng latlng, String placeid){
        this.latlng = latlng;
        this.placeid = placeid;
    }
}

class fbusernode{
    String name = null;
    String id = null;
    public fbusernode(String name, String id){
        this.name = name;
        this.id = id;
    }
}

class fbplacenode{
    LatLng latLng = null;
    String placeID = null;
    String name = null;
    String street = null;
    public fbplacenode(LatLng latLng, String placeID, String name, String street){
        this.latLng = latLng;
        this.placeID = placeID;
        this.name = name;
        this.street = street;
    }
}
