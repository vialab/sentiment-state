package nsidc.spheres;

import java.lang.*;
/********************************************************************
 * SphericalPolygon class - provide services and information relating to a spherical polygon.
 * <P>
 * A spherical polygon is a polygon on a sphere with corner points connected
 * by great circle arcs.
 * <P>
 * This class assumes a spherical earth.
 * <P>
 * This class connects corner points with the shortest great circle arc and 
 * makes no assumptions about convexity.  A poorly defined spherical polygon 
 * may well have sides that cross.
 * 
 * @author
 * R. Swick 14-December-2000 swick@nsidc.org 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder CO <BR>
 * Copyright 2003 University of Colorado.
 **************************************************************************/
 
public class SphericalPolygon extends Sphere
{
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     * Array for polygon corner point locations.
     */
    public Point[] corner_point; 

    /**
     * Longitude range of the polygon 
     */
    public LonRange lon_range;
    
    /**
     * Latitude range of the arcs that make up the polygon
     */
    public LatRange lat_range;
    //public double max_lat, min_lat;
    
    /**
     * Perimeter of the polygon in radians.
     */
    public double perimeter_rad;
    
    /**
     * Perimeter of the polygon in degrees.
     */
    public double perimeter_deg;
  
    /**
     *  A point (probably) outside the polygon.
     */
    public Point external_point;
  
    /**
     * Counters
     */
    private int i,j,k;
    
    /**
     *  Tmp variable for holding a single arc of the polygon.
     */
    private GreatCircleArc arc, other_arc;

    /* 5 = 0ff, 0 = full on */
    private final int logLevel = 4;

    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    /**
     * Given lat and lon arrays.
     */
    public SphericalPolygon(double[] given_lat, double[] given_lon)
    {
	
	/* assert(given_lat.length == given_lon.length);
	 */
	if(given_lat.length != given_lon.length)
	    systemLog("Lat/Lon mismatch!  THIS IS BAD!",1);

	corner_point = new Point[given_lat.length];
	
	for(i=0; i<given_lat.length; i++)
	    {
		corner_point[i] = new Point(given_lat[i], given_lon[i]);
	    } 
	getRanges();
	//getLonRange();
	//max_lat = rangeMax(given_lat);
	//min_lat = rangeMin(given_lat);
	//getPerimeter();
    }

    /**
     * Given an array of points.
     */
    public SphericalPolygon(Point[] given_points)
    {	
	systemLog("New SP "+given_points.length, 4);
	corner_point = new Point[given_points.length];
	
	for(i=0; i<given_points.length; i++)
	    corner_point[i] = new Point(given_points[i].lat, given_points[i].lon);
	
	getRanges();
	//getLonRange();
	//max_lat = rangeMax(lat_array);
	//min_lat = rangeMin(lat_array);
	//getPerimeter();
    }


    /*******************************************************************
     * getLonRange method.   ---> Deprecated <---
     *******************************************************************/
    /**
     * Determine the lon Range of this polygon.
     * <P>
     * This method just melds the lon ranges of all the arcs that 
     * make up the polygon.
     */
    /****************************************VOID***************
    private void getLonRange()
    {
	lon_range = ((new GreatCircleArc(corner_point[corner_point.length-1],
					      corner_point[0])).lon_range);
	
	for(i=0; i<corner_point.length-1; i++)
	    {
		lon_range.meldRange((new GreatCircleArc(corner_point[i],
					      corner_point[i+1])).lon_range);
		systemLog("Min: "+lon_range.min+"Max: "+lon_range.max,4);
	    }

    }
    ****************************************DELETE*****************/



