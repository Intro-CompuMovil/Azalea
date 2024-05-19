package com.example.azalea.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.R
import com.example.azalea.data.PermissionsCodes.Companion.LOCATION_PERMISSION_CODE
import com.example.azalea.databinding.ActivityMapsOsmactivityBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.ArrayList

class MapsOSMActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapsOsmactivityBinding
    private val startPoint = GeoPoint(4.6287662, -74.0636298647595)
    private lateinit var endPoint: GeoPoint
    private var roadOverlay: Polyline? = null
    private lateinit var roadManager: RoadManager
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var database: FirebaseDatabase
    private lateinit var endpointRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsOsmactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        database = FirebaseDatabase.getInstance()
        endpointRef = database.getReference("Users/$uid/location")
        val helperUid = intent.getStringExtra("uid")
        // Llama a obtainEndPoint con el uid del usuario que está pidiendo ayuda
        if (helperUid != null) {
            obtainEndPoint(helperUid)
        }

        startOSM()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermissionForLocation(this)

        // TODO for development; change to false for production
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun startOSM() {
        // Set userAgent to identify the app to the OSM service
        Configuration.getInstance().userAgentValue = "com.example.azalea"
        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)
        roadManager = OSRMRoadManager(this, "ANDROID")
    }



    private fun obtainEndPoint(helperUid: String) {
        val helperEndpointRef = database.getReference("Users/$helperUid/location")

        helperEndpointRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the location data from the snapshot
                val locationData = dataSnapshot.getValue(String::class.java)

                // Parse the location data
                if (locationData != null) {
                    val latLong = locationData.split(",")
                    val latitude = latLong[0].toDouble()
                    val longitude = latLong[1].toDouble()

                    // Update the endpoint with the new location data
                    endPoint = GeoPoint(latitude, longitude)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log a message if there was an error reading the data
                Log.w("MapsOSMActivity", "Failed to read location.", databaseError.toException())
            }
        })
    }

    private fun setUpLocationRequestAndCallback() {
        mLocationRequest = createLocationRequest()
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val currentLocation = GeoPoint(location.latitude, location.longitude)
                    updateRoute(currentLocation)
                }
            }
        }

        startLocationUpdates()
    }

    private fun createLocationRequest() : LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtainLocation()
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = GeoPoint(location.latitude, location.longitude)
                updateRoute(currentLocation)
            }
        }
    }

    private fun updateRoute(currentLocation: GeoPoint) {
        drawRoute(currentLocation, endPoint)
        val mapController: IMapController = binding.osmMap.controller
        mapController.setCenter(currentLocation)
        binding.osmMap.invalidate()
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun unableToLocate() {
        Toast.makeText(this, "Unable to get own location", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MenuNavigationActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createMarker(p: GeoPoint, title: String?, desc: String?, iconID: Int): Marker? {
        var marker: Marker? = null
        marker = Marker(binding.osmMap)
        title?.let { marker.title = it }
        desc?.let { marker.subDescription = it }

        if (iconID != 0) {
            val myIcon = resources.getDrawable(iconID, theme)
            marker.icon = myIcon
        }
        marker.position = p
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        return marker
    }

    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        val routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)

        val road = roadManager.getRoad(routePoints)

        Log.i("OSM_acticity", "Route length: ${road.mLength} klm")
        Log.i("OSM_acticity", "Duration: ${road.mDuration / 60} min")

        // Build road overlay
        roadOverlay?.let { binding.osmMap.overlays.remove(it) }
        roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay?.outlinePaint?.color = Color.RED
        roadOverlay?.outlinePaint?.strokeWidth = 10f
        binding.osmMap.overlays.add(roadOverlay)

        // Agregar marcadores al inicio y al final de la ruta
        val startMarker = createMarker(start, "Inicio", null, R.drawable.baseline_emoji_people_24)
        val finishMarker = createMarker(finish, "Fin", null, R.drawable.baseline_fmd_bad_24)
        startMarker?.let { binding.osmMap.overlays.add(it) }
        finishMarker?.let { binding.osmMap.overlays.add(it) }
    }

    private fun checkPermissionForLocation(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED -> {
                // Permission already granted
                setUpLocationRequestAndCallback()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

        }
    }

    private fun requestPermission(context: Activity, permission: String, requestCode: Int, justify: String) {
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, justify, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            // Permission granted
            setUpLocationRequestAndCallback()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    setUpLocationRequestAndCallback()
                } else {
                    // Permission denied
                    unableToLocate()
                }
                return
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
