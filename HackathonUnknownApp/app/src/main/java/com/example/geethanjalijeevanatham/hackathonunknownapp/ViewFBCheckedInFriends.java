package com.example.geethanjalijeevanatham.hackathonunknownapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ViewFBCheckedInFriends extends AppCompatActivity {
    FbUserListAdapter adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fbchecked_in_friends);

        Bundle b = getIntent().getExtras();
        String attribsval = b.getString("attribs");
        String[] splits = attribsval.split(" ");
        /*for(String split : splits){
            System.out.println(split);
        }*/

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        System.out.println(accessToken.toString());

        StringBuilder sb1 = new StringBuilder();
        sb1.append("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
        sb1.append(splits[0].toString());
        sb1.append("&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E");

        //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJ-_S633lw7ocRiUWoBVUtBHw&key=AIzaSyCPfo7naMtSfHNz30wbFZJ_Y9U4OjfgT6E

        URL url = null;
        try {
            url = new URL(sb1.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        GetFiles getFiles1 = new GetFiles(url);
        getFiles1.execute();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String json1 = getFiles1.getjsonString();
        //System.out.println("From Google ----" + json1);

        try {
            JSONObject jsonObject = new JSONObject(json1);
            String addr = jsonObject.getJSONObject("result").getString("formatted_address");
            String num = jsonObject.getJSONObject("result").getString("formatted_phone_number");
            String name = jsonObject.getJSONObject("result").getString("name");
            //String rating = jsonObject.getJSONObject("result").getString("rating");
            TextView textname = (TextView) findViewById(R.id.textviewname);
            textname.setText(name);
            TextView textaddr = (TextView) findViewById(R.id.textviewaddr);
            textaddr.setText(addr);
            TextView textnum = (TextView) findViewById(R.id.textviewnum);
            textnum.setText(num);
            TextView textrate = (TextView) findViewById(R.id.textviewrate);
            //textrate.setText(rating);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(splits[1].equals("true")){

        StringBuilder sb2 = new StringBuilder();
        sb2.append("/");
        sb2.append(splits[2].toString());
        sb2.append("?fields=id,name,location");

        final GraphRequest req = GraphRequest.newGraphPathRequest(accessToken, sb2.toString(), new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                //System.out.println("fb place response ---" +response.getJSONObject().toString());
                try {
                    String name = response.getJSONObject().getString("name");
                    StringBuilder builder = new StringBuilder();
                    builder.append(response.getJSONObject().getJSONObject("location").getString("street"));
                    builder.append(response.getJSONObject().getJSONObject("location").getString("city"));
                    builder.append(response.getJSONObject().getJSONObject("location").getString("state"));
                    TextView fbname = (TextView) findViewById(R.id.fbname);
                    fbname.setText(name);
                    TextView fbaddr = (TextView) findViewById(R.id.fbaddr);
                    fbaddr.setText(builder);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        req.executeAsync();


            final ArrayList<fbusernode> nodes = new ArrayList<fbusernode>();
            TextView inf = (TextView)findViewById(R.id.info);
            inf.setVisibility(View.VISIBLE);


            for(int k=3; k<splits.length; k++){
                StringBuilder sb3 = new StringBuilder();
                sb3.append("/");
                sb3.append(splits[k].toString());
                sb3.append("?fields=id,name");
                GraphRequest req1 = GraphRequest.newGraphPathRequest(accessToken, sb3.toString(), new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        //System.out.println("fb user response ----" + response.getJSONObject().toString());
                        try {
                            String id = response.getJSONObject().getString("id");
                            String name = response.getJSONObject().getString("name");
                            fbusernode node = new fbusernode(name,id);
                            nodes.add(node);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        lv = (ListView) findViewById(R.id.listview);
                        adapter = new FbUserListAdapter(ViewFBCheckedInFriends.this,R.layout.friendslistview,nodes);
                        lv.setAdapter(adapter);

                    }
                });
                req1.executeAsync();
            }
        }
        else{
            TextView inf = (TextView)findViewById(R.id.info);
            inf.setVisibility(View.INVISIBLE);
        }
    }
}
