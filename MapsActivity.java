package com.example.user.bustrackingwithpresence;

import android.graphics.Bitmap;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,RoutingListener {


    private GoogleMap mMap;

    private String  email;
    TextView mapsTextView;

    Double lat,lng;
    DatabaseReference location;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapsTextView=findViewById(R.id.mapsTV);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        location= FirebaseDatabase.getInstance().getReference("Locationss");
        polylines=new ArrayList<>();
        if (getIntent()!=null){

            email=getIntent().getStringExtra("email");
            lat=getIntent().getDoubleExtra("lat",0);
            lng=getIntent().getDoubleExtra("lng",0);

        }

        if (!TextUtils.isEmpty(email)){

              loadLocationForThisUser(email);
        }

    }

    private void loadLocationForThisUser(String email) {
        Query user_location=location.orderByChild("eMail").equalTo(email);
        user_location.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                    Teacking teacking=postSnapshot.getValue(Teacking.class);
                    LatLng friendLocation=new LatLng(Double.parseDouble(teacking.getLat()),Double.parseDouble(teacking.getLng()));


                    Log.d("---------------------",teacking.geteMail()+"    "+Double.parseDouble(teacking.getLat())+"  "+Double.parseDouble(teacking.getLng()));

                    Location current_user_location =new Location("");
                    current_user_location.setLatitude(lat);
                    current_user_location.setLongitude(lng);

                    Location frient_location=new Location(" ");
                    frient_location.setLatitude(Double.parseDouble(teacking.getLat()));
                    frient_location.setLongitude(Double.parseDouble(teacking.getLng()));
                    //distance (current_user_location,frient_location);


                    mMap.clear();
                  // erasPolyLine();
                   mMap.addMarker(new MarkerOptions()
                   .position(friendLocation)
                   .title(teacking.geteMail())
                   .snippet(" Distance "+new DecimalFormat().format(current_user_location.distanceTo(frient_location))+" km ")


                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));

                    mMap.getUiSettings().setZoomControlsEnabled(true);


                    getRoutToMarker(friendLocation);

                  //  mapsTextView.setText("Duration -:   Time-: "+new DecimalFormat().format(frient_location.getTime()));
                    LatLng currednt=new LatLng(lat,lng);
                    bearingBetweenLocations(currednt,friendLocation);
                }
                LatLng current=new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions()
                        .position(current)
                        .title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

                mMap.getUiSettings().setZoomControlsEnabled(true);



            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRoutToMarker(LatLng friendLocation) {
        Routing routing= new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lat,lng),friendLocation)
                .build();
               routing.execute();
    }



    private double distance(Location current_user_location, Location frient_location) {

        double theta =current_user_location.getLongitude()-frient_location.getLongitude();
        double dist=Math.sin(deg2rad(current_user_location.getLatitude()))
                   * Math.sin(deg2rad(frient_location.getLatitude()))
                   * Math.cos(deg2rad(current_user_location.getLatitude()))
                   * Math.cos(deg2rad(frient_location.getLatitude()))
                   * Math.cos(deg2rad(theta));
        dist=Math.acos(dist);
        dist=red2degree(dist);

        dist=dist*60*1.1515;
        return (dist);
    }

    private double red2degree(double dist) {
          return  (dist*180/Math.PI);
    }

    private double deg2rad(double latitude) {

        return  (latitude*Math.PI/180.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if (e!=null){
            Toast.makeText(this, " Error  "+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        else {
            Toast.makeText(this, "  Somthing went to wrong  Try again ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int sortestRoutIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

           // Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationText(),Toast.LENGTH_SHORT).show();
            mapsTextView.setText(" Distance -: "+ route.get(i).getDistanceText()+"  Time -: "+route.get(i).getDurationText() );
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private  void erasPolyLine(){

        for (Polyline line:polylines){
            line.remove();
        }
        polylines.clear();


    }



    private double bearingBetweenLocations(LatLng latLng1,LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }




}
