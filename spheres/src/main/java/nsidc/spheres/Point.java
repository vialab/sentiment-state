package nsidc.spheres;

import java.lang.*;
/******************************************************************** 
 * Point class - provides services and information relating 
 * to a point on a sphere.
 * <P>
 * In this package we do a lot of switching between spherical coordinates and cartesian coordinates, so it is convenient to define a point class that includs both coordinate systems.
 *<P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 19-December-2000 swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 *******************************************************************/

public class Point extends Sphere
{
 /*******************************************************************
  * instance variables
  *******************************************************************/
    
    /** 
     * Spherical coordinate of the point.
     */
    public double lat, lon;

    /**
     * Cartesian coordinate of the point.
     */
    public double x, y, z;

    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    /**
     * Known spherical coordinates.
     */
    public Point(double given_lat, double given_lon)
    {
	lat = given_lat;
	lon = given_lon;
	x = radius * Math.cos(radians(lon)) * Math.cos(radians(lat));
	y = radius * Math.sin(radians(lon)) * Math.cos(radians(lat));
	z = radius * Math.sin(radians(lat));
    }

     
    /**
     * Known cartesian coordinates
     * <P>
     * This method does not check that the point is actually 
     * on the sphere. The point is, after all, on some sphere. 
     */
    public Point(double given_x, double given_y, double given_z)
    {
	x = given_x;
	y = given_y;
	z = given_z;
	//lat = 90.0 - degrees(Math.asin(z/radius));
	lat = degrees(Math.asin(z/radius));
	lon = degrees(Math.atan2(y, x));
   }

    /**
     * Use a different radius.
     */
    public void setRadius(double given_radius)
    {
	super.setRadius(given_radius);
	x = radius * Math.cos(radians(lon)) * Math.cos(radians(lat));
	y = radius * Math.sin(radians(lon)) * Math.cos(radians(lat));
	z = radius * Math.sin(radians(lat));
    }

    /**
     * Reset the point from known spherical coordinates.
     */
    public void reset(double given_lat, double given_lon)
    {
	lat = given_lat;
	lon = given_lon;
	x = radius * Math.cos(radians(lon)) * Math.cos(radians(lat));
	y = radius * Math.sin(radians(lon)) * Math.cos(radians(lat));
	z = radius * Math.sin(radians(lat));
    }
    
    /**
     * Reset the point from known cartesian coordinates.
     * <P>
     * This method does not check that the point is actually 
     * on the sphere.  The point is, after all, on some sphere. 
     */
    public void reset(double given_x, double given_y, double given_z)
    {
	x = given_x;
	y = given_y;
	z = given_z;
	lat = degrees(Math.asin(z/radius));
	lon = degrees(Math.atan2(y, x));
   }

    public String toString()
    {
	return new String("Point: lat="+lat+" lon="+lon+" x="+x+" y="+y+" z="+z);
    }

} /* END Point class */
