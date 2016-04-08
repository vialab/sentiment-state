package nsidc.spheres;

import java.lang.*;
/******************************************************************** 
 * Sphere class - provides services and information relating 
 * to a sphere.
 * <P>
 * This is the base object of the spheres package.  It defines the 
 * working sphere and several ustility functions the rest of the package uses.
 *<P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 19-December-2000 swick@nsidc.org 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 *******************************************************************/

public class Sphere
{
 /*******************************************************************
  * instance variables
  *******************************************************************/
    
    /**
     * radius of the working Shpere
     *
     * We use a sphere of radius 10.0 so the numbers are reasonable.
     */
    
    public double radius;
    
    /**
     *  Radius of the earth in km. 
     */
    protected final double Re_km = 6367.435; 
    
    /**
     * Radius of the sphere actually used - 
     * makes the numbers easier to work with.
     */
    private final double Ru_km = 10.0; 
    
    private final int logLevel = 5;

    /*******************************************************************
     * The constructor.
     *******************************************************************/
    
    /**
     *  Sets the radius.
     */
    public Sphere()
    {
	radius = Ru_km;
    }
 
    /*******************************************************************
     * Miscellaneous Utility functions for the package..
     *******************************************************************/
    
    /**
     * Get the radius of the sphere.
     */
    public double getRadius()
    {
	return radius;
    }

    /** 
     * Set the radius of the sphere.
     */
    public void setRadius(double given_radius)
    {
	radius = given_radius;
    }

    /**
     * Set longitude to (-180, 180)
     *
     * @param lon Longitude in decimal degrees.
     */
    public float normalize(float lon)
    {
	while (lon < -180.0) lon += 360.0; 
	while (lon >  180.0) lon -= 360.0; 
	return lon;
    } 
    
    /**
     * Set longitude to (-180, 180)
     *
     * @param lon Longitude in decimal degrees.
     */
    public double normalize(double lon)
    {
	while (lon < -180.0) lon += 360.0; 
	while (lon >  180.0) lon -= 360.0; 
	return lon;
    } 
    
    /**
     * Convert decimal degrees to radians.
     *
     * @param deg Spherical coordinate in decimal degrees.
     */
    public double radians(float deg) 
    {
	return  ((deg) * Math.PI / 180.0);
    } 

    /**
     * Convert decimal degrees to radians.
     *
     * @param deg Spherical coordinate in decimal degrees.
     */
    public double radians(double deg) 
    {
	return  ((deg) * Math.PI / 180.0);
    } 
  
    /**
     * Convert radians to decimal degrees.
     *
     * @param deg Spherical coordinate in radians.
     */
    public double degrees(float rad) 
    {
	return ((rad) * 180.0 / Math.PI);
    }

    /**
     * Convert radians to decimal degrees.
     *
     * @param deg Spherical coordinate in radians.
     */
    public double degrees(double rad) 
    {
	return ((rad) * 180.0 / Math.PI);
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

    /**
     * Log diagnostic messages
     *
     * @param msg Message to log.
     */
    protected void systemLog(String msg) {
	systemLog(msg, 1);
    }

    /**
     * Determine where a point is in relation to a line
     * <P>
     * take the scalar triple product to see if a point is to 
     * the left or right of the line joining two other points.
     * 
     * @param given_point Point of interest.
     *        start_point Start of line.
     *        end_point   End of line.
     *
     * @returns 1 First point is to the left of the line joining the second and third points.
     *	       -1 First point to the right.
     *		0 == indeterminate
     *
     * @see nsidc.spheres.SphericalPolygon
     */
    public int scalarTripleProductTest(Point given_point, 
				       Point start_point,
				       Point end_point)
    {
	double product;
	int result;

	product = ((given_point.x*start_point.y*end_point.z)
		   +(given_point.z*start_point.x*end_point.y)
		   +(given_point.y*start_point.z*end_point.x)
		   -(given_point.z*start_point.y*end_point.x)
		   -(given_point.x*start_point.z*end_point.y)
		   -(given_point.y*start_point.x*end_point.z));

	/**********
		   r1[0]*r2[1]*r3[2]
		   + r2[0]*r3[1]*r1[2]
		   + r3[0]*r1[1]*r2[2]
		   - r3[0]*r2[1]*r1[2]
		   - r1[0]*r3[1]*r2[2]
		   -r2[0]*r1[1]*r3[2];
	**********/

  if(Math.abs(product) < 10*Double.MIN_VALUE) return 0;
  else if(product < 0) return -1;
  return 1;

    }







} /* END Sphere class */
