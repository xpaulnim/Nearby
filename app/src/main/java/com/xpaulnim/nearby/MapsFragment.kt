package com.xpaulnim.nearby

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*
import com.xpaulnim.nearby.Utils.convertDrawableToBitmap
import com.xpaulnim.nearby.databinding.MapFragmentBinding
import com.xpaulnim.nearby.network.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MapsFragment : Fragment(R.layout.map_fragment) {

    companion object {
        private const val TAG = "MapsFragment"

        val PERMISSIONS = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    }

    var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!  // This property is only valid between onCreateView and onDestroyView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var pointAnnotationManager: PointAnnotationManager? = null
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val gson = Gson()
    private var nearbyJob: Job? = null

    private val onMapCameraChangeListener = OnCameraChangeListener {
        Log.i(TAG, "Camera position changed to ${binding.mapView.getMapboxMap().cameraState.center}")

        if (binding.searchAreaButton.visibility == View.INVISIBLE) {
            binding.searchAreaButton.visibility = View.VISIBLE
        }
    }

    private val onSearchAreaButtonListener = View.OnClickListener {
        Log.i(TAG, "Search this area button clicked")

        val mapCenter = binding.mapView.getMapboxMap().cameraState.center
        Log.i(TAG, "MapCenter: $mapCenter")
        searchLocationRadius(mapCenter.latitude(), mapCenter.longitude())

        if (binding.searchAreaButton.visibility == View.VISIBLE) {
            binding.searchAreaButton.visibility = View.INVISIBLE
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }

        if (granted) {
            Log.i(TAG, "Coarse and fine location permissions granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)

//        _binding.lifeCycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Look into using subscribe() instead of listener
        binding.mapView.getMapboxMap().addOnCameraChangeListener(onMapCameraChangeListener)
        binding.searchAreaButton.setOnClickListener(onSearchAreaButtonListener)
        binding.dismissCardViewButton.setOnClickListener { binding.cardView.visibility = View.INVISIBLE }
        binding.deviceLocationButton.setOnClickListener {
            Log.i(TAG, "Going to device location")
            enableUserLocationMarker()

            getDeviceLocation {
                if (it != null) {
                    easeMapToPoint(it.latitude, it.longitude)
                } else {
                    Log.w(TAG, "Could not get user location")
                }
            }
        }

        /*
        https://github.com/material-components/material-components-android/blob/master/catalog/java/io/material/catalog/card/CardSwipeDismissFragment.java
        val swipeDismissBehavior = SwipeDismissBehavior<View>()
        swipeDismissBehavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
        swipeDismissBehavior.listener = object : OnDismissListener {
            override fun onDismiss(view: View?) {
                Log.i(TAG, "CarView.onDismiss")
                resetCard(cardContentLayout = binding.cardView)
                val params = binding.cardView.layoutParams as CoordinatorLayout.LayoutParams

                params.setMargins(0, 0, 0, 0)
                binding.cardView.alpha = 1.0f
                binding.cardView.requestLayout()
            }

            override fun onDragStateChanged(state: Int) {
                Log.i(TAG, "CarView.onDragStateChanged")
                when (state) {
                    SwipeDismissBehavior.STATE_DRAGGING, SwipeDismissBehavior.STATE_SETTLING -> binding.cardView.isDragged = true
                    SwipeDismissBehavior.STATE_IDLE -> binding.cardView.isDragged = false
                }
            }
        }

        val coordinatorParams = binding.cardView.layoutParams as CoordinatorLayout.LayoutParams
        coordinatorParams.behavior = swipeDismissBehavior
        */

        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().zoom(16.0).build()
        )

        pointAnnotationManager = binding.mapView.annotations
            .createPointAnnotationManager(AnnotationConfig(layerId = "wikipediaPoiLayer")).apply {
                this.addClickListener {
                    Log.i(TAG, "Annotation clicked id:${it.id}, ${it.point}, ${it.getData()}")

                    val clickedPoi = gson.fromJson(gson.toJson(it.getData()), GeoSearchResult::class.java)
                    getPageIdSummary(clickedPoi.pageid)

                    binding.cardView.visibility = View.VISIBLE
                    true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.getMapboxMap().removeOnCameraChangeListener(onMapCameraChangeListener)
        _binding = null
    }

    private fun enableUserLocationMarker() {
        if (!checkPermission(ACCESS_FINE_LOCATION) && !checkPermission(ACCESS_COARSE_LOCATION)) {
            requestLocationPermission()
        }

        val locationComponentPlugin = binding.mapView.location
        binding.mapView.location.createDefault2DPuck(requireContext())
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = binding.mapView.location.createDefault2DPuck(requireContext())
        }

//        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun addPointsToMap(geoSearchResults: List<GeoSearchResult>, clearMap: Boolean = true) {
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            if (clearMap) {
                pointAnnotationManager?.delete(pointAnnotations)
            }

            val poiIcon = convertDrawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_marker))

            val annotationOptions = geoSearchResults.map {
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(it.lon, it.lat))
                    .withIconImage(poiIcon!!)
                    .withData(JsonParser.parseString(gson.toJson(it)).asJsonObject)
            }

            val createdPointAnnotations = pointAnnotationManager?.create(annotationOptions)
            if (createdPointAnnotations != null) {
                pointAnnotations.addAll(createdPointAnnotations)
            }

//            easeMapToPoint(pointsOfInterest[0].latitude, pointsOfInterest[0].longitude)
        }
    }

    private fun easeMapToPoint(latitude: Double, longitude: Double) {
        binding.mapView.getMapboxMap().easeTo(cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
        })
    }

    private fun getDeviceLocation(callback: (Location?) -> Unit) {
        if (!checkPermission(ACCESS_FINE_LOCATION) && !checkPermission(ACCESS_COARSE_LOCATION)) {
            requestLocationPermission()
        } else {
            Log.w(TAG, "User denied location request")
        }

        fusedLocationProviderClient.lastLocation.apply {
            addOnSuccessListener { location: Location? ->
                callback(location)
            }

            addOnFailureListener {
                Log.i(TAG, "Failed to get device location ${it.message}")
            }
        }
    }

    //  https://stackoverflow.com/questions/67857809/attaching-mapbox-map-to-livedata
    private fun searchLocationRadius(latitude: Double, longitude: Double) {
        nearbyJob?.cancel()
        nearbyJob = lifecycleScope.launch {
            val position = Position(latitude, longitude)

            try {
                val response = NearByApi.retrofitService.searchPointRadius(position.ggscoord, position.radius)

                logResponse(response)
                if (response.query != null) {
                    if (response.query.geosearch == null || response.query.geosearch.isEmpty()) {
                        Log.i(TAG, "No POIs found NearBy")
                        Snackbar.make(this@MapsFragment.requireView(), "No POIs found NearBy", BaseTransientBottomBar.LENGTH_SHORT).show()
                    } else {
                        addPointsToMap(response.query.geosearch)
                    }
                }
            } catch (ex: Exception) {
                Log.i(TAG, "Query failed: ${ex.message}")
            }
        }
    }

    private fun getPageIdSummary(pageId: Int) {
        nearbyJob?.cancel()
        nearbyJob = lifecycleScope.launch {
            try {
                val response = NearByApi.retrofitService.getPageIdSummary(pageId)

                if (response.query != null) {
                    if (response.query.pages == null || response.query.pages.isEmpty()) {
                        Log.i(TAG, "Could not get page summary for pageid $pageId")
                        Snackbar.make(
                            this@MapsFragment.requireView(),
                            "Could not get page summary",
                            BaseTransientBottomBar.LENGTH_SHORT
                        ).show()
                    } else {
                        val pageTitle = response.query.pages[0].title
                        binding.titleTextView.text = pageTitle

                        val pageSummary = response.query.pages[0].extract
                        if (pageSummary != null) {
                            Log.i(TAG, pageSummary)

                            binding.descriptionTextView.text = TextUtils.ellipsize(
                                pageSummary, TextPaint(), 500f, TextUtils.TruncateAt.END
                            )
                        } else {
                            Log.i(TAG, "No page summary")
                        }

                        val pageCoordinates = response.query.pages[0].coordinates
                        if (pageCoordinates != null && pageCoordinates.isNotEmpty()) {
                            val pageLat = pageCoordinates[0].lat
                            val pageLon = pageCoordinates[0].lon

                            if (pageLon != null && pageLat != null) {
                                getDeviceLocation {
                                    if (it != null) {
                                        val results = FloatArray(1)
                                        Location.distanceBetween(it.latitude, it.longitude, pageLat, pageLon, results)
                                        binding.distanceFromPoiTextView.text = "${results[0].toInt()} meters"
                                    } else {
                                        Log.i(TAG, "Could not get user location")
                                    }
                                }
                            } else {
                                Log.i(TAG, "WikiCoordinate lat or lon is null")
                            }
                        } else {
                            Log.i(TAG, "PageCoordinates are null")
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.i(TAG, "Query failed: ${ex.message}")
            }
        }
    }

    private fun logResponse(response: WikipediaResponse) {
        if (response.query != null) {
            Log.i(TAG, "API returned ${response.query.geosearch!!.size}")

            response.query.geosearch.forEach { Log.i(TAG, it.title) }
        }
    }

    private fun requestLocationPermission() {
        Log.i(TAG, "Requesting user permission")

        requestPermissionLauncher.launch(PERMISSIONS)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun searchMapBoundingBox() {
        nearbyJob?.cancel()
        nearbyJob = lifecycleScope.launch {
            val mapBounds = binding.mapView.getMapboxMap().getBounds().bounds

            val bb = BoundingBox(mapBounds.north(), mapBounds.west(), mapBounds.south(), mapBounds.east())
            try {
                val response = NearByApi.retrofitService.searchBoundingBox(bb.ggsbbox)

                logResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error ${e.message}")
            }
        }

    }
}
