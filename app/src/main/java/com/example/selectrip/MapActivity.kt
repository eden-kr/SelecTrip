package com.example.selectrip

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import java.lang.Exception

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    var lat: Double = 0.0
    var lon: Double = 0.0
    var case: Int = 0
    var type: String = ""
    var name: String = ""
    private var placeList = arrayListOf<Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val intent = intent
        lat = intent.getDoubleExtra("lat", 0.0)      //위도
        lon = intent.getDoubleExtra("lon", 0.0)      //경도
        case = intent.getIntExtra("case", 0)         //placeActivity인지 cityActivity인지 구분자
        type = intent.getStringExtra("type")                    //핫플레이스, 맛집 구분자
        name = intent.getStringExtra("name")                    //도시명 구분자
        placeList = PlaceThread(this, type, name).execute().get() as ArrayList<Place>

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        back_map.setOnClickListener {
            finish()
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        var city = LatLng(lat, lon)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(city))        //구글맵 입장
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(city, 15f))

        //아이콘 크기 설정
        val icon = resources.getDrawable(R.drawable.marker) as BitmapDrawable
        var bitmap = icon.bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap,70,70,false)

        when (case) {
            1 -> {          //CityActivity에서 클릭 시
                for (i in 0 until placeList.size) {
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                placeList[i].lat.toDouble(),
                                placeList[i].lng.toDouble()
                            )
                        ).title(placeList[i].name)
                    ).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))   //마커 아이콘을 크기 조정한 이미지로 변경
                }
                mMap.setOnMarkerClickListener(this) // 마커 클릭리스너 설정

            }
            2->{    //placeAcitivy에서 넘어왔을 때
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(city, 18f))
                mMap.addMarker(MarkerOptions().position(LatLng(lat,lon)).icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMarkerClick(p0: Marker?): Boolean {
        var view = layoutInflater.inflate(R.layout.map_marker, null)
        var dialog = Dialog(this)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        val bundle = Bundle()
        //findView
        var markerImg = view.findViewById<ImageView>(R.id.marker_img)
        var markerName = view.findViewById<TextView>(R.id.marker_name)
        var markerHour = view.findViewById<TextView>(R.id.marker_hour)
        var markerCity = view.findViewById<TextView>(R.id.marker_city)
        var markerType = view.findViewById<TextView>(R.id.marker_type)
        var click = view.findViewById<Button>(R.id.marker_click)

        var pos = p0?.position      //position (latlng으로 설정됨)
        for (i in 0 until placeList.size) {
            var latlng = LatLng(
                placeList[i].lat.toDouble(),
                placeList[i].lng.toDouble()
            )       //클릭된 위치의 LatLag으로 클릭된 마커 비교
            if (latlng == pos) {
                markerName.text = placeList[i].name
                markerCity.text = placeList[i].cityname
                markerHour.text = placeList[i].hour
                markerType.text = placeList[i].type
                Glide.with(this).load(placeList[i].image).into(markerImg)
                bundle.putSerializable("place",placeList[i])
            }
        }
        markerImg.clipToOutline = true      //이미지 둥그렇게 잘라주기

        dialog.window?.setGravity(Gravity.BOTTOM)   //다이얼로그 위치 지정
        dialog.show()

        click.setOnClickListener {
            val intent = Intent(this,PlaceActivity::class.java)
            intent.putExtra("bundle",bundle)
            startActivity(intent)
        }

        return true
    }
}

class PlaceThread(var context: Context, var type: String, var name: String) :
    AsyncTask<Void, Void, List<Place>>() {
    var list = arrayListOf<Place>()
    override fun doInBackground(vararg p0: Void?): List<Place> {
        try {
            MyRetrofit.getInstance(context).getDb(name, type).execute().body()?.let { list.addAll(it) }
        } catch (e: Exception) {
            Log.d("myTag", "e.printStackTrace()")
        }
        return list
    }
}
