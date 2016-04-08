package nsidc.spheres;

import java.lang.*;
/*******************************************************************
 * GreatCircle class - provides services and information relating to  
 * a great circle.
 * <P>
 * A great circle is a circle on a sphere with whose origin is at the 
 * center of the sphere.  The shortest distance between any two points  
 * on a sphere is the great circle distance.  Any two points on a sphere  
 * uniquely define a single great circle so long as they are not antipodal.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author
 * R. Swick 13-December-2000 swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder<BR>
 * Copyright 2003 University of Colorado.
 *
 * @see nsidc.spheres.Sphere
 * @see nsidc.spheres.SphericalPolygon
 *******************************************************************/

public class GreatCircle extends Sphere
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    

    /**
     * Points that define the great circle (arc endpoints).
     */
    public Point[] arc_point = new Point[2];

    /**
     * Points for any intersections found.
     */
    public Point[] intersect_point = new Point[2];
    
    /**
     * Highest point achieved by the great circle.
     */
    public Point inflection_point;
    
    /**
     * Constant in plane equations ax+by+cz=0 
     */
    public double a, b, c;
    
    /**
     * Boolean to track if the arc is on a meridian or not.
     */
    public boolean is_meridian = false;
    
    /**
     *  Tmp variable for cartesian coordinates of intersect points.
     */
    private double g, h, w; 
    
    /**
     *  Tmp constant for interim results of the intersect calculations
     */
    protected double planar_const, sphere_const, scale, rad, firstX, secondX;

    /**
     * Tmp point for doing swaps.
     */
    private Point tmp_point;

    /**
     * Constant for intermediate results.
     */
     protected double numerator, denominator;
    
    /**
     * Constant for intermediate results.
     */
    protected  double  m,n,p,q;
    
    /**
     * Constant for intermediate results.
     */
    protected double sqr_term, lin_term, const_term, tmp_z;
    
    /**
     * Maximum latitude of the great circle.
     */
    private double inflection_lat;
    
    /**
     *  Counter
     */
    private int i;
    
    private final int logLevel = 5;

    /*******************************************************************
     * The constructors set the endpoints and initialize the arc.
     *******************************************************************/
    
    /**
     * Empty constructor - do not use.
     */
    protected GreatCircle()
    {
	arc_point = new Point[2];
	arc_point[0] = new Point(0.0,0.0);
	arc_point[1] = new Point(0.0,0.0);
	
	intersect_point = new Point[2];
	intersect_point[0] = new Point(0.0,0.0);
	intersect_point[1] = new Point(0.0,0.0);
    }
    
    /**
     *  Sets the points and initializes the great circle.
     */
    public GreatCircle(double given_lat0, double given_lon0, 
		   double given_lat1, double given_lon1)
    {System.err.println("In GC constuctor #4");
    System.err.println(given_lat1+" "+given_lon1+" "+given_lat0+" "+given_lon0);
    
    arc_point[0] = new Point(given_lat0, given_lon0);
    arc_point[1] = new Point(given_lat1, given_lon1);
    
    System.err.println("About to init");
    init();
    }

    /**
     *  Sets the points and initializes the great circle.
     */
    public GreatCircle(double[] given_lat, double[] given_lon)
    {
	for(i=0; i<given_lat.length; i++)
	    {
		arc_point[i].lat = given_lat[i];
		arc_point[i].lon = given_lon[i];
	    }
	init();
    }
 
    public GreatCircle(Point start, Point end)
    {
	arc_point[0] = new Point(start.lat, start.lon);
	arc_point[1] = new Point(end.lat, end.lon);
	
	init();
    }

    /**
     *  initializes the great circle.
     */
    protected void init(){

	systemLog("In init GC",3); 
	systemLog(arc_point[0].lat+" "+arc_point[0].lon+" "
			   +arc_point[1].lat+" "+arc_point[1].lon,3);
	
	if((arc_point[0].lon == arc_point[1].lon) ||
	   (arc_point[0].lon == arc_point[1].lon + 180.0) ||
	   (arc_point[0].lon + 180.0 == arc_point[1].lon))
	    is_meridian = true;		     
	
	systemLog(arc_point[0].x+" "+arc_point[0].y+" "+arc_point[0].z+" "
		  +arc_point[1].x+" "+arc_point[1].y+" "+arc_point[1].z,3);
	
	systemLog(a+" "+b+" "+c+" ",3);
	
	a = (arc_point[0].y * arc_point[1].z) - (arc_point[1].y * arc_point[0].z);
	b = (arc_point[1].x * arc_point[0].z) - (arc_point[0].x * arc_point[1].z);
	c = (arc_point[0].x * arc_point[1].y) - (arc_point[1].x * arc_point[0].y);
	
	/* get the inflection point */
	/* lat_range = something; */
	computeInflection();
	systemLog("Leave init GC",3); 
    }
    
    /**
     * Get the inflection latitude of the great circle.
     * <P>
     * The inflection latitude is the maximum latitude achieved by the 
     * great circle.  It is the point at which the great circle stops 
     * going north and starts going south.
     *
     * The inflection point, then, is the point at which there is only one
     * (x, y) value that satifies all the equations
     * 
     */
    private void computeInflectionPoint()
    {

	if(arc_point[0].lat == arc_point[1].lat && arc_point[0].lon == arc_point[1].lon){
	    inflection_point = new Point(arc_point[0].lat, arc_point[0].lon);
	    inflection_lat = inflection_point.lat;
	    systemLog("NEW GC INFLECTION "+inflection_point.toString(), 2);

	    return;
	}
	
	/**
	 * In Cartesian space we have two equations and three unknowns.
	 * x^2 + y^2 + z^2 = r^2 : sphere
	 * ax + by + cz = 0      : plane of the great circle
	 * z = z[0]              : plane of latitude - we seek the highest such z[0].
	 *
	 * a, b, c and r are constants.  x, y and z range over {-r, r}
	 * The inflection point is where z is maximized.
	 * 
	 * Depending on which version I use, Mathmetica tells me the solutions are:
	 *
	 * x = (-cz - ((b^2 * (-cz))/(a^2 + b^2)) (-+) (ab * sqrt(-(cz)^2 +(r^2 - z^2)a^2 +(r^2 - z^2)b^2)/(a^2+b^2)))/a
	 * and
	 * y = (-bcz (+-) a * sqrt(-(cz)^2 +(r^2 - z^2)a^2 +(r^2 - z^2)b^2)) / (a^2 + b^2)
	 *
	 * Or
	 * x = (-(acz) +- sqrt(a^2b^2r^2 + b^4r^2 - a^2b^2z^2 - b^4z^2 - b^2c^2z^2)) / (a^2 + b^2)
	 * and
	 * y = (-(cz) + (a^2cz/(a^2 + b^2)) -+ a(sqrt(a^2b^2r^2 + b^4r^2 - a^2b^2z^2 - b^4z^2 - b^2c^2z^2))/(a^2 + b^2))/b
	 * 
	 * Note the radical.  If the term under the radical is negative there is no (real) solution. 
	 * Consequently:
	 *
	 * (-cz)^2 +(r^2 - z^2)a^2 +(r^2 - z^2)b^2) >= 0
	 *
	 * -c^2z^2 + r^2a^2 -z^2a^2 +r^2b^2 -z^2b^2 >= 0
	 *
	 * r^2a^2 + r^2b^2 >= z^2(c^2+a^2+b^2)
	 *
	 * (r^2a^2 + r^2b^2) / (c^2+a^2+b^2) >= z^2
	 *
	 * -Sqrt[(r^2a^2 + r^2b^2) / (c^2+a^2+b^2)] <= z <= +Sqrt[(r^2a^2 + r^2b^2) / (c^2+a^2+b^2)]
	 *
	 * So if we set z == +Sqrt[(r^2a^2 + r^2b^2) / (c^2+a^2+b^2)] we have the inflection z.
	 **/

	double max_z = Math.sqrt((radius*radius*a*a + radius*radius*b*b) / (c*c+a*a+b*b));

	/*
	 * Then y = -(b*c*max_z)/(a*a+b*b)
	 * and  x = (-c*max_z - ((-b*b*c*max_z)/(a*a+b*b)))/a
	 */
	inflection_point = new Point((-c*max_z - ((-b*b*c*max_z)/(a*a+b*b)))/a, -(b*c*max_z)/(a*a+b*b), max_z);
	inflection_lat = inflection_point.lat;
	systemLog("NEW GC INFLECTION "+inflection_point.toString(), 2);
	
	/*
	 * Or   y = (-c*max_z + ((a*a*c*max_z)/(a*a+b*b)))/b 
	 * and  x = (-a*c*max_z)/(a*a+b*b)
	 */
	inflection_point = new Point((-a*c*max_z)/(a*a+b*b),(-c*max_z + ((a*a*c*max_z)/(a*a+b*b)))/b , max_z);
	inflection_lat = inflection_point.lat;
	systemLog("CHK GC INFLECTION "+inflection_point.toString(), 2);

    }
     
    private void computeInflection()
    {
	/**
	 * In Cartesian space we have three equations and three unknowns.
	 * x^2 + y^2 + z^2 = r^2 : sphere
	 * ax + by + cz = 0      : plane of the great circle
	 * z = z[MAX]            : Highest plane of latitude
	 *
	 * We can simplify by substituting some constant terms.
	 * Let s = r^2 - z^2    so x^2 + y^2 = s
	 * and let p = -cz      so ax + by = p
	 *
	 * Mathematica tells me the solutions are:
	 *
	 * x = (p - ((b^2 * p)/(a^2 + b^2)) (-+) (ab * sqrt(-p^2 +sa^2 +sb^2)/(a^2+b^2)))/a
	 * and
	 * y = (bp (+-) a * sqrt(-p^2 +sa^2 +sb^2)) / (a^2 + b^2)
	 *
	 * But we want the point where only one (x, y) satisfys so
	 * 
	 * x = -cz - ((b^2 * -cz)/(a^2 + b^2)) and
	 * y = -bcz so
	 *
	 * Define some constants to work with.
	 **/
	
	denominator = (a * a) + (b * b);
	scale = (b * b * -c) / denominator;

	/**
	 * So x = -cz - (z * scale) = z(-c - scale)
	 *
	 * Since x^2 + y^2 + z^2 = r^2 we can solve for z
	 *
	 * z^2 *(-c - scale)*(-c - scale) +z^2*b^2*c^2 +z^2 = radius*radius
	 * z^2((-c - scale)*(-c - scale)+(b*b*c*c)+1) = r^2
	 * z^2 = (radius*radius) / (-c - scale)*(-c - scale)+(b*b*c*c)+1
	 *
	 **/
	sqr_term = (-c - scale) * (-c - scale);
	lin_term = (b * b * c * c) + 1;
	const_term = (radius * radius);

	rad =  const_term / (sqr_term + lin_term);
	tmp_z = Math.sqrt(rad);
	
	inflection_point = new Point(tmp_z * (-c - scale), (-b * c * tmp_z), tmp_z);
	inflection_lat = inflection_point.lat;
	systemLog("GC INFLECTION "+inflection_point.toString(), 2);

	computeInflectionPoint();
	
    } /* END computeInflection() */
 

    /**********************************************************************
     * The intersect methods.
     **********************************************************************/
    
    /**
     * Determine if/where this great circle intersects another.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * If found the intersection points are placed in the intersect_point array.
     * 
     * @return true    if the two great circles intersect.<br>
     *         false   if the two great circles do not intersect.
     *
     * @see nsidc.spheres.Scene
     * @see nsidc.spheres.SphericalPolygon
     **/

    public boolean intersectsGreatCircle(GreatCircle other)
    {
	/*
	 * First blank out the intersect_points so old results don't linger.
	 * Just in case the calling method doesn't check the return status.
	 */
	intersect_point[0] = null;
	intersect_point[1] = null;

	/*
	 * Check the trivial cases
	 */
	if(null == other)
	    return false;
	if(this.equals(other))
		   return true;

	/*
	 * Then check the special cases.
	 * If both great circles are meridians they cross at the poles.
	 */
	if(this.is_meridian && other.is_meridian)
	    {
		intersect_point[0] = new Point(90.0, 135.0);
		intersect_point[1] = new Point(-90.0, -135.0);
		return true;
	    }

	/*
	 * If one Great circle is a meridian we have only to find the latitude
	 * on the other great circle for the known longitude.
	 *
	 * Unfortunately I don't know how to do that at the moment.
	 */
	/***
	if(this.is_meridian)
	    return true;
	if(other.is_meridian)
	    return true;
	***/
	/*
	 * Neither great circle is a meridian so the great circles cross 
	 * every meridian exactly once.  Moreover the two great circles 
	 * cross each other exactly twice.  Find those crossing points.
	 *
	 * There's a lot going on here.  
	 * We have two points on each great circle converted to cartesian coordinates.
	 * So we have two planes through the origin (ax+by+cz=0)
	 * These two planes intersect and the line formed by that intersection
	 * intersects the sphere.  It is those two points we want.
	 *
	 * So we have:
	 * x^2 + y^2 + z^2 = r^2  : the sphere
	 * ax + by + cz = 0       : a plane
	 * dx + ey + fz = 0       : another plane
	 *
	 * we know x = (-by-cz)/a  
	 * so d((-by-cz)/a) +ey +fz =0
	 * solve for y and
	 * y = z((dc - fa)/ea-db)) so let
	 * g = ((dc - fa)/ea-db))
	 */ 

	numerator = ((other.a * c) - (other.c * a));
	denominator = ((other.b * a) - (other.a * b));
	g = numerator/denominator;
	
	/*
	 * so y = gz, solve for x in terms of z and
	 * x = (-gbz - cz)/a = z((-gb -c)/a)
	 * so let
	 * h = (-gb -c)/a
	 */
	
	numerator = ((-g * b) - c);
	h = numerator / a;
	
	/*
	 * Now y = gz and x = hz, so the points on the sphere are
	 * (hz)^2 + (gz)^2 + z^2 = r^2
	 * solve for z
	 * z = +- sqrt(r^2/(h^2 + g^2 + 1))
	 * so let 
	 * w = sqrt(r^2/(h^2 + g^2 + 1))
	 *
	 * Note that (r^2/(h^2 + g^2 + 1) is absolutely positive.
	 * There's no chance of imaginary numbers because ANY two 
	 * great circles cross exactly twice.
	 */

	numerator = Math.pow(radius, 2);
	denominator = Math.pow(h, 2) + Math.pow(g, 2) + 1;
	w = Math.sqrt(numerator/denominator);

	/*
	 * The points we're interesting in are:
	 * (hw, gw, w) and (-hw, -gw, -w)
	 */

	intersect_point[0] = new Point(h*w, g*w, w);
	intersect_point[1] = new Point(-h*w, -g*w, -w);

	return true;

    } /* END intersectsGreatCircle(other) */

    
    
    
    /**
     * Determine if this great circle intersects a parallel.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * If found the intersection points are placed in the intersect_point array.
     *
     * @param latitude The latitude of interest.
     *
     * @return true    if the great circle intersects the parallel.<br>
     *         false   if the great circle does not intersect the parallel.
     *
     * @see nsidc.spheres.LatLonBoundingBox
     */

    public boolean intersectsLatitude(double latitude)
    {
	double lat_z;
	
	/*
	 * First blank out the intersect_points so old results don't linger.
	 * Just in case the calling method doesn't check the return status.
	 */
	intersect_point[0] = null;
	intersect_point[1] = null;
	
	/*
	 * Check the trivial cases
	 */
	if(Math.abs(latitude) > inflection_point.lat)
	    return false;
	
	/* 
	 * Gotta be one or two crossings.
	 *
	 * In Cartesian space we have three equations and three unknowns.
	 * x^2 + y^2 + z^2 = r^2 : sphere
	 * ax + by + cz = 0      : plane of the great circle
	 * z = z[0]              : plane of latitude
	 *
	 * We can simplify by substituting some constant terms.
	 * Let s = r^2 - z^2    so x^2 + y^2 = s
	 * and let p = -cz      so ax + by = p
	 *
	 * Mathmetica tells me the solutions are:
	 *
	 * x = (p - ((b^2 * p)/(a^2 + b^2)) (-+) (ab * sqrt(-p^2 +sa^2 +sb^2)/(a^2+b^2)))/a
	 * and
	 * y = (bp (+-) a * sqrt(-p^2 +sa^2 +sb^2)) / (a^2 + b^2)
	 *
	 * First define some constants
	 */
	
	lat_z = radius * Math.sin(radians(latitude));
	planar_const = -c * lat_z;
	sphere_const = (radius * radius) - (lat_z * lat_z);
	scale = (a * a) + (b * b);
	rad = (sphere_const * a * a) + (sphere_const * b * b) - (planar_const * planar_const);

	/* If this is negative taking the square root would require imaginary numbers.
	 * I'm guessing that probably means the two planes intersect outside the sphere.
	 *
	 * That could happen if the latitude was above or below the inflection point -
	 * but we already checked that - so something odd is goin on.
	 */
	if(rad < 0)
	    {
		systemLog("RAD IS NEGATIVE! How is that even possible?",1);
		systemLog("-c: "+c+" * z: "+lat_z+" = Plane: "+planar_const, 1);
		systemLog("ILS Rad1: "+ rad+" Plane: "+planar_const, 1);
		return false;
	    }
	systemLog("-c: "+c+" * z: "+lat_z+" = Plane: "+planar_const, 1);
	systemLog("ILS Rad1: "+ rad+" Plane: "+planar_const, 1);
	rad = Math.sqrt(rad);
	systemLog("ILS sqrt Rad1: "+ rad, 1);
	
	firstX = (b*b*planar_const)/scale;
	secondX = ((a*b*rad)/scale);

	/**
	 * Set the points.
	 */
	intersect_point[0] = new Point(((planar_const - firstX - secondX) / a),
				       (((b*planar_const) + (a*rad)) / scale),
				       lat_z);
	intersect_point[1] = new Point(((planar_const - firstX + secondX) / a),
				       (((b*planar_const) - (a*rad)) / scale),
				       lat_z);
	return true;
	
    } /* END intersectsLatitude */


    /**
     * Determine if/where this great circle intersects a small circle.
     * <P>
     * The basic strategy is to convert everything to cartesian 3-space first
     * because the math is easier.  All the action takes place in cartesian space.  
     * If found the intersection points are placed in the intersect_point array.
     * 
     * @return true    if the two circles intersect.<br>
     *         false   if the two circles do not intersect.
     *
     * @see nsidc.spheres.Scene
     * @see nsidc.spheres.SphericalPolygon
     **/

    public boolean intersectsSmallCircle(SmallCircle small)
    {
	/*
	 * First blank out the intersect_points so old results don't linger.
	 * Just in case the calling method doesn't check the return status.
	 */
	
	intersect_point[0] = null;
	intersect_point[1] = null;

	/*
	 * Check the trivial cases
	 */
	if(null == small)
	    return false;
	if(small.parallel(this))
	    return false;

	/*
	 * We have three equations and three unknowns.
	 * ax+by+cz = 0 on the great circle
	 * ex+fy+gz = d on the small circle and
	 * x^2 + y^2 + z^2 = r^2 on the sphere.
	 *
	 * First solve for x on the great circle so
	 * x = (-by -cz)/a
	 *
	 * Solve for y on the small circle so
	 * e((-by -cz)/a) + fy + gz = d
	 * (-eby/a) - (ecz/a) + fy + gz = d
	 * y((eb/a) + f) = d - gz + (ecz/a)
	 * y((eb/a) + f) = d + z((ec/a) - g)
	 * y = (d + z((ec/a) - g))/((eb/a) + f) or
	 * y = (d/((eb/a) + f) + z(((ec/a) - g)/((eb/a) + f))
	 * so let:
	 */
	
	denominator = ((small.a * b)/a) + small.b ;
	n = small.distance / denominator;
	
	numerator = ((small.a * c)/a) - small.c;
	m = numerator / denominator;

	/*
	 * so y = mz + n which means
	 * x = (-b(mz + n) - cz)/a
	 * x = (-bmz -bn - cz)/a
	 * x = z((-bm -c)/a) - bn/a
	 * so let:
	 */
	numerator = (-b * m) - c;
	p = numerator / a;
	q = (-b * n)/a;
	
	/*
	 * so x = pz + q 
	 * substitute and solve for z
	 *
	 * (pz+q)^2 + (mz+n)^2 + z^2 = r^2
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
	 * z = (-lin (-+) sqrt[lin^2 - 4*const*sqr])/2*sqr
	 */
	
	rad = (lin_term*lin_term) - (4.0*const_term*sqr_term);
	
	/* 
	 * If rad is negative taking the square root would involve 
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
 

    } /* END intersectsSmallCircle(SmallCircle)  */



    protected void systemLog(String msg, int level) {
	if(level > logLevel)
	    System.out.println(msg);
    }
    
    public String toString()
    {
	return new String("Great Circle: a="+a+" b="+b+" c="+c+" ("+
			  arc_point[0].toString()+", "+arc_point[1].toString()+")");
    }

} /* END GreatCircle class */

