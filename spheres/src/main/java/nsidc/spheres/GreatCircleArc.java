package nsidc.spheres;

import java.lang.*;
/*******************************************************************
 * GreatCircleArc class - provides services and information relating  
 * to a great circle arc.
 * <P>
 * A great circle arc is a piece of a great circle.  The sides of spherical  
 * polygons are great circle arcs so we need this class to compare spherical  
 * polygons to other areas on the sphere.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 13-December-2000 swick@nsidc.org 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 * @see nsidc.spheres.Sphere
 * @see nsidc.spheres.SphericalPolygon
 * @see nsidc.spheres.GreatCircle
 *******************************************************************/

public class GreatCircleArc extends GreatCircle
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
     
    /**
     * Boolean to track if the arc crosses a pole or not.
     */
    public boolean crosses_pole = false;
    
    /**
     * Longitude range of the arc.
     */
     public LonRange lon_range;
    
    /**
     * Latitude range of the arc.
     */
     public LatRange lat_range;
    
    /**
     * Length of the arc in radians.
     */
    public double arc_length_rad;
    
    /**
     * Length of the arc in degrees.
     */
    public double arc_length_deg;
  
    /**
     * tmp variables to store interim results.
     */
    private double numerator, denominator;
    
    /**
     *  Tmp variable for cartesian coordinates of intersect points.
     */
    private double g, h, w; 
    
    /**
     *  Counter
     */
    private int i;
    
    /**
     *  Tmp constant for interim results of the intersect calculations
     */
    private double planar_const, sphere_const, scale, rad, firstX, secondX;
    
    /**
     *  
     */
    private double tmp_lat, tmp_lon;
    private Point tmp_pnt;
    
    private final int logLevel = 5;

    /*******************************************************************
     * The constructors set the endpoints and initialize the arc.
     *******************************************************************/
    
    /**
     *  Sets the endpoints and initializes the arc.
     */
    public GreatCircleArc(double given_lat0, double given_lon0, 
		   double given_lat1, double given_lon1)
    {
	arc_point[0] = new Point(given_lat0, given_lon0);
	arc_point[1] = new Point(given_lat1, given_lon1);
		    
	init();
    }

    /**
     *  Sets the endpoints and initializes the arc.
     */
    public GreatCircleArc(double[] given_lat, double[] given_lon)
    {

	//if(given_lat.length < 2 ||given_lon.length < 2)
	//    return;
	
	arc_point[0] = new Point(given_lat[0], given_lon[0]);
	arc_point[1] = new Point(given_lat[1], given_lon[1]);
	
	init();
    }
    
    /**
     *  Sets the endpoints and initializes the arc.
     */
    public GreatCircleArc(Point start, Point end)
    {
	arc_point[0] = new Point(start.lat, start.lon);
	arc_point[1] = new Point(end.lat, end.lon);
	
	init();
    }

    /**
     *  initializes the arc.
     */
    protected void init(){

	super.init();

	if(arc_point[0].lon == arc_point[1].lon) 
	   is_meridian = true;
	   
	if((arc_point[0].lon == arc_point[1].lon + 180.0) ||
	   (arc_point[0].lon + 180.0 == arc_point[1].lon))
	    {
		is_meridian = true;	
		crosses_pole = true;
	    }
	if(!is_meridian)
	    orientArc();

	a = (arc_point[0].y * arc_point[1].z) - (arc_point[1].y * arc_point[0].z);
	b = (arc_point[1].x * arc_point[0].z) - (arc_point[0].x * arc_point[1].z);
	c = (arc_point[0].x * arc_point[1].y) - (arc_point[1].x * arc_point[0].y);
		
	lon_range = new LonRange(arc_point[0].lon, arc_point[1].lon);
	lat_range = new LatRange(arc_point[0].lat, arc_point[1].lat);

	if(!is_meridian && lon_range.contains(inflection_point.lon))
	    lat_range.max = inflection_point.lat;
	
	if(!is_meridian && lon_range.contains(inflection_point.lon - 180.0))
	    lat_range.min = -inflection_point.lat;
	
	if(arc_point[0].lat == arc_point[1].lat &&
	   arc_point[0].lon == arc_point[1].lon)
	    arc_length_rad = 0.0;
	else
	    arc_length_rad = Math.acos((Math.cos(radians(arc_point[0].lat)) * 
					Math.cos(radians(arc_point[0].lon)) * 
					Math.cos(radians(arc_point[1].lat)) * 
					Math.cos(radians(arc_point[1].lon))) +
				       (Math.cos(radians(arc_point[0].lat)) * 
					Math.sin(radians(arc_point[0].lon)) * 
					Math.cos(radians(arc_point[1].lat)) * 
					Math.sin(radians(arc_point[1].lon))) +
				       (Math.sin(radians(arc_point[0].lat)) * 
					Math.sin(radians(arc_point[1].lat))));

	arc_length_deg = degrees(arc_length_rad);
	systemLog("Rad Length: "+arc_length_rad+" is degrees: "+degrees(arc_length_rad), 4); 

    }

    /**
     * The orient method.
     *
     * We always use the shortest arc from point 0 to point 1 and
     * it is convenient to orient that arc from west to east.
     */
    private void orientArc()
    {
	if(Double.isNaN(arc_point[0].lon) || Double.isNaN(arc_point[1].lon))
	    return;

	arc_point[0].lon = normalize(arc_point[0].lon);
	arc_point[1].lon = normalize(arc_point[1].lon);
	
	/*
	 *  Eliminate dateline problems 
	 */
	while(0.0 > arc_point[0].lon || 0.0 > arc_point[1].lon){
	    arc_point[0].lon += 360.0;
	    arc_point[1].lon += 360.0;
	}
	
	/*
	 * if W-E is more than 180 degrees switch points
	 */
	if(arc_point[0].lon < arc_point[1].lon && 
	   (arc_point[1].lon - arc_point[0].lon) > 180.0)
	    {
		tmp_pnt = arc_point[0];
		arc_point[0] = arc_point[1];
		arc_point[1] = tmp_pnt;
		
	    }

	/* 
	 * if E->W is greater than 180 then W->E is less than 180
	 */
	if(arc_point[0].lon > arc_point[1].lon && arc_point[0].lon-arc_point[1].lon > 180.0){
	    arc_point[1].lon += 360.0;
	}

	/* 
	 * if E->W is less than 180 switch points to make it W->E
	 */
	if(arc_point[0].lon > arc_point[1].lon && arc_point[0].lon-arc_point[1].lon < 180.0){
	    tmp_pnt = arc_point[0];
	    arc_point[0] = arc_point[1];
	    arc_point[1] = tmp_pnt;
	}
	
	/*
	 *  Check the results
	 */
	if(arc_point[0].lon < arc_point[1].lon && (arc_point[1].lon-arc_point[0].lon) <= 180.0)
	    systemLog("Orientation is W->E along shortest arc.",4); 
	else
	    systemLog("Orientation is WONKY: ("
		      +arc_point[0].lat+", "+arc_point[0].lon+") -> ("
		      +arc_point[1].lat+", "+arc_point[1].lon+")",1);
	
	arc_point[0].lon = normalize(arc_point[0].lon);
	arc_point[1].lon = normalize(arc_point[1].lon);
	
    } /* END orientArc() */

    /**********************************************************************
     * The intersect methods.
     **********************************************************************/

    /**
     * Determine if this arc intersects another.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * Then we convert back to spherical and see what's what.
     * 
     * @return true    if the two arcs intersect.<br>
     *         false   if the two arcs do not instersect.
     *
     * @see nsidc.spheres.Scene
     **/

    public boolean intersectsArc(GreatCircleArc other)
    {
	/*
	 * First check the trivial case.
	 */
	if(null == other)
	    return false;

	/* 
	 * Then check if it's even possible.
	 */
	if(!this.lon_range.overlaps(other.lon_range)){
	    systemLog("No Lon overlap: "+this.lon_range.toString()+" vs "+other.lon_range.toString(), 4);
	    return false;
	}
	
	if(!this.lat_range.overlaps(other.lat_range)){
	   
	    systemLog("--->No Lat overlap: "+this.lat_range.toString()+" vs "+other.lat_range.toString(), 4);
	    return false;
	}	
	    
	systemLog("possible lon:  "+this.lon_range.toString()+" vs "+other.lon_range.toString()+
		  "\npossible lat:  "+this.lat_range.toString()+" vs "+other.lat_range.toString(),4);
	
	/*
	 * Then the special cases.
	 * If both great circles are meridians they cross at the poles.
	 *
	 * So if both arcs cross the pole the arcs cross.
	 */
	if(this.is_meridian && other.is_meridian)
	    {
		systemLog("meridians",4);
		if((this.arc_point[0].lon == this.arc_point[1].lon + 180.0 ||
		    this.arc_point[0].lon + 180.0 == this.arc_point[1].lon) &&
		   (other.arc_point[0].lon == other.arc_point[1].lon + 180.0 ||
		    other.arc_point[0].lon + 180.0 == other.arc_point[1].lon))
		    {
			if((this.arc_point[0].lat >= 0.0 && other.arc_point[0].lat >= 0.0) ||
			   (this.arc_point[0].lat <= 0.0 && other.arc_point[0].lat <= 0.0))
			    {systemLog("meridians",4);
			    return true;}
			else
			    return false;
		    }
		else
		    return false;
	    }

	/*
	 * If one arc is a meridian check that the intersect point is within the 
	 * lat range for that arc.
	 * 
	 * If neither arc is part of a meridian the great circles the two arcs
	 * are part of cross every meridian exactly once.  Moreover the two
	 * great circles cross each other exactly twice.  Find those crossing 
	 * points and check if the lon of the crossing is within the lon range 
	 * of both arcs and voila!
	 */
	
	/*
	 * find the intersect points.
	 *
	 * All great circles cross - this method only fails if there is 
	 * no other circle, which we already checked for - but something 
	 * odd might happen.
	 */
	systemLog("intersects",4);
	if (!this.intersectsGreatCircle(other))
	    {
		systemLog("NO INTERSECTS!  how is that even possible?", 1);
		return false;
	    }

	/*
	 * Check the lat range of the meridian.  This isn't actually correct because the arc
	 * could pass through the inflection point, but it'll do for now.  I still have to 
	 * figure out how to determine the inflection point and what to do about it once I've 
	 * found it.
	 */
	if(this.is_meridian && 
	   java.lang.Math.min(this.arc_point[0].lat,this.arc_point[1].lat) <=  intersect_point[0].lat &&
	   java.lang.Math.max(this.arc_point[0].lat,this.arc_point[1].lat) >=  intersect_point[0].lat)
	   {    		    
		systemLog("This Meridian! ("+intersect_point[0].lat+", "+intersect_point[0].lon+")", 1);
		return true;
	    }
	
	if(this.is_meridian && 
	   java.lang.Math.min(this.arc_point[0].lat,this.arc_point[1].lat) <=  intersect_point[1].lat &&
	   java.lang.Math.max(this.arc_point[0].lat,this.arc_point[1].lat) >=  intersect_point[1].lat)
	   {    		    
		systemLog("This Meridian! ("+intersect_point[1].lat+", "+intersect_point[1].lon+")", 1);
		return true;
	    }

	if(other.is_meridian && 
	   java.lang.Math.min(other.arc_point[0].lat,other.arc_point[1].lat) <=  intersect_point[0].lat &&
	   java.lang.Math.max(other.arc_point[0].lat,other.arc_point[1].lat) >=  intersect_point[0].lat)
	   {    		    
		systemLog("Other Meridian! ("+intersect_point[0].lat+", "+intersect_point[0].lon+")", 1);
		return true;
	    }
	
	if(other.is_meridian && 
	   java.lang.Math.min(other.arc_point[0].lat,other.arc_point[1].lat) <=  intersect_point[1].lat &&
	   java.lang.Math.max(other.arc_point[0].lat,other.arc_point[1].lat) >=  intersect_point[1].lat)
	   {    		    
		systemLog("Other Meridian! ("+intersect_point[1].lat+", "+intersect_point[1].lon+")", 1);
		return true;
	    }

	if(this.is_meridian || other.is_meridian)
	    {
		systemLog("NO Meridian : (this:"+this.is_meridian+", other:"+other.is_meridian+
			  ") \nthis Lats("+this.arc_point[0].lat+", "+this.arc_point[1].lat+
			  ") \nother Lats("+other.arc_point[0].lat+", "+other.arc_point[1].lat+
			  ") \nthis Lons("+this.lon_range.min+", "+this.lon_range.max+
			  ") \nother Lons("+other.lon_range.min+", "+other.lon_range.max+
			  ") \nintersects(("+intersect_point[0].lat+", "+intersect_point[0].lon+"), ("+
			  intersect_point[1].lat+", "+intersect_point[1].lon+")");
		return false;
	    }

	/*
	 * Otherwise check if the intersects are in the lon range of both arcs.
	 * The intersects are on both great circles, so if an intersect
	 * is in the same region as both arcs it is on both arcs.
	 */
	systemLog("range check",4);
	if(this.lon_range.contains(intersect_point[0].lon) &&
	   other.lon_range.contains(intersect_point[0].lon))
	    {    		    
		systemLog("INTERSECT! ("+intersect_point[0].lat+", "+intersect_point[0].lon+")", 1);
		return true;
	    }
	
	if(this.lon_range.contains(intersect_point[1].lon) &&
	   other.lon_range.contains(intersect_point[1].lon))
	    {
		
		systemLog("INTERSECT! ("+intersect_point[1].lat+", "+intersect_point[1].lon+")", 1);
		return true;
	    }
	/*
	 * tried everything and failed.
	 */
	systemLog("NO intersect : (this:"+this.is_meridian+", other:"+other.is_meridian+
			  ") \nthis Lons("+this.lon_range.min+", "+this.lon_range.max+
			  ") \nother Lons("+other.lon_range.min+", "+other.lon_range.max+
			  ") \nintersect Lons(("+intersect_point[0].lon+", "+intersect_point[1].lon+")",4);
	return false;
	
    } /* END intersectsArc */
    
    
    /**
     * Determine if this arc intersects a segment of a parallel.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * Then we convert back to spherical and see what's what.
     *
     * @param seg_lat The latitude of the segment.
     * @param min_lon The minimun longitude of the segment.
     * @param max_lon The maximum longitude of the segment.
     *
     * @return true    if the two arcs intersect.<br>
     *         false   if the two arcs do not instersect.
     *
     * @see nsidc.spheres.LatLonBoundingBox
     */
    public boolean intersectsLatSeg(double seg_lat, double min_lon, double max_lon)
    {
	/**
	 * First check if it's even possible.
	 */
	
	if(!this.lon_range.overlaps(new LonRange(min_lon, max_lon)))
	    return false;
	if(Math.abs(seg_lat) > inflection_point.lat)
	    return false;
	/*
	 * Find the intersects.
	 *
	 * This method only fails if the lat is out of range, which we 
	 * already checked for - but something odd might happen.
	 */

	if (!this.intersectsLatitude(seg_lat))
	    {
		systemLog("NO INTERSECTS!  How is that even possible?", 1);
		return false;
	    } 

	if(this.lon_range.contains(intersect_point[0].lon) &&
	   min_lon <= intersect_point[0].lon && intersect_point[0].lon <= max_lon)
	    return true;
	
	if(this.lon_range.contains(intersect_point[1].lon) &&
	   min_lon <= intersect_point[1].lon && intersect_point[1].lon <= max_lon)
	    return true;
	
	return false;
    }


    /**
     * Determine if this arc intersects a small circle arc.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * Then we convert back to spherical and see what's what. This method is not  
     * actually implemented yet and always returns false.
     *
     * @param small_circle_arc The small circle arc of interest.
     *
     * @return true    if the two arcs intersect.<br>
     *         false   if the two arcs do not instersect.
     *
     * @see nsidc.spheres.SmallCircle
     * @see nsidc.spheres.SmallCircleArc
     * @see nsidc.spheres.Scene
     */
    public boolean intersectsSCArc(SmallCircleArc small_circle_arc)
    { 

	return false;

    } /* END intersectsSCArc(small_circle_arc)  */


    /**
     * Find the center of the arc
     * <P>
     * Equations taken from the Aviation Formulary by Ed Williams.
     *
     * @param none.
     *
     * @return Point center_point The point at the center of the Arc.
     *
     */
    public Point center()
    {	
	double A, B, f;
	double x, y, z, lat, lon;
	Point center_point;
	
	A=Math.sin((0.5)*arc_length_rad)/Math.sin(arc_length_rad);
	B=Math.sin(0.5*arc_length_rad)/Math.sin(arc_length_rad);
	systemLog("fraction: 0.5"+" A: "+A+" B: "+B, 3);
	x = A*Math.cos(radians(arc_point[0].lat))*Math.cos(radians(arc_point[0].lon)) +  
	    B*Math.cos(radians(arc_point[1].lat))*Math.cos(radians(arc_point[1].lon));
	y = A*Math.cos(radians(arc_point[0].lat))*Math.sin(radians(arc_point[0].lon)) +  
	    B*Math.cos(radians(arc_point[1].lat))*Math.sin(radians(arc_point[1].lon));
	z = A*Math.sin(radians(arc_point[0].lat)) + B*Math.sin(radians(arc_point[1].lat));
	
	lat=Math.atan2(z, Math.sqrt(x*x+y*y));
	lon=Math.atan2(y,x);
	center_point = new Point(degrees(lat), degrees(lon));
	systemLog(i+": "+center_point.toString(), 3);
	
	x= A*arc_point[0].x + B*arc_point[1].x;
	y= A*arc_point[0].y + B*arc_point[1].y;
	z= A*arc_point[0].z + B*arc_point[1].z;
	center_point = new Point(x,y,z);
	systemLog(i+": "+center_point.toString(), 3);
	
	return center_point;
    }

    /**
     * Densify the arc
     * <P>
     * Equations taken from the Aviation Formulary by Ed Williams.
     *
     * @param distance_rad The maximum distance in radians between points in the densified arc.
     *
     * @return Point[] dense_point A denser point set defining the same arc.
     *
     */
    public Point[] densify(double distance_rad)
    { 
	int num_points = (int)(arc_length_rad/distance_rad) + 2;
	Point[] dense_point = new Point[num_points];
	
	double fraction = distance_rad / arc_length_rad;
	double A, B, f;
	double x, y, z, lat, lon;
	
	if(distance_rad >= arc_length_rad) return arc_point;
	systemLog("dist: "+distance_rad+" / arc: "+arc_length_rad+" = fraction: "+fraction, 3);
	dense_point[0] = new Point(arc_point[0].lat, arc_point[0].lon);
	systemLog("FIRST of "+num_points+" "+dense_point[0].toString(), 3);
	for(i=1; i<num_points-1; i++)
	    {
		f = fraction*i;
		A=Math.sin((1.0-f)*arc_length_rad)/Math.sin(arc_length_rad);
		B=Math.sin(f*arc_length_rad)/Math.sin(arc_length_rad);
		systemLog("fraction: "+f+" A: "+A+" B: "+B, 3);
		x = A*Math.cos(radians(arc_point[0].lat))*Math.cos(radians(arc_point[0].lon)) +  
		    B*Math.cos(radians(arc_point[1].lat))*Math.cos(radians(arc_point[1].lon));
		y = A*Math.cos(radians(arc_point[0].lat))*Math.sin(radians(arc_point[0].lon)) +  
		    B*Math.cos(radians(arc_point[1].lat))*Math.sin(radians(arc_point[1].lon));
		z = A*Math.sin(radians(arc_point[0].lat)) + B*Math.sin(radians(arc_point[1].lat));
			
		lat=Math.atan2(z, Math.sqrt(x*x+y*y));
		lon=Math.atan2(y,x);
		dense_point[i] = new Point(degrees(lat), degrees(lon));
		systemLog(i+": "+dense_point[i].toString(), 3);

		x= A*arc_point[0].x + B*arc_point[1].x;
		y= A*arc_point[0].y + B*arc_point[1].y;
		z= A*arc_point[0].z + B*arc_point[1].z;
		dense_point[i] = new Point(x,y,z);
		systemLog(i+": "+dense_point[i].toString(), 3);


	    }
	dense_point[i] = new Point(arc_point[1].lat, arc_point[1].lon);
	systemLog("LAST "+dense_point[i].toString(), 3);
	return dense_point;
    }
    
    protected void systemLog(String msg, int level) {
	if(level > logLevel)
	    System.out.println(msg);
    }
    

} /* END GreatCircleArc class */








