package com.sample.gojeck.sampleapp;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sample.gojeck.sampleapp.enums.Status;
import com.sample.gojeck.sampleapp.model.BaseError;
import com.sample.gojeck.sampleapp.model.Error;
import com.sample.gojeck.sampleapp.model.ForecastDay;
import com.sample.gojeck.sampleapp.model.StatusAwareResponse;
import com.sample.gojeck.sampleapp.model.Weather;
import com.sample.gojeck.sampleapp.view.CustomTextView;
import com.sample.gojeck.sampleapp.viewmodels.WeatherViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks*/ {
    private static final String TAG = "MainActivity";
    private static final String ERROR_CODE_NO_DATA_AT_LOCATION = "1003";
    private static final String ERROR_CODE_DEFAULT_ERROR = "1000";
    private static final String ERROR_CODE_LOCATION_NOT_AVAILABLE = "10003";
    private static final int ALL_PERMISSIONS_RESULT = 10001;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10002;
    private static final int REQUEST_CHECK_SETTINGS = 12003;
    LocationRequest locationRequest;
    private ProgressBar mProgressBar;
    private RelativeLayout mPositiveContainer;
    private RelativeLayout mNegativeContainer;
    private LinearLayout mForecastContainer;
    private CustomTextView tempForToday;
    private CustomTextView place;
    private Observer<StatusAwareResponse<Weather>> mResponseObserver;
    private WeatherViewModel mWeatherViewModel;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;

    ///////////////////////////////////////////////////////////////////////////
    // Util methods : start
    ///////////////////////////////////////////////////////////////////////////
    public static String dayForDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        //to convert epoch seconds to milies multiplying by 1000
        return dateFormat.format(new Date(time * 1000L));
    }

    private static String getErrorMessage(Context context, Error error) {
        String response = context.getString(R.string.error_something_went_wrong);
        if (error != null) {
            if (ERROR_CODE_NO_DATA_AT_LOCATION.equals(error.getError().getCode())) {
                response = context.getString(R.string.error_no_data_at_location);
            } else if (ERROR_CODE_LOCATION_NOT_AVAILABLE.equals(error.getError().getCode())) {
                response = context.getString(R.string.no_location_turn_on_gps);
            }
        }
        return response;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Util methods : end
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.loading);

        mPositiveContainer = findViewById(R.id.positive_view);
        mNegativeContainer = findViewById(R.id.negative_container);
        mForecastContainer = findViewById(R.id.forecast_container);
        tempForToday = mPositiveContainer.findViewById(R.id.today_temp);
        place = mPositiveContainer.findViewById(R.id.city);

        mWeatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        mResponseObserver = new Observer<StatusAwareResponse<Weather>>() {
            @Override
            public void onChanged(@Nullable StatusAwareResponse<Weather> weatherStatusAwareResponse) {
                if (weatherStatusAwareResponse == null) {
                    Log.i(TAG, "response is null in main activity");
                    Error error = new Error();
                    BaseError baseError = new BaseError();
                    baseError.setCode(ERROR_CODE_DEFAULT_ERROR);
                    baseError.setMessage(getString(R.string.error_something_went_wrong));
                    error.setError(baseError);
                    updateView(Status.failed, null, error);
                    return;
                }
                updateView(weatherStatusAwareResponse.getStatus(), weatherStatusAwareResponse.getData(), weatherStatusAwareResponse.getError());
            }
        };
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        checkRequiredPermissions();
        settingsCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(this, R.string.install_play_store, Toast.LENGTH_SHORT).show();
        }
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Check for location settings if GPS is off it will show a popup to enable it.
     */
    public void settingsCheck() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                Log.i(TAG, "onSuccess: settingsCheck");
                checkRequiredPermissions();
                getLastLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.e(TAG, "onFailure: settingsCheck");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        updateLocationErrorView();
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    /**
     * Checks if google play services is available in the device, returns true if available else show a popup
     * to install/update google play service.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    /**
     * Fetch weather information for a given location.
     */
    private void fetchWeatherData(double latitude, double longitude) {
        Log.i(TAG, "Fetching weather report for " + latitude + " , " + longitude);
        mWeatherViewModel.getWeatherForecast(latitude, longitude, 4)
                .observe(this, mResponseObserver);
    }

    /**
     * Update the view based on status
     */
    private void updateView(Status status, Weather data, Error error) {
        switch (status) {
            case failed: {
                mPositiveContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mNegativeContainer.setVisibility(View.VISIBLE);
                CustomTextView errorView = mNegativeContainer.findViewById(R.id.error_msg);
                errorView.setText(getErrorMessage(this, error));
                break;
            }
            case loading: {
                mNegativeContainer.setVisibility(View.GONE);
                mPositiveContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            }
            case success: {
                mPositiveContainer.setVisibility(View.VISIBLE);
                mNegativeContainer.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                updatePositiveView(data);
                mForecastContainer.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
                break;
            }
        }
    }

    /**
     * parse weather information and update the view
     */
    private void updatePositiveView(Weather data) {
        if (data != null) {
            tempForToday.setText(String.valueOf(data.getCurrent().getTempC()) + (char) 0x00B0);
            place.setText(data.getLocation().getName());
            LayoutInflater inflater = getLayoutInflater();
            if (mForecastContainer.getChildCount() > 0) {
                mForecastContainer.removeAllViews();
            }
            for (ForecastDay day : data.getForecast().getForecastday()) {
                View forecastView = inflater.inflate(R.layout.forecast, null);
                CustomTextView dayCtv = forecastView.findViewById(R.id.day);
                dayCtv.setText(dayForDate(day.getDateEpoch()));

                CustomTextView tempCtv = forecastView.findViewById(R.id.temp);
                tempCtv.setText(getString(R.string.temp_unit_suffice, String.valueOf(day.getDay().getAvgtempC())));
                mForecastContainer.addView(forecastView);
            }
        }
    }

    public void retry(View view) {
        Log.d(TAG, "retry");
        if (mLocationCallback == null) {
            buildLocationCallback();
        }
        if (mCurrentLocation == null) {
            requestLocationUpdate();
        }
    }

    /**
     * if we don't have a last know location then we request a location update.
     */
    private void requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission not available");

            return;
        }

        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null | locationResult.getLocations().size() <= 0) {
                    updateLocationErrorView();
                    return;
                }
                // Update UI with location data
                mCurrentLocation = locationResult.getLocations().get(0);
                Log.i(TAG, "onLocationResult: success ");
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                fetchWeatherData(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                final List<String> permissionsRejected = new ArrayList<>();
                for (String perm : getRequiredPermissions()) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage(R.string.error_permission_needed).
                                    setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            requestPermissions(permissionsRejected.
                                                    toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    }).setNegativeButton(R.string.cancel, null).create().show();

                            return;
                        }
                    }

                } else {
                    getLastLocation();
                }

                break;
        }
    }

    /**
     * Request to get last known location and once get call weather api to get data for the location.
     */
    private void getLastLocation() {
        if (mFusedLocationClient != null) {
            Log.d(TAG, "fusedLocationClient is not null");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onConnected permission not available");

                return;
            }

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            Log.i(TAG, "onSuccess getLastLocation: " + location);
                            if (location != null) {
                                mCurrentLocation = location;
                                fetchWeatherData(location.getLatitude(), location.getLongitude());
                            } else {
                                Log.e(TAG, "error in onSuccess while fetching last location");
                                if (mLocationCallback == null) {
                                    buildLocationCallback();
                                }
                                requestLocationUpdate();
                            }
                        }
                    });
        }
    }

    /**
     * Generates error for location not available case and update the view.
     */
    private void updateLocationErrorView() {
        Error error = new Error();
        BaseError baseError = new BaseError();
        baseError.setCode(ERROR_CODE_LOCATION_NOT_AVAILABLE);
        error.setError(baseError);
        updateView(Status.failed, null, error);
    }

    /**
     * Checks for all required permissiona nd return a list of these if any required permission is not given by user
     */
    private List<String> getRequiredPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return requiredPermissions;
    }


    private void checkRequiredPermissions() {
        List<String> requiredPermissions = getRequiredPermissions();
        if (requiredPermissions.size() > 0) {
            // request for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requiredPermissions.size();
                requestPermissions(requiredPermissions.
                        toArray(new String[requiredPermissions.size()]), ALL_PERMISSIONS_RESULT);
            }
        } else {
            Log.i(TAG, "All required permissions available");
        }
    }

    /**
     * checks if a given permission is provided by user or not
     */
    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            getLastLocation();
        }
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_please_enable_gps, Toast.LENGTH_SHORT).show();
        }

    }
}
