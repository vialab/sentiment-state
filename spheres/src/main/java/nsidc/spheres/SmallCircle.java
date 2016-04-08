package nsidc.spheres;

import java.lang.*;
/*******************************************************************
 * SmallCircle class - provides services and information relating 
 * to a small circle.
 * <P>
 * A small circle is a circle on a sphere that is not a great circle.  
 * For purposes of this package we define small circles relative to  
 * the parallel great circle.  This seems reasonable since the small   
 * circles used  in this package are all edges of orbits and hence a   
 * known distance from the great circle defined by nadir.
 * <P>
 * The left and right edges of a scene are small circle arcs so we need 
 * this class to compare scenes to other areas on a sphere.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 19-December-2000 swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 * @see nsidc.spheres.Scene
 * @see nsidc.spheres.GreatCircle
 * @see nsidc.spheres.SmallCircleArc
 *******************************************************************/

public class SmallCircle extends GreatCircle
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    public double distance;
    public Point[] arc_point = new Point[2];
    public Point[] intersect_point = new Point[2];

    /**
     * Constant for intermediate results.
     */
    protected double  m,n,p,q;
    protected LonRange lon_range;
    
    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    /**
     * Empty constructor - do not use.
     */
    protected SmallCircle()
    {
    }

    /**
     * Given a great circle and a distance.
     */
    public SmallCircle(GreatCircle referenceGC, double given_distance)
    {
	if(given_distance > radius)
	    return;
	    
	copyGC(referenceGC);
	distance = given_distance;
    }
    
    /**
     *  Given two points on the great circle and a distance.
     */
    public SmallCircle(double given_lat0, double given_lon0, 
		       double given_lat1, double given_lon1,
		       double given_distance)
    {
	if(given_distance > radius)
	    return;

	copyGC(new GreatCircle(given_lat0, given_lon0, 
			       given_lat1, given_lon1));
	distance = given_distance;
    }
    
    /**
     *  Given two points on the small circle and a reference great circle.
     */
    public SmallCircle(double given_lat0, double given_lon0, 
		       double given_lat1, double given_lon1,
		       GreatCircle reference)
    {
	arc_point[0] = new Point(given_lat0, given_lon0);
	arc_point[1] = new Point(given_lat1, given_lon1);
	
	copyGC(reference);
	distance = a*arc_point[0].x + b*arc_point[0].y + c*arc_point[0].z;
	
	/*
	 * Check make sure.
	 */
	if(distance != a*arc_point[1].x + b*arc_point[1].y + c*arc_point[1].z)
	    {
		systemLog("DISTANCES DON'T MATCH!  Bad points!",1);
		systemLog(distance + " != "+ a*arc_point[1].x + b*arc_point[1].y + c*arc_point[1].z,1);
	    }
    }
    
    /**
     *  Given two points on the great circle and a distance.
     */
    public SmallCircle(Point point_0,
		       Point point_1,
		       double given_distance)
    {
	if(given_distance > radius)
	    return;

	copyGC(new GreatCircle(point_0, point_1));
	distance = given_distance;
    }
     
    /**
     *  Given two points on the small circle and a reference great circle.
     */
    public SmallCircle(Point point_0,
		       Point point_1,
		       GreatCircle reference)
    {
	arc_point[0] = new Point(point_0.lat, point_1.lon);
	arc_point[1] = new Point(point_1.lat, point_1.lon);
	
	copyGC(reference);
	distance = a*arc_point[0].x + b*arc_point[0].y + c*arc_point[0].z;
	
	/*
	 * Check make sure.
	 */
	if(distance != a*arc_point[1].x + b*arc_point[1].y + c*arc_point[1].z)
	    {
		systemLog("DISTANCES DON'T MATCH!  Bad points!",1);
		systemLog(distance + " != "+ a*arc_point[1].x + b*arc_point[1].y + c*arc_point[1].z,1);
	    }
    }
    
    public void copyGC(GreatCircle reference)
    {
	this.a = reference.a;
	this.b = reference.b;
	this.c = reference.c;
	this.is_meridian = reference.is_meridian;
    }

    /**
     * Determine if this small circle is parallel to a given great circle.
     */
    public boolean parallel(GreatCircle gc)
    {
	return(this.a == gc.a && this.b == gc.b && this.c == gc.c);
    }


    
    /*******************************************************************
     * Intersect methods.
     *******************************************************************/
    
    /**
     * Determine if this small circle intersects another.
     *
     * @param other Another small circle.
     *
     * @result true - if the two arcs intersect.<br>
     *         false - if the two arcs do not intersect.
     */
    
    public boolean intersectsSmallCircle(SmallCircle other)
    {
	/* double  m,n,p,q;
	double numerator, denominator;
	double sqr_term, lin_term, const_term, rad;
	double tmp_z;
	*/
	intersect_point[0] = null;
	intersect_point[1] = null;

	/*
	 * first check see if it's even possible.
	 */
	if(!(this.lon_range.overlaps(other.lon_range)))
	    return false;
	
	/*
	 * Each small circle is defined by ax+by+cz = d and
	 * we want to know where they intersect on the sphere.
	 * 
	 * So we have three equations and three unkowns.
	 * First solve for x in terms of z so:
	 * x = (d-by-cz)/a
	 *
	 * Then solve ex+fy+gz = h for y in terms of z so:
	 * e(d-by-cz)/a + fy + gz = h
	 * ed/a - eby/a - ecz/a + fy + gz = h
	 * y(f-eb/a) = h - ed/a + ecz/a - gz
	 * y = (h - ed/a + ecz/a - gz)/(f-eb/a) or
	 * y = z((ec/a - g)/(f-eb/a)) + ((h - ed/a)/(f-eb/a))
	 * so let
	 */
	numerator = ((other.a * c)/a) - other.c;
	denominator = other.b - ((other.a * b)/a);
	m = numerator / denominator;
	
	numerator = other.distance - ((other.a * distance)/a);
	denominator = other.b - ((other.a * b)/a);
	n = numerator / denominator;
	
	/*
	 * So y = mz + n which means
	 * x = (d-b(mz+n)-cz)/a
	 * x = (d - bmz - bn - cz)/a
	 * x = z(-bm -c)/a + (d-bn)/a
	 * so let
	 */
	numerator = (-b * m) -c;
	p = numerator / a;
	
	numerator = distance - (b * n);
	q = numerator / a;
	
	/*
	 * so x = pz + q;
	 *
	 * substitute in the equation of the sphere and solve for z
	 * (pz+q)^2 + (mz+n)^2  + z^2 = r^2
	 * p^2z^2 + 2pzq + q^2 + m^2z^2 + 2mzn + n^2 + z^2 = r^2
	 * z^2(1+p^2+n^2) + z(2pq + 2mn) + q^2 + p^2 = r^2 
	 * so let
	 */
	
	sqr_term = 1.0+(p*p)+(n*n);
	lin_term = (2.0*p*q) + (2.0*m*n);
	const_term = (q*q) + (p*p) - (radius * radius);
	
	/*
	 * So sqr_trm(z^2) + lin_term(z) + const_term = 0
	 *
	 * Solve for z
	 * z = (-lin (-+) sqrt(lin^2 - 4*const*sqr))/2*sqr
	 */
	
	rad = (lin_term*lin_term) - (4.0*const_term*sqr_term);
	
	/* 
	 * If rad is negative taking the sqaure root would involve 
	 * imaginary numbers.  I'm guessing this means the circles 
	 * don't cross.
	 */
	if(rad < 0.0)
	    return false;

	rad = Math.sqrt(rad);
	numerator = -lin_term - rad;
	denominator = 2.0*sqr_term;
	tmp_z = numerator/denominator;
	intersect_point[0] = new Point(((p*tmp_z)+q), ((m*tmp_z)+n), tmp_z);

	numerator = -lin_term + rad;
	tmp_z = numerator/denominator;
	intersect_point[1] = new Point(((p*tmp_z)+q), ((m*tmp_z)+n), tmp_z);
	
	return true;
	    
    }



} /* END SmallCircle class */
