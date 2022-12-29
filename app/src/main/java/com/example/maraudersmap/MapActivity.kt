package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import kotlinx.coroutines.*
import okhttp3.Response
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

/**
 * provides a map which shows your own location
 * @author Leo Kalmbach
 * @since 2022.12.25
 */
class MapActivity : AppCompatActivity() {

    private val requestPermissionRequestCode = 1
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var settingsBtn: Button
    private lateinit var toastMessage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance().load(this, getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_map)

        settingsBtn = findViewById(R.id.settings_btn)
        settingsBtn.setOnClickListener {
            val intent = Intent(this@MapActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        mapController = map.controller
        mapController.setZoom(18.0)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        map.overlays.add(locationOverlay)
        map.postInvalidate()

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {

            /**
             * updates current location
             */
            val serializer: Serializer = Persister()
            val userController = UserControllerAPI()
            val longitude = map.mapCenter.longitude
            val latitude = map.mapCenter.latitude
            val userID = LoginActivity.userID

            val response1 : Response = userController.updateUserGpsPosition(latitude, longitude, userID)
            toastMessage = when(response1.code){
                200 -> ""   //successful

                403 -> "Authentication failed"  //Authentication error

                else -> "Unknown Error"     // Unknown error
            }
            if(toastMessage != "") {
                withContext(Dispatchers.Main){
                    makeToast(toastMessage)
                }
            }

            /**
             * requests the location of other users in a radius
             */
            val response : Response = userController.getLocationsWithinRadius(5.0, latitude, longitude)
            val xmlBody = response.body!!.string()
            val data = serializer.read(ResponseData::class.java, xmlBody)
            println(data.toString())
            val markers: ArrayList<Marker> = arrayListOf()
            for (thing in data.thingXTO) {
                markers.add(createMarker(thing.currentLocation.latitude, thing.currentLocation.longitude))
            }
            markers.add(createMarker(49.1218, 9.2114))
            setMarkers(markers)
        }
    }

    /**
     * Data class representing all users with their respective data.
     *
     * @property thingXTO Data of a user.
     */
    @Root(name = "thingXTOs", strict = false)
    data class ResponseData(
        @field:Element(name = "thingXTO", required = false)
        var thingXTO: List<ThingData> = emptyList(),
    )

    /**
     * Data class representing a user with an name, description, privacy radius and their current location.
     *
     * @property name The user's name.
     * @property description The user's description.
     * @property privacyRadius The user's privacy radius.
     * @property currentLocation The user's current location.
     */

    @Root(name = "thingXTO", strict = false)
    data class ThingData(
        @field:Element(name = "name")
        var name: String? = null,

        @field:Element(name = "description")
        var description: String? = null,

        @field:Element(name = "privacyRadius")
        var privacyRadius: Double? = null,

        @field:Element(name = "currentLocation")
        var currentLocation: LocationData,
    )

    /**
     * Data class representing a location with a latitude and longitude.
     *
     * @property latitude The latitude of the location.
     * @property longitude The longitude of the location.
     */
    @Root(name = "currentLocation", strict = false)
    data class LocationData(
        @field:Element(name = "latitude")
        var latitude: Double = 0.0,

        @field:Element(name = "longitude")
        var longitude: Double = 0.0,
    )

    override fun onResume() {
        super.onResume()
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationOverlay.disableMyLocation()
        map.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                requestPermissionRequestCode)
        }
    }

    /**
     * creates a toast
     * @param msg message of the toast
     */
    private fun makeToast(msg: String) {
        Toast.makeText(this@MapActivity, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * sets markers at the positions of the users
     * @param markers ArrayList of the users current location
     */
    private fun setMarkers(markers: ArrayList<Marker>) {
        for(overlay in map.overlays){
            if (overlay != locationOverlay) {
                map.overlays.remove(overlay)
            }
        }
        for (marker in markers) {
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    /**
     * creates a marker for a location
     * @param latitude latitude of a location
     * @param longitude longitude of a location
     * @return a Marker
     */
    private fun createMarker(latitude: Double, longitude: Double): Marker {
        val marker = Marker(map)
        marker.position = GeoPoint(latitude, longitude)
        return marker
    }
}
