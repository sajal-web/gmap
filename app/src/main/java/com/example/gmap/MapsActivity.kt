        package com.example.gmap

        import android.content.pm.PackageManager
        import androidx.appcompat.app.AppCompatActivity
        import android.os.Bundle
        import androidx.core.content.ContextCompat
        import android.Manifest;
        import android.graphics.Color
        import android.location.Address
        import android.location.Geocoder
        import android.util.Log
        import android.widget.*
        import androidx.appcompat.app.AlertDialog
        import androidx.core.app.ActivityCompat
        import com.google.android.gms.maps.CameraUpdateFactory
        import com.google.android.gms.maps.GoogleMap
        import com.google.android.gms.maps.OnMapReadyCallback
        import com.google.android.gms.maps.SupportMapFragment
        import com.google.android.gms.maps.model.LatLng
        import com.google.android.gms.maps.model.MarkerOptions
        import com.example.gmap.databinding.ActivityMapsBinding
        import com.google.android.gms.common.api.Status
        import com.google.android.gms.location.Geofence
        import com.google.android.gms.location.GeofencingClient
        import com.google.android.gms.location.LocationServices
        import com.google.android.gms.maps.model.CircleOptions
        import com.google.android.gms.maps.model.PolylineOptions
        import com.google.android.libraries.places.api.Places
        import com.google.android.libraries.places.api.model.Place
        import com.google.android.libraries.places.api.model.TypeFilter
        import com.google.android.libraries.places.api.net.FetchPlaceRequest
        import com.google.android.libraries.places.api.net.FetchPlaceResponse
        import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
        import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
        import com.google.android.libraries.places.widget.AutocompleteSupportFragment
        import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
        import java.io.IOException

        class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
            GoogleMap.OnMapLongClickListener {
            private  val TAG = "MapsActivity"
            private lateinit var map: GoogleMap
            private lateinit var binding: ActivityMapsBinding
            private val REQUEST_LOCATION_PERMISSION = 1
            private var mMap: GoogleMap? = null
            private val GEOFENCE_RADIUS : Float = 500.0F
            private val GEOFENCE_ID = "MyGeofenceId"
            // geofencing
            private lateinit var  geoFencingClient : GeofencingClient
            private lateinit var geofenceHelper: GeofenceHelper
            private var prevLatLng: LatLng? = null // Variable to store the previous location

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityMapsBinding.inflate(layoutInflater)
                setContentView(binding.root)

                isPermissionGranted()
                // Initialize the  Places SDK
                Places.initialize(applicationContext,"AIzaSyAnb0HjXQank6MVfSM_NYbGbF2Lueyy5r4")
                searchLocation()
//                searchFunctionalityNoSuggestions()
                setupMap()
                setupZoomController()
                setupAddGeofenceButton()
                setupSetCounterButton()

                // Initializing geofencing
                geoFencingClient = LocationServices.getGeofencingClient(this)
                geofenceHelper = GeofenceHelper(this)




            }

