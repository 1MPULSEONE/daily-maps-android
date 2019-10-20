package com.x3noku.daily_maps_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AddTaskFragment extends DialogFragment
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener {
    private static final String TAG = "AddTaskFragment";
    private View rootView;
    private Toolbar toolbar;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Marker marker;

    private AHBottomNavigation bottomNavigation;
    private ImageButton readyButton;
    private TextView timeFieldInput;
    private Task task;

    private int previousSelectedItem;
    private int MY_PERMISSIONS_REQUEST_LOCATION = 89;


    static AddTaskFragment display(FragmentManager fragmentManager, AHBottomNavigation bottomNavigation, int previousSelectedItem) {
        AddTaskFragment addTaskFragment = new AddTaskFragment(bottomNavigation, previousSelectedItem);
        addTaskFragment.show(fragmentManager, TAG);
        return addTaskFragment;
    }

    private AddTaskFragment(AHBottomNavigation bottomNavigation, int previousSelectedItem) {
        this.bottomNavigation = bottomNavigation;
        this.previousSelectedItem = previousSelectedItem;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }

        task = new Task();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            rootView = inflater.inflate(R.layout.fragment_add_task, container, false);
            MapsInitializer.initialize( getActivity() );
            mMapView = rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);
            checkLocationPermission();
        }
        catch (InflateException e){
            Log.e(TAG, "Inflate exception");
        }
        toolbar = rootView.findViewById(R.id.toolbar);
        readyButton = rootView.findViewById(R.id.ready_button);
        timeFieldInput = rootView.findViewById(R.id.time_field_input);
        timeFieldInput.setOnClickListener(v->{
            TimePickerDialog timePickerDialog
                    = new TimePickerDialog(
                            getActivity(),
                            onTimeSet,
                    task.getStartTimeOfTask()/60,
                    task.getStartTimeOfTask()%60,
                    true);
            timePickerDialog.show();
        });

        return rootView;
    }
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        readyButton.setOnClickListener(v -> saveTask());

        //toolbar.setTitle("Some Title");
        //toolbar.inflateMenu(R.menu.add_task);
        /*toolbar.setOnMenuItemClickListener(item -> {
            Toast.makeText(getContext(), "Done Is Clicked", Toast.LENGTH_SHORT).show();
            return true;
        });

         */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        try {
            mGoogleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition camPos = new CameraPosition.Builder()
                    .target(new LatLng(userLocation.latitude, userLocation.longitude))
                    .zoom(12.2f)
                    .build();
            CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
            mGoogleMap.moveCamera(camUpdate);
            Log.i(TAG, "onMapReady: User's Location is " + userLocation);

        }
        catch (SecurityException e) {
            Log.w(TAG, "onMapReady: User Rejected Location Request");
        }
        catch (NullPointerException e) {
            Log.e(TAG, "onMapReady: Trouble in Picking User's Location (CreateTaskFragment.java:81) \n" + e);
        }

        mGoogleMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(LatLng clickPoint) {
        if(marker != null) {
            marker.remove();
        }
        marker = mGoogleMap.addMarker(new MarkerOptions().position(clickPoint));
        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));
    }

    private void checkLocationPermission() {

        if (getContext()!=null && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (getActivity()!=null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok_location_permission, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            if( getActivity()!=null ) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                // No explanation needed, we can request the permission.
                if( getActivity()!=null ) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState); mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        bottomNavigation.setCurrentItem(previousSelectedItem, false);
    }

    private void saveTask() {
        // ToDo: realize this stuff
        Toast.makeText(this.getContext(), "Task Saved", Toast.LENGTH_SHORT).show();
    }

    private TimePickerDialog.OnTimeSetListener onTimeSet = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            task.setStartTimeOfTask( hourOfDay*60 + minute );
            timeFieldInput.setText(hourOfDay+":"+ (minute < 10 ? "0"+minute : minute) );
        }
    };

}
