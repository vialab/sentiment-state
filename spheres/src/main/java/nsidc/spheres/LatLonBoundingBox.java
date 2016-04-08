package nsidc.spheres;

import java.lang.*;
/*************************************************************************
 * LatLonBoundingBox class - provide services and information relating to 
 * a lat/lon bounding box.
 * <P>
 * A bounding box is a rectangle on a simple cylindrical projection. 
 * It is defined solely by lat/lon extremes and corner points are 
 * connected with rhumb lines.  The top and bottom of a lat/lon bounding 
 * box go east/west, the sides go north/south.  
 * <P>
 * This class assumes a flat, rectangular earth.
 *
 * @author 
 * 15-December-2000 R.Swick swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 **************************************************************************/

public class LatLonBoundingBox 
{
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     * Minimum latitude of the bounding box.
     */
    public double lat_min; 
    
    /**
     * Maximum latitude of the bounding box.
     */
    public double lat_max;
    
    /**
     * Minimum longitude of the bounding box.
     */
    public double lon_min;

    /**
     * Maximum longitude of the bounding box.
     */
    public double lon_max;
      
    /**
     * Perimeter (in degrees lat/lon) of the bounding box.
     */
    public double perimeter;

    /**
     * Longitude range of the bounding box.
     */
    public LonRange lon_range;
 
    /* 5 = 0ff, 0 = full on */
    private final int logLevel = 0;
    
    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    /**
     * Constructs a lat/lon bounding box.
     */
    public LatLonBoundingBox(double given_lat_min, double given_lat_max,
		      double given_lon_min, double given_lon_max)
    {
	this.init(given_lat_min, given_lat_max,
		  given_lon_min, given_lon_max);
    }
    
    /**
     * Constructs a lat/lon bounding box.
     */
    public LatLonBoundingBox(float given_lat_min, float given_lat_max,
				float given_lon_min, float given_lon_max)
    {
	this.init((double) given_lat_min, (double) given_lat_max,
		  (double) given_lon_min, (double) given_lon_max);
    }
    
    /**
     * Constructs a lat/lon bounding box.
     */
    public LatLonBoundingBox(String given_lat_min, String given_lat_max,
		      String given_lon_min, String given_lon_max)
    {
	this.init(Double.valueOf(given_lat_min).doubleValue(), 
		  Double.valueOf(given_lat_max).doubleValue(),
		  Double.valueOf(given_lon_min).doubleValue(), 
		  Double.valueOf(given_lon_max).doubleValue());
    }
    
    
    /*******************************************************************
     * The initializer.
     *******************************************************************/
    
    private void init(double given_lat_min, double given_lat_max,
		     double given_lon_min, double given_lon_max)
    {
	
	lat_min = given_lat_min;
	lat_max = given_lat_max;
	lon_min = given_lon_min;
	lon_max = given_lon_max;
	lon_range = new LonRange(lon_min, lon_max);
	perimeter = 2*(lat_max - lat_min) + 2*(lon_max - lon_min);
    
    }
    
    /*******************************************************************
     * Contains method.
     *******************************************************************/
    
    /**
     * Determine if this box contains a given point.
     * <P>
     * It's just a lat/lon bounding box so we only have to check the ranges.
     * <P>
     * @param given_point Point of interest.
     * <P>
     * @returns true  If the point is inside the box.
     *          false If the point is outside the box.
     */

    public boolean contains(Point given_point)
    {
	if(lat_max < given_point.lat)
	    return false;
	if(lat_min > given_point.lat)
	    return false;

	return(lon_range.contains(given_point.lon));
	
    }


    /*******************************************************************
     * The overlap methods.
     *******************************************************************/
    
    /** 
     * Determine if this bounding box overlaps another.
     * <P>
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     **/
    public boolean overlaps(LatLonBoundingBox other)
    {
	if(!((other.lon_min <= lon_min && lon_min <= other.lon_max) ||
	     (other.lon_min <= lon_max && lon_max <= other.lon_max) ||
	     (lon_min <= other.lon_min && other.lon_min <= lon_max) ||
	     (lon_min <= other.lon_max && other.lon_max <= lon_max)))
	    return false;
	
	if(!((other.lat_min <= lat_min && lat_min <= other.lat_max) ||
	     (other.lat_min <= lat_max && lat_max <= other.lat_max) ||
	     (lat_min <= other.lat_min && other.lat_min <= lat_max) ||
	     (lat_min <= other.lat_max && other.lat_max <= lat_max)))
	    return false;
	
	return true;
	
    } /* END overlaps(LatLonBoundingBox other) */
    
    /**
     * Determine if this bounding box overlaps a scene.
     * <P>
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc instersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     * <P>
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     * <P>
     * @see nsidc.spheres.Scene
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(Scene scene)
    {
	
	return false;
    } /* END overlaps(Scene scene) */
    
    /**
     * Determine if this bounding box overlaps a sperical polygon.
     * <P>
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc intersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     * <P>
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     * <P>
     * @see nsidc.spheres.SphericalPolygon
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(SphericalPolygon spolygon)
    {
	return spolygon.overlaps(this);
    
    } /* END overlaps(SphericalPolygon spolygon) */


    /**
     * Convert the bounding box to a spherical polygon.
     * <P>
     * 
     * @see nsidc.spheres.SphericalPolygon
     * 
     **/

    public SphericalPolygon toSphericalPolygon(int num_points)
    {
	double step = perimeter / (num_points-4);
	Point[] tmp_points = new Point[num_points];
	double lat, lon;
	int i=0;
	
	systemLog("LLBox to Spherical Polygon: ",4);
	// Across the top.
	for(i=0, lon=lon_min; lon <lon_max && i<num_points; lon += step, i++)
	    {tmp_points[i] = new Point(lat_max, lon);
	    }
	
	//Down the right side
	for(lat=lat_max; lat >lat_min && i<num_points; lat -= step, i++)
	    {tmp_points[i] = new Point(lat, lon_max);
	    }
	
	//Across the bottom
	for(lon=lon_max; lon >lon_min && i<num_points; lon -= step, i++)
	    {tmp_points[i] = new Point(lat_min, lon);
	    }
	
	//Up the left side
	for(lat=lat_min; lat <lat_max && i<num_points; lat += step, i++)
	    {tmp_points[i] = new Point(lat, lon_min);
	    }

	//fill up the array
	for(; i<num_points; i++)
	tmp_points[i] = new Point(lat_max, lon_min);
	systemLog("LLBox to Spherical Polygon: DONE",4);
	return new SphericalPolygon(tmp_points);
	
    } /* END toSphericalPolygon(int num_points) */
    
    public String toString(){
	return ("Lat: ("+lat_min+", "+lat_max+") lon: ("+
		lon_min+", "+lon_max+") perimeter: "+perimeter);
    }

    /**
     * Log diagnostic messages
     *
     * @param msg Message to log.
     * @param level Debug sensitivity.
     */
    protected void systemLog(String msg, int level) {
	if(level > logLevel)
	    System.out.println(msg);
    }

} /* END LatLonBoundingBox class */
