package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.interval
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.visibilityRadius
import com.example.maraudersmap.backend.UserControllerAPI
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
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister


/**
 * provides a map which shows your own location
 * @author Leo Kalmbach & Julian Ertle
 * @since 2023.01.08
 */
class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var centerBtn: Button
    private lateinit var numberView: TextView
    private lateinit var toastMessage: String
    private val markers: ArrayList<Marker> = arrayListOf()
    private val requestPermissionRequestCode = 1
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance().load(this, getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_map)

        centerBtn = findViewById(R.id.center_btn)
        centerBtn.setOnClickListener {
            locationOverlay.disableFollowLocation()
            locationOverlay.enableFollowLocation()
        }
        numberView = findViewById(R.id.number_tV)
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        mapController = map.controller
        mapController.setZoom(18.0)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this@MapActivity), map)
        map.overlays.add(locationOverlay)
        map.postInvalidate()

        if (interval == 0L) {
            autoUpdatePos(5000)
        }

        if(interval != 0L){
            autoUpdatePos(interval * 1000)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

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

    /**
     * removes markers of the users
     * @param markers ArrayList of the users current location
     */
    private fun removeMarkers(markers: ArrayList<Marker>) {
        for (marker in markers) {
            map.overlays.remove(marker)
        }
        markers.clear()
        map.invalidate()
    }

    /**
     * Data class representing all users with their respective data.
     *
     * @property thingXTOs Data of a user.
     */
    @Root(name = "thingXTOs", strict = false)
    data class ResponseData(
        @field:ElementList(name = "thingXTO", inline = true, required = true)
        var thingXTOs: List<ThingXTO>?
    ) {
        constructor() : this(null)
    }

    /**
     * Data class representing a user with his data
     *
     * @property name The name of the user.
     * @property description The description of the user.
     * @property privacyRadius The privacy radius of the user.
     * @property currentLocation The current location of the user.
     */
    @Root(name = "thingXTO", strict = false)
    data class ThingXTO(
        @field:Element(name = "name", required = false)
        var name: String?,
        @field:Element(name = "description", required = false)
        var description: String?,
        @field:Element(name = "privacyRadius", required = false)
        var privacyRadius: Double?,
        @field:Element(name = "currentLocation", required = false)
        var currentLocation: CurrentLocation?
    ) {
        constructor() : this(null, null, null, null)
    }

    /**
     * Data class representing a location with a latitude and longitude.
     *
     * @property latitude The latitude of the location.
     * @property longitude The longitude of the location.
     */
    @Root(name = "currentLocation", strict = false)
    data class CurrentLocation(
        @field:Element(name = "latitude", required = false)
        var latitude: Double?,
        @field:Element(name = "longitude", required = false)
        var longitude: Double?
    ) {
        constructor() : this(null, null)
    }

    /**
     * parses an xmlBody
     * @param xmlString
     */
    fun parseXML(xmlString: String): ResponseData {
        val serializer = Persister()
        return serializer.read(ResponseData::class.java, xmlString)
    }

    /**
     * Starts a timer. OnFinish the user location is updated and the position of other
     * users in a radius are requested
     * @param millisInFuture Length of the timer
     */
    private fun autoUpdatePos(millisInFuture: Long){
       timer = object : CountDownTimer(millisInFuture,1000){
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val scope = CoroutineScope(Job() + Dispatchers.IO)
                scope.launch {
                    val userController = UserControllerAPI()
                    val latitude = locationOverlay.myLocationProvider.lastKnownLocation.latitude
                    val longitude = locationOverlay.myLocationProvider.lastKnownLocation.longitude

                    userController.updateUserGpsPosition(latitude,longitude, userID!!)

                    val response : Response = userController.getLocationsWithinRadius(visibilityRadius, latitude, longitude)

                    var xmlBody = response.body!!.string()
                    xmlBody = xmlBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")

                    toastMessage = when(response.code){
                        200 -> ""
                        403 -> ({
                            getString(R.string.permissionDenied_text)
                            val intent = Intent(this@MapActivity, LoginActivity::class.java)
                            startActivity(intent)
                        }).toString()
                        else -> getString(R.string.unknownError_text)
                    }

                    if(toastMessage != "") {
                        withContext(Dispatchers.Main){
                            makeToast(toastMessage)
                        }
                    }
                    removeMarkers(markers)
                    try {
                        val data = parseXML(xmlBody)
                        println("Amount of visible accounts: " + data.thingXTOs!!.size)

                        for (thing in data.thingXTOs!!) {
                            val marker =createMarker(thing.currentLocation!!.latitude!!, thing.currentLocation!!.longitude!!)
                            marker.title = "User: ${thing.name} " +
                                    "\nDescription: ${thing.description} " +
                                    "\nPrivacyRadius: ${thing.privacyRadius} "
                            markers.add(marker)
                        }
                        setMarkers(markers)
                    }
                    catch (e : Exception){
                        println(e)
                        e.printStackTrace()
                    }
                    numberView.text = getString(R.string.displayUser_text,markers.size)
                }
                if(interval != 0L){
                    start()
                }else{
                    cancel()
                }
            }
       }.start()
    }

    /**
     * Handles the selection of an item in the options menu.
     *
     * @param item The selected item in the options menu.
     *
     * @return A boolean indicating whether the selection was handled successfully.
     * Possible return values are:
     * - `true`: The selection was handled successfully and the activity should close the options menu.
     * - `false`: The selection was not handled successfully and the options menu should remain open.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                timer.cancel()
                val intent = Intent(this@MapActivity, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_logOut -> {
                userID = null
                timer.cancel()
                val intent = Intent(this@MapActivity, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
