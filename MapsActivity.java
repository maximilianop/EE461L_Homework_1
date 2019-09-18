package com.example.homework1_weatherapp;

import androidx.fragment.app.FragmentActivity;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;

//Shri driving now
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static String ADDRESS = "address";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    public void onMapReady(GoogleMap googleMap)  {
        mMap = googleMap;
        String addr = getIntent().getStringExtra(ADDRESS);
        double latitude = 0.0;
        double longitude = 0.0;
        double temperature = 0.0;
        double humidity = 0.0;
        double windspeed = 0.0;
        double precipIntensity = 0.0;
        String precipitation = "";
        //JSONParser parser = new JSONParser();
        try {
            String json = new Coordinates().execute(addr).get();
            latitude = Double.parseDouble(json.substring(0, json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            longitude = Double.parseDouble(json.substring(0, json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            temperature = Double.parseDouble(json.substring(0,json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            humidity = Double.parseDouble(json.substring(0, json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            windspeed = Double.parseDouble(json.substring(0, json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            precipIntensity = Double.parseDouble(json.substring(0, json.indexOf('|')-1));
            json = json.substring(json.indexOf('|') + 1, json.length());
            precipitation = json;
        } catch (InterruptedException e){
            e.printStackTrace();
        } catch (ExecutionException e){
            e.printStackTrace();
        } catch (Throwable t) {
        t.printStackTrace();
        }
        LatLng position = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        MarkerOptions marker = new MarkerOptions();
        mMap.addMarker(marker.position(position));
        TextView tv = (TextView)findViewById(R.id.text);
        tv.setText("Temperature: " + temperature + "\n" + "Humidity: " + humidity + "\n" + "Windspeed: " + windspeed + "\n" + "Precipitation Intensity: " + precipIntensity + "\n" + "Precipitation: " + precipitation);

    }
}
//end of Shri driving, Max driving now

class Coordinates extends AsyncTask<String, Void, String> {
    String googleURL = "https://maps.googleapis.com/maps/api/geocode/json";
    String darkSkyURL =  "https://api.darksky.net/forecast/656f8aec966c8e92207919efdbbf3f1e/";


    @Override
    public String doInBackground(String...addr){
        double temperature = 0.0;
        double humidity = 0.0;
        double windspeed = 0.0;
        double precipIntensity = 0.0;
        String precipitation = "";
        String fullAddr = addr[0];
        URL url = null;
        URLConnection conn = null;
        ByteArrayOutputStream output = null;
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            //Latitude and Longitude Data
            url = new URL(googleURL + "?address=" + URLEncoder.encode(fullAddr, "UTF-8") + "&key=AIzaSyDBedLde3QysdGZSMk-G3S8s6Q8oEqzgKQ" + "&sensor=false");
            conn = url.openConnection();
            output = new ByteArrayOutputStream(1024);
            IOUtils.copy(conn.getInputStream(), output);
            output.close();
            String json = output.toString();
            JSONObject jsonObj = new JSONObject(json);
            JSONArray results = (JSONArray) jsonObj.getJSONArray("results");
            JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            latitude = location.getDouble("lat");
            longitude = location.getDouble("lng");

            //Weather Data
            url = new URL(darkSkyURL + ((Double)latitude).toString() + "," + ((Double)longitude).toString());
            conn = url.openConnection();
            output = new ByteArrayOutputStream(1024);
            IOUtils.copy(conn.getInputStream(), output);
            output.close();
            String weather = output.toString();
            JSONObject weatherJson = new JSONObject(weather);
            temperature = weatherJson.getJSONObject("currently").getDouble("temperature");
            humidity = weatherJson.getJSONObject("currently").getDouble("humidity");
            windspeed = weatherJson.getJSONObject("currently").getDouble("windSpeed");
            precipIntensity = weatherJson.getJSONObject("currently").getDouble("precipIntensity");
            if(precipIntensity != 0.0){
                precipitation = weatherJson.getJSONObject("currently").getString("precipType");
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        return ((Double)latitude).toString() + "|" + ((Double)longitude).toString() + "|" + ((Double)temperature).toString() + "|" + ((Double)humidity).toString() + "|" + ((Double)windspeed).toString() + "|" + precipitation + "|" + precipIntensity;
    }
}
//end of Max driving