    /*******************************************************************
     * getRanges method.
     *******************************************************************/
    /**
     * Determine the lon range, lat range, and perimeter of this polygon.
     * <P>
     * This method just adds up the arc distance of all the arcs that 
     * make up the polygon, and melds the ranges.
     */
    private void getRanges()
    {
	lon_range = ((new GreatCircleArc(corner_point[0],
					 corner_point[1])).lon_range);
	lat_range = new LatRange(corner_point[0].lat, corner_point[1].lat);
	perimeter_rad = 0;

	for(i=0; i<corner_point.length-1; i++){
	    arc = new GreatCircleArc(corner_point[i],corner_point[i+1]);
	    perimeter_rad += arc.arc_length_rad;
	    
	    if(!lat_range.meldRange(arc.lat_range))
		systemLog("SP: ERROR - Unable to meld lat range for: "+arc.toString(), 1);
	
	    if(!lon_range.meldRange(arc.lon_range))
		systemLog("SP: ERROR - Unable to meld lon range for: "+arc.toString(), 1);

	    systemLog(i+": arc length = "+arc.arc_length_rad+" total: "+perimeter_rad+" lon: "+lon_range.toString()+" arc: "+arc.lon_range.toString(), 3);
	}
	perimeter_deg = degrees(perimeter_rad);

	external_point = guessExternalPoint();
    }


    /*******************************************************************
     * guessExternalPoint method.
     *******************************************************************/
    /**
     * Guess a point that is outside the polygon.  
     * <P>
     * This method has to assume that the polygon is somewhat reasonable.
     * <P>
     * If the lon range is the full 360 we're guessing the polygon includes 
     * a pole.  We're also guessing the included pole is the pole nearest the edges, 
     * so the other pole and the surrounding area would be excluded. 
     * <P>
     * For most purposes those are reasonable guesses, but if you are using a polygon
     * with an unusual shape you should set the external point yourself. If your 
     * polygon covers more than a hemisphere, or won't fit within a hemisphere, it 
     * counts as "unusual".
     * <P>
     * If the lon range is not the full 360 the task is much easier and the
     * guess should be correct.
     * 
     */
    public Point guessExternalPoint()
    {
	double external_lon, external_lat;
	
	if(lon_range.max - lon_range.min >=360.0){
	    external_lon = 90.0;
	    if(lat_range.max < 0.0)
		 external_lat = 45.0;
	    else if(lat_range.min > 0.0)
		external_lat = -45.0;
	    else if(Math.abs(lat_range.max) >= Math.abs(lat_range.min))
		external_lat = lat_range.min - ((90.0 + lat_range.min)/2.0);
	    else
		external_lat = lat_range.max + ((90.0 - lat_range.max)/2.0);

	}else{
	     external_lon = normalize(((lon_range.max+lon_range.min)/2.0) -180.0);
	     
	     if(Math.abs(lat_range.max) >= Math.abs(lat_range.min))
		 external_lat = lat_range.min + ((-90.0 - lat_range.min)/2.0);
	     else
		 external_lat = lat_range.max + ((90.0 - lat_range.max)/2.0);
	}

	    return new Point(external_lat, external_lon);

    }  /* END private void guessExternalPoint() */
    
    /**
     *  Return the point currently being used as the "external" point.  
     */
    public Point getExternalPoint()
    {
	return external_point;
    }
    
    /**
     *  Set the point that is to be used as the "external" point.  
     */
    public void setExternalPoint(Point given_point)
    {
	external_point = new Point(given_point.lat, given_point.lon);
    }

    

    /*******************************************************************
     * Contains methods.
     *******************************************************************/
    
    /**
     * Determine if this polygon contains a given point.
     * <P>
     * This method creates a great circle arc between the point of 
     * interest and the "known" external point and counts how many 
     * times that arc crosses the edges of the polygon. Iff the arc 
     * crosses an odd number of edges the point of interest must be 
     * inside the polygon.  
     * <P>
     * The corner points are assumed to be in some order.  This should 
     * work with both convex and concave polygons, but make sure the 
     * external point actually is external.
     * <P>
     * For corner points and points on the edge of the polygon the behavior
     * is undefined. It "should" be that corner points are "outside" (two edge
     * crossings) and edge points are "inside" (one edge crossing) but it's 
     * really down to the precision of the math processor and which way the 
     * rounding goes. 
     *
     * @param given_point Point of interest.
     *
     * @returns true  If the point is inside the polygon.
     *          false If the point is outside the polygon or the algorithm can't figure it out.
     */
    
    public boolean contains(Point given_point)
    { 
	GreatCircleArc arc;
	int count = 0;
	
	arc = new GreatCircleArc(given_point, external_point);
	
	for(i=0; i<corner_point.length-1; i++){
	    if(arc.intersectsArc(new GreatCircleArc(corner_point[i],
						    corner_point[i+1])))
		count++;
	}
	if(1 == (count % 2))
	    return true;
	return false;
	
    }  /* END contains(Point) */
    

