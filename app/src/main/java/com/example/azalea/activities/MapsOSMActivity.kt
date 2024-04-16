package com.example.azalea.activities

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.example.azalea.R
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
    private lateinit var osmMap: MapView
    private val startPoint = GeoPoint(4.6287662, -74.0636298647595)
    private lateinit var endPoint: GeoPoint
    private var longPressedMarker: Marker? = null
    private var roadOverlay: Polyline? = null
    lateinit var roadManager: RoadManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().setUserAgentValue(
            Configuration.getInstance().setUserAgentValue("com.example.pruebaosm")
            .toString())
        setContentView(R.layout.activity_maps_osmactivity)
        osmMap = findViewById(R.id.osmMap)
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.overlays.add(createOverlayEvents())
        roadManager = OSRMRoadManager(this, "ANDROID")

        // Recibir el Bundle de coordenadas finales
        val bundle = intent.extras
        if (bundle != null) {
            val latitude = bundle.getDouble("latitude", 0.0)
            val longitude = bundle.getDouble("longitude", 0.0)
            endPoint = GeoPoint(latitude, longitude)
        } else {
            // Mostrar un Toast indicando que no se encontraron coordenadas finales
            Toast.makeText(this, "No se encontraron coordenadas finales", Toast.LENGTH_SHORT).show()

            // Asignar coordenadas predeterminadas o manejar el caso según sea necesario
            endPoint = GeoPoint(0.0, 0.0)
        }



        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

    }

    override fun onResume() {
        super.onResume()
        osmMap.onResume()
        val mapController: IMapController = osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(startPoint)

        drawRoute(startPoint, endPoint)
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
    }

    private fun createOverlayEvents(): MapEventsOverlay {
        val overlayEvents = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                longPressOnMap(p)
                return true
            }
        })
        return overlayEvents
    }

    private fun longPressOnMap(p: GeoPoint) {
        longPressedMarker?.let { osmMap.overlays.remove(it) }
        longPressedMarker = createMarker(p, "location", null, R.drawable.map_marker)
        longPressedMarker?.let { osmMap.overlays.add(it) }
    }

    private fun createMarker(p: GeoPoint, title: String?, desc: String?, iconID: Int): Marker? {
        var marker: Marker? = null
        if (osmMap != null) {
            marker = Marker(osmMap)
            title?.let { marker.title = it }
            desc?.let { marker.subDescription = it }
            if (iconID != 0) {
                val myIcon = resources.getDrawable(iconID, theme)
                marker.icon = myIcon
            }
            marker.position = p
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        return marker
    }

    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        val routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)

        val road = roadManager.getRoad(routePoints)

        Log.i("OSM_acticity", "Route length: ${road.mLength} klm")
        Log.i("OSM_acticity", "Duration: ${road.mDuration / 60} min")

        if (osmMap != null) {
            roadOverlay?.let { osmMap.overlays.remove(it) }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay?.outlinePaint?.color = Color.RED
            roadOverlay?.outlinePaint?.strokeWidth = 10f
            osmMap.overlays.add(roadOverlay)

            // Agregar marcadores al inicio y al final de la ruta
            val startMarker = createMarker(start, "Inicio", null, R.drawable.map_marker)
            val finishMarker = createMarker(finish, "Fin", null, R.drawable.map_marker)
            startMarker?.let { osmMap.overlays.add(it) }
            finishMarker?.let { osmMap.overlays.add(it) }
        }
    }












}
