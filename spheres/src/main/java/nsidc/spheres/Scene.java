package nsidc.spheres;

import java.lang.*;
/*************************************************************************
 * Scene class - provide services and information relating to a scene.
 * <P>
 * A Scene is a piece of an orbit, a partial orbit.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 13-December-2000 swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 * @see nsidc.spheres.Orbit
 **************************************************************************/

import java.lang.*;

public class Scene extends Orbit
{
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     * Circular Latitude (0-360) of nadir at the start of the scene.
     */
    public double start_clat;   
    /**
     * Circular Latitude (0-360) of nadir at the end of the scene.
     */
    public double end_clat;     
    /**
     * Nodal Crossing of the orbit that this scene is part of.
     */
    public double nodal_crossing;
    
     
    private final int logLevel = 5;

    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    public Scene(Orbit given_orbit, String given_start, String given_end, String given_crossing)
    {
	this.init(given_orbit,
		  Double.valueOf(given_start).doubleValue(), 
		  Double.valueOf(given_end).doubleValue(),
		  Double.valueOf(given_crossing).doubleValue());
    }
    
    public Scene(Orbit given_orbit, float given_start, float given_end, float given_crossing)
    {
	this.init(given_orbit,
		  (double) given_start, 
		  (double) given_end, 
		  (double) given_crossing);
    }
    
    public Scene(float given_declination, 
	  float given_period, 
	  float given_eq_swath_width,
	  float given_start, float given_end, float given_crossing)
    {
	this.init(new Orbit(given_declination, given_period, 
			    given_eq_swath_width),
		  (double) given_start, 
		  (double) given_end, 
		  (double) given_crossing);
    }
    
    public Scene(double given_declination, 
	  double given_period, 
	  double given_eq_swath_width,
	  double given_start, double given_end, double given_crossing)
    {
	this.init(new Orbit(given_declination, given_period, 
			    given_eq_swath_width),
		  (double) given_start, 
		  (double) given_end, 
		  (double) given_crossing);
    }
    
    /*******************************************************************
     * The initializers.
     *******************************************************************/
    
    private void init(Orbit given_orbit, 
		      double given_start, double given_end, 
		      double given_crossing)
    {
	copy_orbit(given_orbit);
	start_clat = given_start;
	end_clat = given_end;
	nodal_crossing = given_crossing;
    }
    
    private void copy_orbit(Orbit given_orbit)
    {
	declination = given_orbit.declination;
	period = given_orbit.period;
	eq_swath_width = given_orbit.eq_swath_width;
	inflection_lat = given_orbit.inflection_lat;
	inflection_max_lat = given_orbit.inflection_max_lat;
	maxCoverageLat = given_orbit.maxCoverageLat;
	totalCoverageLat = given_orbit.totalCoverageLat;
    }


/*************************************************************************
     * Get the longitude of nadir at the given latitude
     *
     * @param lat Latitude of the given point in degrees.
     * @param lon[] Array for output of longitude of the given point in degrees.
     * @param ascending If true find edges for the orbit with nadir  
     *     	passing through the given point on the ascending pass.<br>
     *    	Else find the edges for the orbit with nadir 
     *     	passing through the given point on the descending pass.
     * @param nodal_crossing Crossing of the orbit of interest.
     * <P>   
     *	<b>Notes:</b><br>	
     *          'right' and 'left' are from the satellites perspective.
     * <P>
     *		If any of the edge location pointers are NULL this procedure
     *		will only calculate the nadir longitude.
     * <P>
     *		This routine assumes the orbit is a great circle and does not
     *		correct for nodal regression.  Consequently values for orbits 
     *		other than sun-syncronous will be in error.
     *	
     **************************************************************************/
    
    public float getNadirLongitude(float lat,  
				   boolean ascending)
    {
	double theta, lamn, numerator, denominator, distance, adjustment;

	/*
	 * Use law of sines to find theta (the angle between the desired 
	 * longitude and the track)
	 */
	theta =  Math.asin(Math.sin(radians(declination)) / 
		      Math.sin(radians(90 - lat)));
	if(ascending) theta =  Math.PI - theta;
	
	/*
	 *  Use Napiers second analogy (CRC Standard Math Tables, 26th Edition, p. 147)
	 *    to find the desired longitude.
	 */
	if(0.0 == lat) 
	    {
		if(ascending)
		    lamn = radians(nodal_crossing);
		else
		    lamn = (radians(nodal_crossing) + Math.PI);
	    }
	else
	    {
		numerator = Math.cos(radians(declination) / 2 - theta / 2) * Math.sin(radians(-lat / 2));
		denominator = Math.sin(radians(declination) / 2 - theta / 2) * Math.sin(radians(90 - lat / 2));
		lamn = radians(nodal_crossing) - 2 * Math.atan2(numerator,denominator); 
	    }
	
	/*
	 *  Use Napiers first analogy to find the distance from the nodal crossing to 
	 *    the point in order to correct for the rotation of the Earth.
	 */  
	if(0.0 == lat) 
	    {
		if(ascending)
		    distance = 0;
		else
		    distance = Math.PI;
	    }
	else
	    {
		numerator = Math.sin(radians(-lat / 2)) *
		    Math.sin(radians(declination) / 2 + theta / 2);
		denominator = Math.cos(radians(-lat / 2)) *
		    Math.sin(radians(declination) / 2 - theta / 2);
		distance = 2 * Math.atan2(numerator, denominator);
	    }
	systemLog("DISTANCE: before: "+distance,3);
	if(0 > distance) distance += (2 * Math.PI);
	systemLog(" after: "+distance,3);
	adjustment = period * distance * ROTATION_RATE / (2 * Math.PI);
	lamn -= adjustment;
	return((float)normalize(degrees(lamn)));

    } /* END getNadirLongitude */
    


