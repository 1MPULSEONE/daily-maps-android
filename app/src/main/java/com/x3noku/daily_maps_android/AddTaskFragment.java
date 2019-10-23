package com.x3noku.daily_maps_android;

import android.Manifest;
import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AddTaskFragment extends DialogFragment
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        View.OnClickListener {
    private static final String TAG = "AddTaskFragment";
    private View rootView;
    private Toolbar toolbar;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Marker marker;

    private AHBottomNavigation bottomNavigation;
    private ImageButton readyButton;
    private TextView timeFieldInput;
    private TextView durationFieldInput;
    private TextView priorityFieldInput;
    private Task task;

    private int previousSelectedItem;
    private int MY_PERMISSIONS_REQUEST_LOCATION = 89;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private UserInfo currentUserInfo;
    private FirebaseFirestore fireStore;


    static void display(FragmentManager fragmentManager, AHBottomNavigation bottomNavigation, int previousSelectedItem) {
        AddTaskFragment addTaskFragment = new AddTaskFragment(bottomNavigation, previousSelectedItem);
        addTaskFragment.show(fragmentManager, TAG);
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

        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        task = new Task();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            rootView = inflater.inflate(R.layout.fragment_add_task, container, false);
            MapsInitializer.initialize(Objects.requireNonNull(getActivity()));
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
        durationFieldInput = rootView.findViewById(R.id.duration_field_input);
        priorityFieldInput = rootView.findViewById(R.id.priority_field_input);

        timeFieldInput.setOnClickListener(this);
        durationFieldInput.setOnClickListener(this);
        priorityFieldInput.setOnClickListener(this);


        return rootView;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = mAuth.getCurrentUser();
        DocumentReference userDocument = fireStore.collection(getString(R.string.firestore_collection_users)).document(currentUser.getUid());
        userDocument.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if ( documentSnapshot != null && documentSnapshot.exists() ) {
                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                currentUserInfo = documentSnapshot.toObject(UserInfo.class);
            }
            else {
                Log.d(TAG, "Current data: null");
            }
        });

        toolbar.setNavigationOnClickListener(v -> dismiss());
        readyButton.setOnClickListener(this);

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
            Log.i(TAG, "onMapReady: UserInfo's Location is " + userLocation);

        }
        catch (SecurityException e) {
            Log.w(TAG, "onMapReady: UserInfo Rejected Location Request");
        }
        catch (NullPointerException e) {
            Log.e(TAG, "onMapReady: Trouble in Picking UserInfo's Location (CreateTaskFragment.java:81) \n" + e);
        }

        mGoogleMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(LatLng clickPoint) {
        if(marker != null) {
            marker.remove();
        }
        marker = mGoogleMap.addMarker(new MarkerOptions().position(clickPoint));
        task.setCoordinatesOfTask(clickPoint);
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

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.time_field_input:
                TimePickerDialog timePickerDialog
                        = new TimePickerDialog(
                        getActivity(),
                        onTimeSet,
                        task.getStartTimeOfTask()/60,
                        task.getStartTimeOfTask()%60,
                        true);
                timePickerDialog.show();
                break;
            case R.id.duration_field_input:
                break;
            case R.id.priority_field_input:
                showMenu(v);
                break;
            case R.id.ready_button:
                saveTask();
                break;
        }
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(Objects.requireNonNull(getContext()), anchor);
        popup.getMenuInflater().inflate(R.menu.priority_field_popup, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(v-> {
            switch(v.getItemId()) {
                case R.id.maximum_priority:
                    task.setPriorityOfTask(0);
                    break;
                case R.id.nearly_maximum_priority:
                    task.setPriorityOfTask(1);
                    break;
                case R.id.medium_priority:
                    task.setPriorityOfTask(2);
                    break;
                case R.id.nearly_minimum_priority:
                    task.setPriorityOfTask(3);
                    break;
                case R.id.minimum_priority:
                    task.setPriorityOfTask(4);
                    break;
            }
            priorityFieldInput.setText( v.getTitle() );
            return false;
        });
    }

    private boolean allFieldsFilledRight() {
        // ToDO: realize this stuff
        return true;
    }

    private void saveTask() {
        if( allFieldsFilledRight() && currentUserInfo != null ) {
            fireStore
                .collection(getString(R.string.firestore_collection_tasks))
                .add( task )
                .addOnSuccessListener(documentReference -> {
                    currentUserInfo.addTask( documentReference.getId() );
                    fireStore
                        .collection(rootView.getContext().getString(R.string.firestore_collection_users))
                        .document( currentUser.getUid() )
                        .set( currentUserInfo );
                    Snackbar
                        .make(rootView, rootView.getContext().getString(R.string.succes_task_saved), Snackbar.LENGTH_SHORT)
                        .show();
                    dismiss();
                });
        }
    }

    private TimePickerDialog.OnTimeSetListener onTimeSet = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("SetTextI18n")
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            task.setStartTimeOfTask( hourOfDay*60 + minute );
            timeFieldInput.setText(hourOfDay+":"+ (minute < 10 ? "0"+minute : minute) );
        }
    };

}
