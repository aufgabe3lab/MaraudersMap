package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Response
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.interval

/**
 * provides a map which shows your own location
 * @author Leo Kalmbach
 * @since 2022.12.06
 */
class MapActivity : AppCompatActivity() {

    private val requestPermissionRequestCode = 1
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var settingsBtn: Button

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
        scope.launch(){

            val userController = UserControllerAPI()
            val longitude = map.mapCenter.longitude
            val latitude = map.mapCenter.latitude

           // val response1 : Response = userController.updateUserGpsPosition(latitude,longitude, userID)
            //val responseString1 : String = response1.body!!.toString()


            val response : Response = userController.getLocationsWithinRadius(5L, latitude, longitude)
            val xmlBody = response.body!!.string()
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

    private fun autoUpdatePos(millisInFuture: Long){
       object : CountDownTimer(millisInFuture, 1000){
            override fun onTick(millisUntilFinished: Long) {
                Log.i(MapActivity::class.java.simpleName,"${millisUntilFinished / 1000}")
                val scope = CoroutineScope(Job() + Dispatchers.IO)
                scope.launch {
                    val userControllerAPI = UserControllerAPI()
                    userControllerAPI.updateUserGpsPosition(map.mapCenter.latitude,map.mapCenter.longitude, userID!!)
                }
            }

            override fun onFinish() {
                if(interval != 0L){
                    Log.i(MapActivity::class.java.simpleName,"Finish")
                    start()
                }else{
                    cancel()
                }
            }

       }.start()


    }
}
