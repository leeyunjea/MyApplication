package org.androidtown.mylocationplayservice;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = "MainActivity";
    protected int count=0;

    protected Location mLastLocation;

    protected String mLatitudeLabel;
    protected String mLognitudeLabel;

    private EditText editText;
    private Button search;
    private ListView listView;
    private String str_address;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int RC_LOCATION = 1;
    private static final int RC_LOCATION_UPDATE = 2;

    protected LocationCallback mLocationCallback;

    ArrayAdapter adapter;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.listView);
        search = (Button)findViewById(R.id.search);

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str_address = editText.getText().toString();

                Log.i("yunjae", "size=" + arrayList.size());

                if (str_address.equals("현재위치")) {
                    stopLocationUpdate();
                    getLastLocation();
                }
                else if(str_address.equals("위치추적")) {
                    startLocationUpdate();
                }
                else {
                    stopLocationUpdate();
                    toAddress();
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION)
    public void getLastLocation() {
        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this,
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
                                    List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                                    if (addresses.size() > 0) {
                                        Address bestResult = (Address) addresses.get(0);
                                        str_address = bestResult.getFeatureName();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                updateUI();
                            }else {
                                Log.w(TAG, "getLastLocation:exception", task.getException());
                            }
                        }
                    });
        }else {
            EasyPermissions.requestPermissions(this, "This app needs access to your location to know where you are.", RC_LOCATION, perms);
        }
    }

    @SuppressWarnings("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION_UPDATE)
    public void startLocationUpdate() {
        LocationRequest locRequest = new LocationRequest();
        locRequest.setInterval(10000);
        locRequest.setFastestInterval(3000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
                    List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address bestResult = (Address) addresses.get(0);
                        str_address = bestResult.getFeatureName();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(arrayList.size() >= 10) {
                    arrayList.remove(0);
                }
                updateUI();
            }
        };

        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            mFusedLocationClient.requestLocationUpdates(locRequest, mLocationCallback, Looper.myLooper());
        }else {
            EasyPermissions.requestPermissions(this,
                    "This app needs access to your location to know where you are.",
                    RC_LOCATION_UPDATE, perms);
        }

    }

    protected void updateUI() {
        count++;
        arrayList.add(count + ". " + "주소 : " + str_address + " " + "위도: " + String.format(Locale.ENGLISH, "%f",
                mLastLocation.getLatitude()) + " " + "경도: " + String.format(Locale.ENGLISH, "%f",
                mLastLocation.getLongitude()));
        adapter.notifyDataSetChanged();
    }

    public void stopLocationUpdate() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void toAddress(){
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
            List<Address> addresses = geocoder.getFromLocationName(str_address, 1);
            if (addresses.size() > 0) {
                Address bestResult = (Address) addresses.get(0);
                count++;
                arrayList.add(String.format(count + ". " + "주소: %s, 위도: %s , 경도: %s ", bestResult.getFeatureName(), bestResult.getLatitude(), bestResult.getLongitude()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
