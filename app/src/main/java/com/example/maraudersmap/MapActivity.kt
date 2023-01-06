package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.interval
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
import org.simpleframework.xml.*
import org.simpleframework.xml.core.Persister
/**
 * provides a map which shows your own location
 * @author Leo Kalmbach & Julian Ertle
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

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this@MapActivity), map)
        map.overlays.add(locationOverlay)
        map.postInvalidate()

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {

            /**
             * updates current location
             */
            val serializer: Serializer = Persister()
            val userController = UserControllerAPI()
            val longitude : Double = map.mapCenter.longitude
            val latitude : Double = map.mapCenter.latitude
            val userID = LoginActivity.userID


            val response1 : Response = userController.updateUserGpsPosition(latitude, longitude, userID)
            toastMessage = when(response1.code){
                200 -> ""   //successful

                403 -> ({
                    getString(R.string.permissionDenied_text)
                    val intent = Intent(this@MapActivity, LoginActivity::class.java)
                    startActivity(intent)
                }).toString()  //Authentication error


                else -> getString(R.string.unknownError_text)     // Unknown error
            }
            if(toastMessage != "") {
                withContext(Dispatchers.Main){
                    makeToast(toastMessage)
                }
            }
        }

        if(interval != 0L){
            autoUpdatePos(interval * 1000)
        }

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


    /**
     * Data class representing all users with their respective data.
     *
     * @property thingXTO Data of a user.
     */
    @Root(name = "thingXTOs", strict = false)
    data class ResponseData(
        @field:ElementList(name = "thingXTO", inline = true, required = true)
        var thingXTOs: List<ThingXTO>?
    ) {
        constructor() : this(null)
    }

    @Root(name = "currentLocation", strict = false)
    data class CurrentLocation(
        @field:Element(name = "latitude", required = false)
        var latitude: Double?,
        @field:Element(name = "longitude", required = false)
        var longitude: Double?
    ) {
        constructor() : this(null, null)
    }


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



    /**class Description {
        @Text(required = false)
        var text: String? = null
    }*/

    /**
     * Data class representing a location with a latitude and longitude.
     *
     * @property latitude The latitude of the location.
     * @property longitude The longitude of the location.
     */

    fun parseXML(xmlString: String): ResponseData {
        val serializer = Persister()
        return serializer.read(ResponseData::class.java, xmlString)
    }

    private fun autoUpdatePos(millisInFuture: Long){
       object : CountDownTimer(millisInFuture,1000){
            override fun onTick(millisUntilFinished: Long) {
                //println("tick")
            }

            override fun onFinish() {
                if(interval != 0L){
                    //Log.i(MapActivity::class.java.simpleName,"Finish")

                    val scope = CoroutineScope(Job() + Dispatchers.IO)
                    scope.launch {
                        val userController = UserControllerAPI()

                        userController.updateUserGpsPosition(map.mapCenter.latitude,map.mapCenter.longitude, userID!!)

                        val latitude = map.mapCenter.latitude
                        val longitude = map.mapCenter.longitude

                        val response : Response = userController.getLocationsWithinRadius(10L,latitude, longitude)

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


                        try {
                            val data = parseXML(xmlBody)
                            println("Amount of visible accounts: " + data.thingXTOs!!.size)
                            val markers: ArrayList<Marker> = arrayListOf()

                            markers.add(createMarker(latitude, longitude))  // add own position

                            for (thing in data.thingXTOs!!) {
                                markers.add(createMarker(thing.currentLocation!!.latitude!!, thing.currentLocation!!.longitude!!))
                            }
                            setMarkers(markers)
                        }
                        catch (e : Exception){
                            println(e)
                            e.printStackTrace()
                        }
                    }
                    start()
                }else{
                    cancel()
                }
            }

       }.start()
    }
}