    /*******************************************************************
     * Conversion methods.
     *******************************************************************/

    /**
     * Convert the scene to a spherical polygon.
     *
     * @see nsidc.spheres.SphericalPolygon
     */
    public SphericalPolygon toSphericalPolygon(int side_points)
    {
	
	double lat_step = (end_clat - start_clat)/(double)(side_points-2);
	Point[] tmp_points = new Point[side_points*2];
	double lat, lon;
	float[] edge_lat = new float[2];
	float[] edge_lon = new float[2];
	int i=0;
	
	if(start_clat < inflection_lat && end_clat > inflection_lat)
	   lat_step = ((end_clat - circularLat(inflection_lat, false) +
		       circularLat(inflection_lat, true) - start_clat))/(double)(side_points-2);

	   if(start_clat < circularLat(-inflection_lat, false) && 
	      end_clat > circularLat(-inflection_lat, false))
	   lat_step = ((end_clat - circularLat(-inflection_lat, true) +
			circularLat(-inflection_lat, false) - start_clat))/(double)(side_points-2);
	
	   /************
	System.out.println("to SP "+this.toString());

	lon = getNadirLongitude((float)0.01, true);
	getCrossSwathEdges((float)standardLat(0.01),
			   (float)lon,	edge_lat, edge_lon,
			   true);
	tmp_points[i] = new Point(edge_lat[0], edge_lon[0]);
	tmp_points[tmp_points.length-i-1] =  new Point(edge_lat[1], edge_lon[1]);
	//System.out.print(".");
	System.out.println("Nadir: ("+0.01+", "+lon+") POINTS: ("+i+", "+(tmp_points.length-i-1)+") : "+
				   tmp_points[i].toString()+", "+
				   tmp_points[tmp_points.length-i-1].toString());
	   *****/ 
	lat = start_clat - lat_step;
	for(i=0; i < side_points-1; i++)
	    {
		lat += lat_step;
		
		if(lat > end_clat)
		    System.out.println("OVERSHOT! "+lat+" > "+end_clat);
		    

		if(lat > circularLat(inflection_lat, true) &&
		   lat < circularLat(inflection_lat, false))
		    lat = circularLat(inflection_lat, false)+0.00001;
		if(lat > circularLat(-inflection_lat, false) &&
		   lat < circularLat(-inflection_lat, true))
		    lat = circularLat(-inflection_lat, true)+0.00001;
			
		lon = getNadirLongitude((float)lat, lat<90.0 || lat>270.0);
		getCrossSwathEdges((float)standardLat(lat),
				   (float)lon,	edge_lat, edge_lon,
				   lat < 90.0 || lat > 270.0);
		tmp_points[i] = new Point(edge_lat[0], edge_lon[0]);
		tmp_points[tmp_points.length-i-1] =  new Point(edge_lat[1], edge_lon[1]);
		
		systemLog("Nadir: ("+lat+", "+lon+") POINTS: ("+i+", "+(tmp_points.length-i-1)+") : "+
			  tmp_points[i].toString()+", "+tmp_points[tmp_points.length-i-1].toString(),4);
	    }

	lon = getNadirLongitude((float)end_clat, (lat<90.0 || lat>270.0));
	getCrossSwathEdges((float)standardLat(lat),
			   (float)lon,	edge_lat, edge_lon,
			   lat < 90.0 || lat > 270.0);
	tmp_points[i] = new Point(edge_lat[0], edge_lon[0]);
	tmp_points[tmp_points.length-i-1] =  new Point(edge_lat[1], edge_lon[1]);
	
	systemLog("lat: "+lat+" POINTS: "+tmp_points[i].toString()+", "+
		  tmp_points[tmp_points.length-i-1].toString(),4);
	
	return new SphericalPolygon(tmp_points);
	
    } /* END toSphericalPolygon(int) */

    /*******************************************************************
     * Overlap methods for other scenes, spherical polygons, and
     * lat/lon bounding boxes.
     *******************************************************************/
    
    /**
     * Determine if this scene overlaps another.
     * 
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc instersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     *
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     *
     * @see nsidc.spheres.GreatCircleArc
     * @see nsidc.spheres.SmallCircleArc
     **/
    public boolean overlaps(Scene other)
    {

	return false;
    }

    /**
     * Determine if this scene overlaps a spherical polygon.
     * 
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc intersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     *
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     *
     * @see nsidc.spheres.SphericalPolygon
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(SphericalPolygon s_poly)
    {

	return false;
    }
    
    /**
     * Determine if this scene overlaps a lat/lon bounding box.
     * 
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc intersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     *
     * @return true if the areas overlap.<br>
     *         false if the ares donot overlap.
     *
     * @see nsidc.spheres.LatLonBoundingBox
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(LatLonBoundingBox box)
    {

	return false;
    }

    public String toString()
    {
	return("Scene [start: "+start_clat+" end: "+end_clat+" crossing: "+nodal_crossing+
	       " inf lat: "+inflection_lat+" width: "+eq_swath_width+"]");
    }

} /* END Scene class */