    /**
     * Determine if this polygon contains a given point using STP.
     * <P>
     * This method checks the scalar triple product of the point 
     * and consective corner points all the way around the polygon. 
     * If the point is on the same side of every edge, the point 
     * must be inside the polygon.
     * <P>
     * The corner points are assumed to be in some order
     * and the polygon has to be convex for this to work.  
     * 
     * @param given_point Point of interest.
     *
     * @returns true  If the point is inside the polygon.
     *          false If the point is outside the polygon or the algorithm can't figure it out.
     */
    public boolean containsSTP(Point given_point)
    {
	int test_result;
	boolean left = false, right = false;
	
	test_result = scalarTripleProductTest(given_point, 
					      corner_point[corner_point.length-1],
					      corner_point[0]);
	if(test_result < 0)
	    right = true;
	else if (test_result > 0)
	    left = true;
	
	for(i=0; i<corner_point.length-1; i++)
	    {
		test_result = scalarTripleProductTest(given_point, 
						      corner_point[i],
						      corner_point[i+1]);
		if(test_result < 0)
		    right = true;
		else if (test_result > 0)
		    left = true;

		if(left && right)
		    return false;
 }
	return true;
	
    } /* END containsSTP(Point) */



    /*******************************************************************
     * Overlap methods for other spherical polygons, lat/lon bounding 
     * boxes, and scenes.
     *******************************************************************/
    
    /**
     * Determine if this polygon overlaps another.
     * 
     * After determining if it's even possible, and we don't have the 
     * trivial case where one polygon is entirely inside the other, 
     * this method checks for arc instersections between the sides of 
     * the two polygons.  If any arcs intersect the polygons overlap.
     *
     * @return true if the polygons overlap.<br>
     *         false if the polygons do not overlap.
     *
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(SphericalPolygon other)
    {
	systemLog("Begin overlaps SP",3);
	/* 
	 * First check if it's even possible.
	 */
	systemLog("Lon: Min: "+this.lon_range.min+" Max: "+this.lon_range.max+
		      " vs. Min: "+other.lon_range.min+" Max: "+other.lon_range.max,3);

	if(!this.lon_range.overlaps(other.lon_range)){
	    systemLog("SP overlap NOT POSSIBLE! - Lon Range",3);
	    return false;
	}
	/**** WOOOP WOOOP WOOOP ****
	if(true) return true;
	*********/
	
	/*
	 * Then check a single point from each to see if it is inside the other.
	 * If either polygon is wholly inside the other this is the only way to 
	 * catch that.  And we could get lucky even if that's not the case.
	 */
	systemLog("contains",4);	
	if(this.contains(other.corner_point[0])){
	    systemLog("SP overlap! - containment1",3);
	    return true;
	}
	systemLog("contains",4);	
	if(other.contains(this.corner_point[0])){
	    systemLog("SP overlap! - containment2",3);
	    return true;
	}
	/*
	 * If the two polygons overlap then some pair of sides intersect.
	 * Check them all.
	 */
	
	systemLog("arc compares",4);	
	