//            private fun searchFunctionalityNoSuggestions() {
//
//                binding.svLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(p0: String): Boolean {
//                        val location =
//                            p0.trim() // Trim the query text to remove leading/trailing spaces
//                        var addressList: List<Address>? = null
//
//                        if (location.isBlank()) {
//                            Toast.makeText(
//                                applicationContext,
//                                "Please provide a valid location",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            val geocoder = Geocoder(this@MapsActivity)
//                            try {
//                                addressList = geocoder.getFromLocationName(location, 1)
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//
//                            if (addressList != null && addressList.isNotEmpty()) {
//                                val address = addressList[0]
//                                val latLng = LatLng(address.latitude, address.longitude)
//                                mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
//                               zoomToLocation(latLng)
//                            } else {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Location not found",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                        return true
//                    }
//
//                    override fun onQueryTextChange(p0: String?): Boolean {
//                        return false
//                    }
//
//
//
//
//                 Initialize Places API
//
//
//
//                })
//            }

            private fun setupSetCounterButton() {
                binding.fabSetCounter.setOnClickListener {
                    map.clear()
                    showSetCounterDialog()
                }
            }
            private fun searchAndMarkLocations(counterValue: Int) {
                for (i in 1..counterValue) {

                    val dialogView = layoutInflater.inflate(R.layout.dialog_search_location, null)
                    val searchInput = dialogView.findViewById<EditText>(R.id.etSearchLocation)

                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Search Location $i")
                        .setView(dialogView)
                        .setPositiveButton("Search") { _, _ ->

                            val location = searchInput.text.toString().trim()
                            if (location.isNotEmpty()) {
                                searchLocationAndMark(location)
                            } else {
                                Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                    dialog.show()
                }
            }
            private fun searchLocationAndMark(location: String) {
















                val placesClient = Places.createClient(this)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setQuery(location)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                        if (response.autocompletePredictions.isNotEmpty()) {
                            val placeId = response.autocompletePredictions[0].placeId
                            val fetchRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG))
                                .build()

                            placesClient.fetchPlace(fetchRequest)
                                .addOnSuccessListener { fetchResponse: FetchPlaceResponse ->
                                    val place = fetchResponse.place
                                    val latLng = place.latLng
                                    if (latLng != null) {
                                        addMarker(latLng)
                                        connectWithPolyline(latLng)
                                    }
                                }
                                .addOnFailureListener { exception: Exception ->
                                    Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Exception: $exception")
                                }
                        } else {
                            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception: Exception ->
                        Toast.makeText(this, "Failed to search location", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Exception: $exception")
                    }
            }
            private fun connectWithPolyline(latLng: LatLng) {
                if (prevLatLng != null) {
                    mMap!!.addPolyline(
                        PolylineOptions()
                            .add(prevLatLng, latLng)
                            .width(5f)
                            .color(Color.BLUE)
                    )
                }
                prevLatLng = latLng
                zoomToLocation(latLng)
            }

            private fun showSetCounterDialog() {
                val dialogView = layoutInflater.inflate(R.layout.dialog_set_counter, null)
                val counterInput = dialogView.findViewById<EditText>(R.id.etCounter)

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Set Counter Value")
                    .setView(dialogView)
                    .setPositiveButton("OK") { _, _ ->
                        val counterValueText = counterInput.text.toString()
                        val counterValue = counterValueText.toIntOrNull()

                        if (counterValue != null && counterValue > 0) {
                            searchAndMarkLocations(counterValue)
                            // Do something with the counter value (e.g., process the value or use it in your application)
                            // For now, let's just display a toast to show the input value.
                            Toast.makeText(this, "Counter Value: $counterValue", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }

            private fun setupAddGeofenceButton() {
                binding.fabAddGeofence.setOnClickListener {
                    map.clear()
                    showAddGeofenceDialog()
                }
            }

            private fun setupZoomController() {
                binding.zoomControls.setOnZoomInClickListener {
                    map.animateCamera(CameraUpdateFactory.zoomIn())
                }

                binding.zoomControls.setOnZoomOutClickListener {
                    map.animateCamera(CameraUpdateFactory.zoomOut())
                }
            }

            private fun setupMap(){
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
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
                map = googleMap
                mMap = googleMap
                enableMyLocation()
                mMap!!.setOnMapLongClickListener(this)
                // Add a circle around the geofence area

            }

            private fun enableMyLocation() {
                if (isPermissionGranted()) {
                    map.isMyLocationEnabled = true
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
            }
            private fun isPermissionGranted(): Boolean {
                return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }

            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<String>,
                grantResults: IntArray
            ) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == REQUEST_LOCATION_PERMISSION) {
                    if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                        enableMyLocation()
                    }
                }
            }


            override fun onMapLongClick(p0: LatLng) {
                map.clear()
                addMarker(p0)

                addCircle(p0,GEOFENCE_RADIUS)
                addGeofence(p0,GEOFENCE_RADIUS)
            }

            private fun addGeofence(latLng: LatLng,radius: Float){
                val geofence = geofenceHelper.getGeofence(
                    GEOFENCE_ID,
                    latLng.latitude,
                    latLng.longitude,
                    radius,
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT or
                            Geofence.GEOFENCE_TRANSITION_DWELL
                )
                val geofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
                val geofencePendingIntent = geofenceHelper.getGeofencePendingIntent()

                geoFencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener {
                        Log.d(TAG, "onSuccess: Geofence added")
                        Toast.makeText(this, "Geofence added successfully", Toast.LENGTH_SHORT).show()
                        val circleOptions = CircleOptions()
                            .center(latLng)
                            .radius(radius.toDouble())
                            .strokeWidth(5f)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.argb(70, 0, 0, 255))
                        map.addCircle(circleOptions)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add geofence", Toast.LENGTH_SHORT).show()
                        Log.d("onFailure",it.message.toString())
                    }


            }


            private fun addMarker(latLng: LatLng){
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                )

            }
            private fun zoomToLocation(latLng: LatLng){
                mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
            private fun addCircle(latLng: LatLng, radius: Float){
                val circleOptions = CircleOptions()
                    .center(latLng)
                    .radius(radius.toDouble())
                    .strokeWidth(5f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(70,0,0,255))
                map.addCircle(circleOptions)
            }
            private fun showAddGeofenceDialog() {
                val dialogView =layoutInflater.inflate(R.layout.dialog_add_geofence,null)
                val latitudeInput = dialogView.findViewById<EditText>(R.id.etLatitude)
                val longitudeInput = dialogView.findViewById<EditText>(R.id.etLongitude)
                val radiusInput = dialogView.findViewById<EditText>(R.id.etRadius)

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Add Geofence")
                    .setView(dialogView)
                    .setPositiveButton("Add"){_, _ ->
                        val latitudeText = latitudeInput.text.toString().toDoubleOrNull()
                        val longitudeText = longitudeInput.text.toString().toDoubleOrNull()
                        val radius = radiusInput.text.toString().toFloatOrNull()
                        if (latitudeText != null && longitudeText != null && radius != null && radius > 0) {
                            val latLng = LatLng(latitudeText, longitudeText)
                            Log.d("dialogSuccess", "Latitude: $latitudeText, Longitude: $longitudeText, Radius: $radius")
                            addMarker(latLng)
                            addCircle(latLng, radius)
                            addGeofence(latLng, radius)
                            zoomToLocation(latLng)

                        } else {
                            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel",null)
                    .create()
                dialog.show()
            }
            private fun searchLocation(){

                val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
                autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
                    override fun onError(p0: Status) {
                        Log.d("searchError", "Error: ${p0.statusMessage}")
                    }

                    override fun onPlaceSelected(place: Place) {
                        val latLng = place.latLng
                        val name = place.name
                        Log.d("searchSuccess","$latLng")
                        addMarker(latLng)
                        zoomToLocation(latLng)
                    }

                })
            }


            private fun openAutocompleteFragment() {
                val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
                autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
                    override fun onError(p0: Status) {
                        Log.d("searchError", "Error: ${p0.statusMessage}")
                    }

                    override fun onPlaceSelected(place: Place) {
                        val latLng = place.latLng
                        val name = place.name
                        Log.d("searchSuccess","$latLng")
                        // Handle the selected place, e.g., add a marker, zoom to location, etc.
                    }
                })
            }


        }