	for(i=1; i<=corner_point.length; i++){
	    //System.out.print(".");	
	    //System.out.print(i+": ");	
	 
	    arc = new GreatCircleArc(corner_point[i-1],
				     corner_point[i%corner_point.length]);
	    for(j=1; j<=other.corner_point.length; j++){
		//System.out.print(j+" ");
		System.gc();
		other_arc = new GreatCircleArc(other.corner_point[j-1],
					       other.corner_point[j%other.corner_point.length]);
		//System.out.println("SP test - ("+(i-1)+", "+(i%corner_point.length)+
		//		   ") : ("+(j-1)+", "+(j%other.corner_point.length)+")");
		//System.out.print(i+":"+j+" ");
		if(arc.intersectsArc(other_arc)){
		    systemLog("SP overlap! - intersect " +i+ ":" +j 
				       /***
					   +" (("+
					   arc.arc_point[0].lat+", "+arc.arc_point[0].lon+"), ("+
					   arc.arc_point[1].lat+", "+arc.arc_point[1].lon+")) and (("+ 
					   other_arc.arc_point[0].lat+", "+other_arc.arc_point[0].lon+"), ("+
					   other_arc.arc_point[1].lat+", "+other_arc.arc_point[1].lon
					   +")) at ("+ 
					   arc.intersect_point[0].lat+", "+arc.intersect_point[0].lon+") or ("+
					   arc.intersect_point[1].lat+", "+arc.intersect_point[1].lon+")"
				       ***/
				       ,4);
				       
		    return true;
		}
	    }
	}
	systemLog("SP overlap NOT! - NO Intersects "+
			   "\nLat: Min: "+this.lat_range.min+" Max: "+this.lat_range.max+
			   " vs. Min: "+other.lat_range.min+" Max: "+other.lat_range.max+
			   "\nLon: Min: "+this.lon_range.min+" Max: "+this.lon_range.max+
			   " vs. Min: "+other.lon_range.min+" Max: "+other.lon_range.max, 4);
	return false;
	
    } /* END overlaps(SphericalPolygon other) */

    /**
     * Determine if this polygon overlaps a lat/lon bounding box.
     * <P>
     * After determining if it's even possible, and we don't have the 
     * trivial case where one area is entirely inside the other, 
     * this method checks for arc intersections between the sides of 
     * the two areas.  If any arcs intersect the areas overlap.
     * <P>
     * @return true if the areas overlap.<br>
     *         false if the areas do not overlap.
     * <P>
     * @see nsidc.spheres.LatLonBoundingBox
     * @see nsidc.spheres.GreatCircleArc
     **/
    public boolean overlaps(LatLonBoundingBox box)
    {
	/*
	 * First check a single point from each to see if it is inside the other.
	 * If either area is wholly inside the other this is the only way to 
	 * catch that.  And we could get lucky even if that's not the case.
	 */
	
	if(this.contains(new Point(box.lat_max, box.lon_max)))
	    return true;
	if(box.contains(this.corner_point[0]))
	    return true;

	/*
	 * If the two areas overlap then some pair of sides intersect.
	 * Check them all. 
	 */

	for(i=1; i<=corner_point.length; i++){
	    arc = new GreatCircleArc(corner_point[i-1],
				     corner_point[i%corner_point.length]);	

	    if(arc.intersectsLatSeg(box.lat_max, box.lon_min, box.lon_max))
		return true;
	    if(arc.intersectsLatSeg(box.lat_min, box.lon_min, box.lon_max))
		return true;
	    
	    if(arc.intersectsArc(new GreatCircleArc(box.lat_min, box.lon_min, 
						    box.lat_max, box.lon_min)))
		return true;
	    if(arc.intersectsArc(new GreatCircleArc(box.lat_min, box.lon_max, 
						    box.lat_max, box.lon_max)))
		return true;
	}
	return false;

    } /* END overlaps(LatLonBoundingBox box) */
	      
    /**
     * Determine if this polygon overlaps a scene.
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
	//System.out.println("overlaps Scene");
	System.gc();
	//System.out.println(this.toString()+" vs. "+scene.toString());
	
	/* really ought to do it properly - for now this a good guess */
	return (this.overlaps(scene.toSphericalPolygon(50)));
	
    } /* END overlaps(Scene scene) */


    /**
     * Densify the polygon
     * <P>
     * Densify each arc of the spherical polygon to 
     * <P>
     * @param distance_rad The maximum distance in radians between points in the densified polygon.
     * <P>
     * @return SphericalPolygon dense_spherical_polygon A denser spherical polygon defining the same area.
     *
     */ 

    public SphericalPolygon densify(double distance_rad)
    { 
	int num_points = (int)(perimeter_rad/distance_rad) + corner_point.length;
	Point[] dense_point = new Point[num_points];
	Point[] arc_point = new Point[num_points];
	GreatCircleArc arc;
	
	double fraction;
	double A, B, f;
	double x, y, z;
	int num_arc_points = 0;
	
	/* Do nothing if this polygon is already dense enough */
	if(perimeter_rad<distance_rad)
	    return this;

	/***************
	 * This is a good idea - but doesn't actually work.
	 * Arcs are oriented W->E which means a lot of these points 
	 * end up in the wrong order.
	 ***

	for(i=0, j=0; j<corner_point.length-1; j++){
	    arc = new GreatCircleArc(corner_point[j], corner_point[j+1]);
	    arc_point = arc.densify(distance_rad);
	    for(k=0; k<arc_point.length-2; k++, i++){
		dense_point[i] = new Point(arc_point[k].lat, arc_point[k].lon);
		
	    } 
	    systemLog(i+": "+i+" length: "+num_points, 3);
	    dense_point[i] = new Point(arc_point[k].lat, arc_point[k].lon);
	}
	systemLog(i+": "+i+" length: "+num_points, 3);
	arc_point = new Point[i];
	
	for(i=0; i<arc_point.length; i++)
	    arc_point[i] = dense_point[i];
	    

	    ******
	    * This loop preserves the order of the points, at the expense of having to repeat
	    * a lot of the code in the arc densifier.  As a result I'm not sure if the arc 
	    * densifier is even worth having anymore.  But might as well keep it.
	    ********************/

	for(i=0, j=0; j<corner_point.length-1; j++){
	    arc = new GreatCircleArc(corner_point[j], corner_point[j+1]);
	    
	    dense_point[i] = corner_point[j];
	    systemLog("CORNER "+i+": "+dense_point[i].toString(), 3);
	    i++;
	    if(distance_rad >= arc.arc_length_rad){
		systemLog("Too small --- "+distance_rad+" > "+arc.arc_length_rad+" SKIP.",3);
		continue;
	    }
	    
	    fraction = distance_rad / arc.arc_length_rad;
	    num_arc_points = (int)((arc.arc_length_rad/distance_rad) + 2.5);
	    f=fraction;
	    for(k=1, f=fraction; f<1.0; k++,f+=fraction, i++){
		//f = fraction*k;
		A=Math.sin((1.0-f)*arc.arc_length_rad)/Math.sin(arc.arc_length_rad);
		B=Math.sin(f*arc.arc_length_rad)/Math.sin(arc.arc_length_rad);
		systemLog("fraction: "+f+" A: "+A+" B: "+B, 3);
		
		x= A*corner_point[j].x + B*corner_point[j+1].x;
		y= A*corner_point[j].y + B*corner_point[j+1].y;
		z= A*corner_point[j].z + B*corner_point[j+1].z;

		dense_point[i] = new Point(x,y,z);
		systemLog(i+": "+dense_point[i].toString(), 3);
		
	    }
	}
	dense_point[i] = corner_point[j];
	systemLog("LAST "+i+" of "+num_points+": "+dense_point[i].toString(), 3);
	arc_point = new Point[i+1];
	
	for(i=0; i<arc_point.length; i++)
	    arc_point[i] = dense_point[i];
	systemLog("There are "+arc_point.length+" points.", 3); 
	return new SphericalPolygon(arc_point);
    }
    

    public String toString()
    {
	String string = new String("SP[Lat("+lat_range.min+", "+lat_range.max+
				   ") Lon("+lon_range.min+", "+lon_range.max+")]\n");
	for(i=0; i<corner_point.length; i++)
	    {
		string = string + corner_point[i].toString()+ "\n";
	    }
	string = string + lat_range.toString();
	string = string + lon_range.toString();
	return(string);
    }

    public double rangeMax(double[] array)
    {
	double max = java.lang.Double.NEGATIVE_INFINITY;
	/************
	 for(i=0; i<array.length; i++)
	    if(!Double.isNaN(array[i]))
		{
		   max = array[i];
		   break;
		}
	****************/
	for(i=0; i<array.length; i++)
	    if(!Double.isNaN(array[i]))
		max = java.lang.Math.max(max, array[i]);
	return max;
    }

    public double rangeMin(double[] array)
    {
	double min = java.lang.Double.POSITIVE_INFINITY;
	/********************
	for(i=0; i<array.length; i++)
	    if(!Double.isNaN(array[i]))
		{
		    min = array[i];
		    break;
		}
	**************/
	for(i=0; i<array.length; i++)
	    if(!Double.isNaN(array[i]))
		min = java.lang.Math.min(min, array[i]);
	return min;
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

} /* END SphericalPolygon class */


