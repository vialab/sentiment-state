package nsidc.spheres;

import java.lang.*;
/**************************************************************************
 * Orbit class - Provide services and information relating to an orbit.
 * <P>
 * This orbit model isn't anything fancy.  We just draw a great circle 
 * and rotate the sphere under it.  As such it works fairly well for satellites
 * in a circular orbit with forward, nadir, or backward looking sensors.  For 
 * satellites with elliptical orbits and/or side viewing sensors you'll need 
 * something fancier.
 * <P>
 * This class assumes a spherical earth.<br>
 * This class assumes orbits start and end at the equator on the ascending pass.<br>
 *
 * @author 
 * 08-September-2000 R.Swick swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 *
 * @see nsidc.spheres.Sphere
 * @see nsidc.spheres.Scene
 **************************************************************************/

public class Orbit extends Sphere
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     *  The declination of the orbit in decimal degrees West of North.
     *  For sun-syncronous orbits this is the same as (inclination - 90).
     */
    public double declination;      
    
    /**
     * Orbital period in decimal minutes.
     */
    public double period;         
    
    /**
     *  Swath width at the equator in (scaled) Km.
     */
    public double eq_swath_width;   
    
    /**
     *  Swath width at the equator in radians.
     */
    public double eq_swath_width_rad;   
    
    /**
     *  Swath width at the equator in degrees.
     */
    public double eq_swath_width_deg;   
    
    /**
     * Absolute value of highest/lowest lat achieved by nadir in decimal degrees. 
     */
    public double inflection_lat; 
    
    /**
     * Max latitude of the swath edge at the inflection point. 
     */
    public double inflection_max_lat;  
    
    /**
     * Min latitude of the swath edge at the inflection point. 
     */
    public double inflection_min_lat;  
    
    /**
     *  Absolute value of latitude above/below which no orbits have coverage.
     */
    public float maxCoverageLat = 91;
    
    /**
     *  Absolute value of latitude above/below which all orbits have coverage.
     */
    public float totalCoverageLat = 91;
    
    /**
     *  Boolean to indicate if this starts/ends at the south pole or the equator
     */
    public boolean pole_to_pole = true;
    
    /**
     * rotation of the earth in radians/minute 
     */
    protected final double ROTATION_RATE = (2.0*Math.PI/(24.0*60.0));
    
    /* 5 = off, 0 = full on */
    private final int logLevel = 5;

    /*******************************************************************
     * The constructors.
     *******************************************************************/
    /**
     * Empty constructor - do not use.
     */
    protected Orbit()
    {
    }

    /**
     * Construct and initialize an orbit.
     */
    public Orbit(String given_declination, 
	  String given_period, 
	  String given_eq_swath_width)  
    {
	this.init(Float.valueOf(given_declination).floatValue(),
		  Float.valueOf(given_period).floatValue(),
		  Float.valueOf(given_eq_swath_width).floatValue());
    }
        
    /**
     * Construct and initialize an orbit.
     */
    public Orbit(float given_declination, 
	  float given_period, 
	  float given_eq_swath_width) 
    {
	this.init((double)given_declination, 
		  (double)given_period, 
		  (double)given_eq_swath_width); 
    } 

    /**
     * Construct and initialize an orbit.
     */
    public Orbit(double given_declination, 
	  double given_period, 
	  double given_eq_swath_width) 
    {
	this.init(given_declination, 
		  given_period, 
		  given_eq_swath_width); 
    }
    
    /**
     * Initialize the various aspects of this orbit - on a tiny earth.
     */
    private void init(double given_declination, 
		      double given_period, 
		      double given_eq_swath_width) 
    {
	double ratio;
	int i;
	float[] edgeLon, edgeLat;
	
	declination = given_declination;
	period = given_period;

	/* check they didn't accidentally give us inclination */
	while(declination > 90){
	    systemLog("DECLINATION: "+declination+" GREATER THAN 90.  This is bad. FIX IT!");
	    declination -= 90.0;
	}	    

	/* scale the swath width to the working sphere. */
	ratio = radius / Re_km;
	eq_swath_width = given_eq_swath_width * ratio;
	eq_swath_width = Math.max(eq_swath_width, 0.00001);

	eq_swath_width_deg = eq_swath_width / ((2*Math.PI*radius)/360.0);
	eq_swath_width_rad =  radians(eq_swath_width_deg);
	systemLog("Converted RAD: "+eq_swath_width_rad, 4);
	eq_swath_width_rad =  eq_swath_width / radius; 
	systemLog("Calculated RAD: "+eq_swath_width_rad, 4);
	
	inflection_lat = (90.0 - declination);
	edgeLon = new float[2];
	edgeLat = new float[2];

	getCrossSwathEdges((float)(inflection_lat - 0.00001), 45, 
			   edgeLat, edgeLon, true);
	inflection_min_lat = Math.min(edgeLat[0], edgeLat[1]);
	inflection_max_lat = inflection_lat + (inflection_lat - inflection_min_lat);
	    
	if(inflection_max_lat < 90)
	    maxCoverageLat = (float)inflection_max_lat;
	if(inflection_max_lat > 90)
	    totalCoverageLat = (float)(180.0 - inflection_max_lat);
	
	systemLog("INF: "+inflection_lat+" Max: "+inflection_max_lat+" Min: "+inflection_min_lat, 1);
	systemLog("TOT: "+totalCoverageLat+" Lim: "+maxCoverageLat, 1);
    }


    /********************************************************************************
     * Get the ascending nodal crossing of the orbit with nadir passing through the 
     * given point.
     *
     * @param lat Latitude of the given point in degrees.
     * @param lon Longitude of the given point in degrees.
     * @param ascending if true find the orbit with nadir passing 
     *          through the given point on the ascending pass.<br>
     *          Else find the orbit with nadir passing through 
     *     	the given point on the descending pass.
     * @param adjust if true adjust for the rotation of the Earth.<br>
     *                      Else consider a static sphere.
     *    
     * @return	lam0 - the nodal crossing in degrees longitude.
     *            
     *********************************************************************************/   
    public float getNodalCrossing(float lat, float lon, 
				  boolean ascending, boolean adjust)
    { 
	return getNodalCrossing((double) lat, (double) lon, 
			       ascending, adjust);
    }
	public float getNodalCrossing(double lat, double lon, 
				  boolean ascending, boolean adjust)
    {
	double theta, numerator, denominator, adjustment;
	double lam0 = 0, alt_lam0 = 0;
	double distance = 0, alt_distance = 0;
	systemLog("GNC: "+lat+" - "+lon+" - "+declination+" - "+period+" - "+ascending, 1);
	lon = normalize(lon);
	
	/*
	 *    	Use law of sines to find theta (the angle between the longitude
	 *		and the track)
	 */
	systemLog( "inc: "+declination+" r(inc): "+radians(declination)+" lat: "+
	   lat+" r(90-lat): "+radians(90 - lat), 1);
	systemLog( "sin(inc): "+Math.sin(radians(declination))+" sin(r(90-lat)): "+ 
	   Math.sin(radians(90 - lat)), 1); 

	theta = Math.asin(Math.sin(radians(declination)) / 
			  Math.sin(radians(90 - lat)));
	if(ascending){
	    systemLog("ascending: theta = " + theta + " : "+ degrees(theta)+" degrees\n", 1);
	    theta = Math.PI - theta;
	    
	}
	else{
	     systemLog("descending: theta = " + theta + " : "+ degrees(theta)+" degrees\n", 1); 
	}
	/*
	 *  	Use Napiers second analogy (CRC Standard Math Tables, 26th Edition, 
	 *    		p. 147) to find lam0 (the nodal crossing of the orbit with 
	 *		nadir passing through (lat, lon))
	 */
	if(0.0 == lat) 
	    {
		if(ascending)
		    lam0 = radians(lon);
		else
		    lam0 = radians(lon) + Math.PI;
	    }
	else
	    {
		systemLog( "theta: "+theta+" inc: "+declination+" radians(inc): "+
		   radians(declination)+" lat: "+lat, 1); 
		numerator = Math.cos(radians(declination) / 2.0 - theta / 2.0) * 
		    Math.sin(radians(-lat / 2.0));
		denominator = Math.sin(radians(declination) / 2.0 - theta / 2.0) *
		    Math.sin(radians(90 - lat / 2.0));
		lam0 = radians(lon) + 2.0 * Math.atan2(numerator,denominator); 
		
		/*
		 *	Try it again with law of sines.
		 */
		numerator = Math.sin(radians(lat)) * Math.sin(Math.PI - theta);
		denominator = Math.sin((Math.PI / 2.0) - radians(declination));

		/* systemLog( "num: "+numerator+" den: "+denominator+"\n", 1);
		   systemLog( "calc: "+(numerator/denominator, 1)+", "+
		   Math.asin(numerator / denominator)+"\n");
		*/

		if(ascending)
		    alt_lam0 = radians(lon) + Math.asin(numerator / denominator);
		else
		    alt_lam0 = radians(lon) - Math.asin(numerator / denominator) - Math.PI ;
		

		/* systemLog("lam0: "+ 
		   lam0+" - "+
		   alt_lam0+" - "+
		   (radians(lon) - Math.asin(numerator / denominator))+"\n");
		   systemLog("lam0: "+ 
		   degrees(lam0)+" - "+
		   degrees(alt_lam0)+" - "+ 
		   degrees(radians(lon) - Math.asin(numerator / denominator))+"\n", 1);
		*/
	    }

	/*
	 *  	Use Napiers first analogy to find the distance from the nodal 
	 *    		crossing to the point in order to correct for the rotation 
	 *		of the Earth.
	 */ 
	
	if(adjust){ 
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
		    
		    /*
		     *	Try it again with law of sines.
		     */
		    numerator = Math.sin(radians(lon) - alt_lam0);
		    denominator = Math.sin(theta);
		    if(ascending)
			alt_distance = -2 * Math.PI - Math.asin(numerator / denominator);
		    else
			alt_distance = Math.asin(numerator / denominator) - Math.PI;
		    
		    //systemLog("dist: "+distance+" vs. "+alt_distance+"\n", 1);
		}

	    if(pole_to_pole){
		systemLog("Pole to Pole", 1);
		systemLog("Point "+lat+" : "+lon, 1);
		systemLog("Distance "+distance+" : "+alt_distance, 1);
		
		while((-0.5 * Math.PI) > distance) distance += 2.0 * Math.PI;
		while((-0.5 * Math.PI) > alt_distance) alt_distance += 2.0 * Math.PI;
		while((1.5 * Math.PI) < distance) distance -= 2.0 * Math.PI;
		while((1.5 * Math.PI) < alt_distance) alt_distance -= 2.0 * Math.PI;
	    }else{
		systemLog("E to E", 1);
		while(0 > distance) distance += 2 * Math.PI;
		while(0 > alt_distance) alt_distance += 2 * Math.PI;
		while((2.0 * Math.PI) < distance) distance -= 2 * Math.PI;
		while((2.0 * Math.PI) < alt_distance) alt_distance -= 2 * Math.PI;
	    }

	    adjustment = period * distance * ROTATION_RATE / (2 * Math.PI);
	    lam0 += adjustment;
	    systemLog("adj: "+adjustment+" - ", 1); 
	    adjustment = period * alt_distance * ROTATION_RATE / (2 * Math.PI);
	    alt_lam0 += adjustment;
	    systemLog("alt: "+adjustment+"\n", 1);
	       systemLog("lam0: "+degrees(lam0)+" vs. "+degrees(alt_lam0)+"\n", 1);
	    
	}
	return((float)normalize(degrees(lam0))); 
	
    } /* end getNodalCrossing */


/************************************************************************
 * Get the swath edge points at the given latitude.
 *
 * @param lat Latitude of the given point in degrees.
 * @param lon Longitude of the given point in degrees.
 * @param ascending If true find edges for the orbit with nadir  
 *     	passing through the given point on the ascending pass. <br> 
 *    	Else find the edges for the orbit with nadir 
 *     	passing through the given point on the descending pass.<P>
 *            
 * @param edgeLon[] array for the output.  The east and west lon of 
 *                      the swath edge in no guaranteed order.<P>
 * @return true - if sucessful <br>
 *         true - if unsucessful.
 *
 ************************************************************************/

    public boolean getLonSwathEdges(float lat, float lon, 
				  boolean ascending, float[] edgeLon)
    {
	double crossingLon, eastDeclinationLon, westDeclinationLon;
	double planar_const, sphere_const, scale;
	double rad, firstX, secondX, tmpLon;
	double a,b,c,d;
	double[] x,y,z;
	int i,j;
	double circumference, distance;
	
	float adjusted_lat = lat;
	/*
	 *  If the given point is on the equator don't bother.
	 *  We already know the equatorial swath width.
	 */
	if(0 == lat) {
	    circumference = (2 * Math.PI * radius);
	    distance = ((360.0 * (eq_swath_width/circumference)) / 2.0);
	    edgeLon[0] = (float)(lon - distance);
	    edgeLon[1] = (float)(lon + distance);
	    
	    return true;
	}
	  
	/*  Else we have to do things the hard way.
	 *  First get the static earth crossing lon so we have a 
	 *  second point to define the great circle.
	 */
	if(lat > inflection_lat && lat < inflection_max_lat)
	    adjusted_lat = (float)(inflection_lat - 0.00001);
	
	if(lat < -inflection_lat && lat > -inflection_max_lat)
	    adjusted_lat = (float)(-inflection_lat + 0.00001);
	

	crossingLon = getNodalCrossing(adjusted_lat, lon, ascending, false);
	systemLog("GLSE Cross: "+ crossingLon, 1);
	/*  On a static earth the declination lon is 90 degrees off the crossing.
	 *  The orbit is a great circle after all.
	 */
	if(ascending){
	    westDeclinationLon = crossingLon - 90.0;
	    normalize(westDeclinationLon);
	    eastDeclinationLon = crossingLon + 90.0;
	    normalize(eastDeclinationLon);
	}else{
	    westDeclinationLon = crossingLon + 90.0;
	    normalize(westDeclinationLon);
	    eastDeclinationLon = crossingLon - 90.0;
	    normalize(eastDeclinationLon);
	}
	/* declare a bunch coordinates */
	x = new double[6];
	y = new double[6];
	z = new double[6];
	for( i=0; i<6; i++){
	    x[i]=y[i]=z[i]=0;
	}
		  
	/* convert everything to cartesian coordinates */
	/* given point */
	x[0] = radius * Math.cos(radians(lon)) * Math.cos(radians(adjusted_lat));
	y[0] = radius * Math.sin(radians(lon)) * Math.cos(radians(adjusted_lat));
	z[0] = radius * Math.sin(radians(adjusted_lat));
	systemLog("Given (x,y,z): ("+x[0]+", "+y[0]+", "+z[0]+")", 1);
	/* nodal crossing */
	x[1] = radius * Math.cos(radians(crossingLon)) * Math.cos(0.0);
	y[1] = radius * Math.sin(radians(crossingLon)) * Math.cos(0.0);
	z[1] = radius * Math.sin(0.0); 
	systemLog("Crossing (x,y,z): ("+x[1]+", "+y[1]+", "+z[1]+")", 1);
	systemLog("GLSE 0: ("+x[0]+", "+y[0]+", "+z[0]+")", 1);
	systemLog("GLSE 1: ("+x[1]+", "+y[1]+", "+z[1]+")", 1);
	/*  The given point, the crossing, and the origin define
	 *  a plane by ax+by+cz = 0 so find a, b, and c.
	 */
	a = (y[0] * z[1]) - (y[1] * z[0]);
	b = (x[1] * z[0]) - (x[0] * z[1]);
	c = (x[0] * y[1]) - (x[1] * y[0]);
	systemLog("Consts (a, b, c): ("+a+", "+b+", "+c+")", 1);
	/* check */
	systemLog("Check: "+((a*x[0]) + (b*y[0]) + (c*z[0]))+" = "+
			   ((a*x[1]) + (b*y[1]) + (c*z[1]))+" = 0", 1);

	/* The small circle defines a plane by ax+by+cz = d, where d
	 * is the distance to the Great Circle of the orbit. 
	 * We want to know where that plane intersects z = z[0] 
	 * and the sphere x^2 + y^2 + z^2 = r^2
	 *
	 * make sure we're working with the original lat
	 */
	if(adjusted_lat != lat)
	    z[0] = radius * Math.sin(radians(lat));

	/* 
	 * Substituting z = z[0]:
	 *
	 * (a*x[2]) + (b*y[2]) + (c*z[0]) = d
	 * so
	 * (a*x[2]) + (b*y[2]) + (c*z[0]) - d = 0
	 * AND
	 * (x[2]*x[2]) + (y[2]*y[2]) + (z[0]*z[0]) = (radius * radius)
	 * so
	 * (x[2]*x[2]) + (y[2]*y[2]) + (z[0]*z[0]) - (radius * radius) = 0
	 *
	 * Two equations and two unknowns - Mathmatica says the equations
	 * are solved by these heinous equations.
	 *
	 * Define some constants to work with:
	 */ 
	d = getSCPConstant(lat, lon, true);
systemLog("small circle constant: "+d, 1);
systemLog("radius: "+radius, 1);
	
	planar_const = (c * z[0]) - d; 
	sphere_const = (z[0] * z[0]) - (radius * radius); 
	scale = ((a*a)+(b*b));
systemLog("planar: "+planar_const+" Spherical: "+sphere_const+" scale: "+scale, 1);
	
	rad = (-(Math.pow(a,4) * sphere_const) - (a*a*b*b*sphere_const) - 
	       (a*a*planar_const*planar_const));
	/* If this is negative taking the square root would require imaginary numbers.
	 * I'm guessing that probably means the two planes intersect outside the sphere.
	 */
systemLog("a: "+a+" a^4: "+Math.pow(a,4)+" sphere: "+sphere_const,1);
systemLog("b: "+b+" a^2: "+Math.pow(a,2)+" b^2 "+Math.pow(b,2)+" a*a*b*b*sphere: "+a*a*b*b*sphere_const,1);


	systemLog("c: "+c+" * z[0]: "+z[0]+" - d: "+ d+" = Plane: "+planar_const, 1);
	systemLog("GLSE Rad1: "+ rad+" Plane: "+planar_const, 1);
	if(rad >= 0) 
	    {
		rad = Math.sqrt(rad);
		systemLog("GLSE sqrt Rad1: "+ rad, 1);
	
		firstX = (b*b*planar_const)/scale;
systemLog("firstX: "+firstX,1);
		secondX = ((b*rad)/scale);
		systemLog("secondX: "+secondX,1);

		x[2] = ((-planar_const + firstX - secondX) / a);
		y[2] = ((-(b*planar_const) + rad) / scale);
		/* or */
		x[3] = ((-planar_const + firstX + secondX) / a);
		y[3] = ((-(b*planar_const) - rad) / scale);
		systemLog("GLSE LEFT (x,y) 2: ("+x[2]+", "+y[2]+")", 1);
systemLog("GLSE LEFT (x,y) 3: ("+x[3]+", "+y[3]+")", 1);
	    }
	
	/* 
	 * Now do the other edge.  The only difference being the small circle 
	 * defines a plane by ax+by+cz = -d.
	 */
	planar_const = (c * z[0]) + d; 
	rad = (-(Math.pow(a,4) * sphere_const) - (a*a*b*b*sphere_const) - 
	       (a*a*planar_const*planar_const));
	systemLog("OTHER planar: "+planar_const+" Spherical: "+sphere_const+" scale: "+scale, 1);
systemLog("a:"+a+" a^4:"+Math.pow(a,4)+" sphere: "+sphere_const,1);
systemLog("b:"+b+" a^2:"+Math.pow(a,2)+" b^2 "+Math.pow(b,2)+" a*a*b*b*sphere: "+a*a*b*b*sphere_const,1);
systemLog("a*a*planar_const*planar_const = " +a*a*planar_const*planar_const,1);
	/* If this is negative taking the square root would require imaginary numbers.
	 * I'm guessing that probably means the two planes intersect outside the sphere.
	 */
	systemLog("c: "+c+" * z[0]: "+z[0]+" + d: "+d+" = Plane: "+planar_const, 1);
	systemLog("GLSE Rad2: "+ rad +" Plane: "+planar_const, 1);
	if(rad >= 0) 
	    {
		rad = Math.sqrt(rad);
systemLog("GLSE sqrt Rad2: "+ rad, 1);
		firstX = (b*b*planar_const)/scale;
		systemLog("firstX: "+firstX,1);
		secondX = ((b*rad)/scale);
		systemLog("secondX: "+secondX,1);
		
		x[4] = ((-planar_const + firstX - secondX) / a);
		y[4] = ((-(b*planar_const) + rad) / scale);
		/* or */
		x[5] = ((-planar_const + firstX + secondX) / a);
		y[5] = ((-(b*planar_const) - rad) / scale);
		systemLog("GLSE RIGHT 4: ("+x[4]+", "+y[4]+")", 1);
		systemLog("GLSE RIGHT 5: ("+x[5]+", "+y[5]+")", 1);
	
	    }
	systemLog("Check: "+((a*x[2]) + (b*y[2]) + (c*z[0]))+" = "+
			   ((a*x[3]) + (b*y[3]) + (c*z[0]))+" = "+d, 1);
	systemLog("Check: "+((a*x[4]) + (b*y[4]) + (c*z[0]))+" = "+
			   ((a*x[5]) + (b*y[5]) + (c*z[0]))+" = "+d, 1);
	
	/* Find the two closest points. 
	 * If there are only two points it means we are above the 
	 * minimum inflection lat so we need to cut one of the edges
	 * at the declination lon. 
	 */
	if(0 == x[2] && 0 == y[2] && 0 == z[2])
	    {
		
		edgeLon[0] = (float)degrees(Math.atan2(y[4], x[4]));
		if(lonDistance(lon, edgeLon[0]) > lonDistance(lon, (float)eastDeclinationLon))
		    edgeLon[0] = (float)eastDeclinationLon;
		if(lonDistance(lon, edgeLon[0]) < lonDistance(lon, (float)westDeclinationLon))
		    edgeLon[0] = (float)westDeclinationLon;
		
		edgeLon[1] = (float)degrees(Math.atan2(y[5], x[5]));
		if(lonDistance(lon, edgeLon[1]) > lonDistance(lon, (float)eastDeclinationLon))
		    edgeLon[1] = (float)eastDeclinationLon;
		if(lonDistance(lon, edgeLon[1]) < lonDistance(lon, (float)westDeclinationLon))
		    edgeLon[1] = (float)westDeclinationLon;
		
		/****
		if(Math.abs(lonDistance(lon, edgeLon[0])) > 
		   Math.abs(lonDistance(lon, (float)declinationLon))) 
		    edgeLon[0] = (float)declinationLon;
		edgeLon[1] = (float)degrees(Math.atan2(y[5], x[5]));
		if(Math.abs(lonDistance(lon, edgeLon[1])) > 
		   Math.abs(lonDistance(lon, (float)declinationLon))) 
		    edgeLon[1] = (float)declinationLon;
		***/
		systemLog("TWO POINTS 45 -> "+crossingLon+" : ("+edgeLon[0]+", "+edgeLon[1]+")" ,1);
	    }
	
	else if(0 == x[4] && 0 == z[4] && 0 == z[4])
	    {
		edgeLon[0] = (float)degrees(Math.atan2(y[2], x[2]));
		
		if(lonDistance(lon, edgeLon[0]) > lonDistance(lon, (float)eastDeclinationLon))
		    edgeLon[0] = (float)eastDeclinationLon;
		if(lonDistance(lon, edgeLon[0]) < lonDistance(lon, (float)westDeclinationLon))
		    edgeLon[0] = (float)westDeclinationLon;
		
		edgeLon[1] = (float)degrees(Math.atan2(y[3], x[3]));
		if(lonDistance(lon, edgeLon[1]) > lonDistance(lon, (float)eastDeclinationLon))
		    edgeLon[1] = (float)eastDeclinationLon;
		if(lonDistance(lon, edgeLon[1]) < lonDistance(lon, (float)westDeclinationLon))
		    edgeLon[1] = (float)westDeclinationLon;
		
		/***
		    if(Math.abs(lonDistance(lon, edgeLon[0])) > 
		    Math.abs(lonDistance(lon, (float)declinationLon))) 
		    edgeLon[0] = (float)declinationLon;
		    edgeLon[1] = (float)degrees(Math.atan2(y[3], x[3]));
		    if(Math.abs(lonDistance(lon, edgeLon[1])) > 
		    Math.abs(lonDistance(lon, (float)declinationLon))) 
		    edgeLon[1] = (float)declinationLon;
		***/
		systemLog("TWO POINTS 23 -> "+crossingLon+" : ("+edgeLon[0]+", "+edgeLon[1]+")" ,1);
	    }
	/*
	 * Else we need the closest two points.  They are guaranteed to be 
	 * in the same hemisphere as the original point because ...
	 */
	else
	    { 
		for(i=2, j=0; i< 6 && j < 2; i++) {
		    tmpLon = degrees(Math.atan2(y[i], x[i]));
		    systemLog("i: "+i+" j: "+j+" tmp: "+tmpLon+
			      " Dist: "+lonDistance(lon, (float)tmpLon), 1);
		    if(Math.abs(lonDistance(lon, (float)tmpLon)) < 90.0) {
			edgeLon[j] = (float)tmpLon;
			j++;
		    }
		}
	    }
		       
	return true;

    } /* END getLonSwathEdges */

/*************************************************************************
 * Get the range of ascending nodal crossings for orbits that cover the 
 * given spherical polygon.
 *
 * @param spolygon spherical polygon to get coverage for.
 * @param ascending If true find the orbits that cover the given point 
 *                  on the ascending pass. <br>
 *    	            Else find the orbits that cover the given point 
 *                  on the descending pass.
 *              
 * @return LonRange[] contains the longitudinal ranges for crossings 
 *                  of orbits that cover the given corner points.<br>
 *                  There may be more than one range if the corner points 
 *                  are too far apart, but this method densifies the polygon 
 *                  to help prevent that from happening. <br>
 *                  Returns null if it fails utterly (e.g.: if your
 *                  polygon is completely outside the sensors viewing area.)
 *            
 **************************************************************************/

    public LonRange[] getAreaCrossingRange(SphericalPolygon spolygon, boolean ascending)
    {
	double max_rad;
	Point[] point_set;
	SphericalPolygon denseSP;
	
	systemLog("Get crossings using SP: ",3);
	systemLog("Get crossings using SP: perimeter = "+spolygon.perimeter_rad, 3);
	systemLog("Get crossings using SP: width = "+eq_swath_width, 3);
	systemLog("Get crossings using SP: in degrees = "+eq_swath_width_deg, 3);
	systemLog("Get crossings using SP: in radians = "+eq_swath_width_rad, 3);

	max_rad = eq_swath_width_rad * 0.9;
	denseSP = spolygon.densify(max_rad);
	
	return getAreaCrossingRange(denseSP.corner_point, ascending);

    }
/*************************************************************************
 * Get the range of ascending nodal crossings for orbits that cover the 
 * given Lat/Lon Bounding Box.
 *
 * @param llbox lat/lon bounding box to get coverage for.
 * @param ascending If true find the orbits that cover the given point 
 *                  on the ascending pass. <br>
 *    	            Else find the orbits that cover the given point 
 *                  on the descending pass.
 *              
 * @return LonRange[] contains the longitudinal ranges for crossings 
 *                  of orbits that cover the given corner points.<br>
 *                  There may be more than one range if the corner points 
 *                  are too far apart, but this method densifies the LLbox 
 *                  to help prevent that from happening. <br>
 *                  Returns null if it fails utterly (e.g.: if your
 *                  polygon is completely outside the sensors viewing area.)
 *            
 **************************************************************************/

    public LonRange[] getAreaCrossingRange(LatLonBoundingBox llbox, boolean ascending)
    {
	int density;
	Point[] point_set;
	
systemLog("Get crossings using LLBox: ",3);
systemLog("Get crossings using LLBox: perimeter = "+llbox.perimeter, 3);
systemLog("Get crossings using LLBox: twice = "+2*llbox.perimeter, 3);
systemLog("Get crossings using LLBox: width = "+eq_swath_width, 3);
systemLog("Get crossings using LLBox: in degrees = "+(eq_swath_width/110.0)*(radius / Re_km), 3);

density = (int) ((2*llbox.perimeter)/(eq_swath_width/110.0)*(radius / Re_km));
systemLog("Get crossings using LLBox: density = "+density, 3);
	systemLog("Get crossings using LLBox: old density = "+density, 3);
density = (int) ((llbox.perimeter)/((eq_swath_width)/((2*Math.PI*radius)/360)));

	systemLog("Get crossings using LLBox: old density = "+density, 3);
	//density = Math.max(density, 50);

systemLog("Get crossings using LLBox: new density = "+density, 3);
	density += 4;
systemLog("Get crossings using LLBox: density+4 = "+density, 3);

	point_set = llbox.toSphericalPolygon(density).corner_point;
systemLog("Get crossings using LLBox: DONE", 3);
	return getAreaCrossingRange(point_set, ascending);
	    	

    } /* END getAreaCrossingRange(llbox, ascending)


/*************************************************************************
 * Get the range of ascending nodal crossings for orbits that cover the 
 * given spherical polygon.
 *
 * @param lat[] Array for latitudes (in degrees) of the corner points.
 * @param lon[] Array for latitudes (in degrees) of the corner points.
 * @param ascending If true find the orbits that cover the given point 
 *                  on the ascending pass. <br>
 *    	            Else find the orbits that cover the given point 
 *                  on the descending pass.
 *              
 * @return LonRange[] contains the longitudinal ranges for crossings 
 *                         of orbits that cover the given corner points.<br>
 *                  There may be more than one range if the corner points 
 *                  are far apart. <br>
 *                  Returns null if it fails utterly (e.g.: if your
 *                  polygon is completely outside the sensors viewing area.)
 *            
 **************************************************************************/

    public LonRange[] getAreaCrossingRange(float[] lat, float[] lon, boolean ascending)
    {
	LonRange[] tmpRange, lonRanges;
	int i, j, k, oldLength;
	float[] lonRange = new float[2];
	lonRanges = new LonRange[lat.length];
	
	lonRanges[0] = getPointCrossingRange(lat[0], lon[0], ascending);
	
	systemLog("Range: ("+lonRange[0]+", "+lonRange[1]+")", 3);
	for(i=1; i<lat.length && i<lon.length; i++){
	    lonRanges[i] = getPointCrossingRange(lat[i], lon[i], ascending);
	    systemLog("Range "+i+": ("+lonRanges[i].min+", "+lonRanges[i].max+")", 3);
	}

	oldLength = 0;
	while(oldLength != lonRanges.length){
	    oldLength = lonRanges.length;
	    for(i=0; i<lonRanges.length; i++){
		if(null == lonRanges[i]) {
		    tmpRange = new LonRange[lonRanges.length-1];
		    for(k=0; k<i; k++){
			tmpRange[k] = lonRanges[k];
		    }
		    for(k=i+1; k<lonRanges.length; k++){
			tmpRange[k-1] = lonRanges[k];
		    }
		    lonRanges = tmpRange;
		    i--;
		}
		for(j=i+1; j<lonRanges.length; j++){
		    //systemLog("i: "+i+" j: "+j, 4);
		    if(lonRanges[i].meldRange(lonRanges[j])) {
			tmpRange = new LonRange[lonRanges.length-1];
			for(k=0; k<j; k++){
			    tmpRange[k] = lonRanges[k];
			}
			for(k=j+1; k<lonRanges.length; k++){
			    tmpRange[k-1] = lonRanges[k];
			}
			lonRanges = tmpRange;
			j--; 
		    }
		}
	    }
	}
	for(i=0; i<lonRanges.length; i++){
	    systemLog("Range "+i+" : ("+lonRanges[i].min+", "+ lonRanges[i].max+")", 2);
	}
	systemLog("-------------------------------------------------------------------------------",2);
	return lonRanges;
    }/* END getAreaCrossingRange */
    
    /*************************************************************************
     * Get the range of ascending nodal crossings for orbits that cover the 
     * given spherical polygon.
     *
     * @param lat[] Array for latitudes (in degrees) of the corner points.
     * @param lon[] Array for latitudes (in degrees) of the corner points.
     * @param ascending If true find the orbits that cover the given point 
     *                  on the ascending pass. <br>
     *    	            Else find the orbits that cover the given point 
     *                  on the descending pass.
     *              
     * @return LonRange[] contains the longitudinal ranges for crossings 
     *                         of orbits that cover the given corner points.<br>
     *                  There may be more than one range if the corner points 
     *                  are far apart. <br>
     *                  Returns null if it fails utterly (e.g.: if your
     *                  polygon is completely outside the sensors viewing area.)
     *            
     **************************************************************************/

    public LonRange[] getAreaCrossingRange(double[] lat, double[] lon, boolean ascending)
    {
	LonRange[] tmpRange, lonRanges;
	int i, j, k, oldLength;
	float[] lonRange = new float[2];
	lonRanges = new LonRange[lat.length];
	
	lonRanges[0] = getPointCrossingRange((float)lat[0], (float)lon[0], ascending);
	
	// systemLog("Range: ("+lonRange[0]+", "+lonRange[1]+")", 3);
	for(i=1; i<lat.length && i<lon.length; i++){
	    lonRanges[i] = getPointCrossingRange((float)lat[i], (float)lon[i], ascending);
	    systemLog("Range: ("+lonRanges[i].min+", "+lonRanges[i].max+")", 4);
	}
	
	oldLength = 0;
	while(oldLength != lonRanges.length){
	    oldLength = lonRanges.length;
	    for(i=0; i<lonRanges.length; i++){
		for(j=i+1; j<lonRanges.length; j++){
		    //systemLog("i: "+i+" j: "+j,4);
		    if(null != lonRanges[i] && lonRanges[i].meldRange(lonRanges[j])) {
			tmpRange = new LonRange[lonRanges.length-1];
			for(k=0; k<j; k++){
			    tmpRange[k] = lonRanges[k];
			}
			for(k=j+1; k<lonRanges.length; k++){
			    tmpRange[k-1] = lonRanges[k];
			}
			lonRanges = tmpRange;
			j--; 
		    }
		}
	    }
	}
	for(i=0; i<lonRanges.length; i++){
	    // systemLog("Range "+i+" : ("+lonRanges[i].min+", "+ lonRanges[i].max+")", 2);
	}
	systemLog("-------------------------------------------------------------------------------",2);
	return lonRanges;
    }/* END getAreaCrossingRange */


    /*************************************************************************
     * Get the range of ascending nodal crossings for orbits that cover the 
     * given spherical polygon.
     *
     * @param points[] Array for the corner points.
     * @param ascending If true find the orbits that cover the given point 
     *                  on the ascending pass. <br>
     *    	            Else find the orbits that cover the given point 
     *                  on the descending pass.
     *              
     * @return LonRange[] contains the longitudinal ranges for crossings 
     *                         of orbits that cover the given corner points.<br>
     *                  There may be more than one range if the corner points 
     *                  are far apart. <br>
     *                  Returns null if it fails utterly (e.g.: if your
     *                  polygon is completely outside the sensors viewing area.)
     *            
     **************************************************************************/

    public LonRange[] getAreaCrossingRange(Point[] points, boolean ascending)
    {
	LonRange[] tmpRange, lonRanges;
	int i, j, k, oldLength;
	float[] lonRange = new float[2];
	lonRanges = new LonRange[points.length];
	systemLog("BEGIN: getAreaCrossingRange - "+points.length, 4);
	lonRanges[0] = getPointCrossingRange(points[0], ascending);
	
	systemLog("Range: ("+lonRange[0]+", "+lonRange[1]+")", 3);
	for(i=1; i<points.length; i++){
	    lonRanges[i] = getPointCrossingRange(points[i], ascending);
	    systemLog("Range: ("+lonRanges[i].min+", "+lonRanges[i].max+")", 4);
	    systemLog(i+": "+points[i].toString(), 4);
	}
	
	oldLength = 0;
	while(oldLength != lonRanges.length){
	    oldLength = lonRanges.length;
	    for(i=0; i<lonRanges.length; i++){
		for(j=i+1; j<lonRanges.length; j++){
		    //systemLog("i: "+i+" j: "+j,4);
		    if(null != lonRanges[i] && lonRanges[i].meldRange(lonRanges[j])) {
			tmpRange = new LonRange[lonRanges.length-1];
			for(k=0; k<j; k++){
			    tmpRange[k] = lonRanges[k];
			}
			for(k=j+1; k<lonRanges.length; k++){
			    tmpRange[k-1] = lonRanges[k];
			}
			lonRanges = tmpRange;
			j--; 
		    }
		}
	    }
	}
	for(i=0; i<lonRanges.length; i++){
	    systemLog("Range "+i+" : ("+lonRanges[i].min+", "+ lonRanges[i].max+")", 2);
	}
	systemLog("-------------------------------------------------------------------------------",2);
systemLog("END: getAreaCrossingRange", 2);
	
	return lonRanges;
    }/* END getAreaCrossingRange */
    

    /*************************************************************************
     * Get the range of ascending nodal crossings for orbits that cover the given point.
     *
     * @param point - The given point.
     * @param ascending - If true find the orbits that cover the 
     *     	given point on the ascending pass.
     *    	Else find the orbits that cover the given point 
     *                      on the descending pass.
     *              
     * @return lonRange - longitude range of crossings.<br>
     *            Returns null if it fails utterly (e.g.: if your
     *            point is completely outside the sensors viewing area.)
     **************************************************************************/
    
    public LonRange getPointCrossingRange(Point point, boolean ascending)
    { 
	//System.out.println("GET: "+point.toString());
	return (getPointCrossingRange((float)point.lat, (float)point.lon, ascending));
    }

    /*************************************************************************
     * Get the range of ascending nodal crossings for orbits that cover the given point.
     *
     * @param lat Latitude of the given point in degrees.
     * @param lon Longitude of the given point in degrees.
     * @param ascending If true find the orbits that cover the 
     *     	given point on the ascending pass.
     *    	Else find the orbits that cover the given point 
     *                      on the descending pass.
     *              
     * @return lonRange - longitude range of crossings.<br>
     *            Returns null if it fails utterly (e.g.: if your
     *            point is completely outside the sensors viewing area.)
     **************************************************************************/
    
    public LonRange getPointCrossingRange(float lat, float lon, boolean ascending)
    {
	float[] nodalCrossing = new float[3];
	float[] edgeLon = new float[2];
	float lonDist = 0;
	float[] tmpLon = new float[2];
	LonRange lonRange;

	/** Neither of these is quite right - But what is?
	   if(Math.abs(lat) > maxCoverageLat)
	   return null;
	**/
	systemLog("GET: ("+lat+", "+lon+")", 4);
	if(Math.abs(lat) > maxCoverageLat){
	    lat = (float)(inflection_lat+ 0.00001) * (Math.abs(lat)/lat);
	}

	if(Math.abs(lat) > totalCoverageLat){
	    lonRange = new LonRange(-180, 180);
	    return lonRange;
	}

	getLonSwathEdges(lat, lon, ascending, edgeLon);
	systemLog("\nLat: "+lat+" First: "+edgeLon[0]+" Second: "+edgeLon[1], 4);
	
	if(lat > inflection_lat && lat < inflection_max_lat)
	    lat = (float)(inflection_lat - 0.00001);
	
	if(lat < -inflection_lat && lat > -inflection_max_lat)
	    lat = (float)(-inflection_lat + 0.00001);
		
	lonDist = lonDistance(lon, edgeLon[0]);
	nodalCrossing[0] = getNodalCrossing(lat, lon - lonDist, ascending, true);
	systemLog("Dist: "+lonDist+" Lon: "+(lon - lonDist)+" Cross: "+nodalCrossing[0], 4);

	nodalCrossing[1] = getNodalCrossing(lat, lon, ascending, true);
	systemLog("Dist: "+"0"+" Lon: "+lon+" Cross: "+nodalCrossing[1], 1);

	lonDist = lonDistance(lon, edgeLon[1]);
	nodalCrossing[2] = getNodalCrossing(lat, lon - lonDist, ascending, true);
	systemLog("Dist: "+lonDist+" Lon: "+(lon - lonDist)+" Cross: "+nodalCrossing[2], 4);

	tmpLon[0] = nodalCrossing[1] + lonDistance(nodalCrossing[1], nodalCrossing[0]);
	tmpLon[1] = nodalCrossing[1] + lonDistance(nodalCrossing[1], nodalCrossing[2]);
	
	lonRange = new LonRange(Math.min(tmpLon[0], tmpLon[1]), Math.max(tmpLon[0], tmpLon[1]));
	systemLog("Point: ("+lat+", "+lon+") Range: ("+lonRange.min+", "+lonRange.max+")", 4);
	return lonRange;
    } /* END getCrossingRange */



    /*************************************************************************
     * Get the cross track edge points of the swath with nadir at the given point.
     *
     * @param lat Latitude of the given point in degrees.
     * @param lon Longitude of the given point in degrees.
     * @param edgeLat[] Array for the output latitudes in degrees.  
     *                  edgeLat[0] is the left lat 
     *                  edgeLat[1] is the right lat.   
     *                      
     * @param edgeLon[] Array for the output longitudes in degrees.  
     *                  edgeLon[0] is the left lon
     *                  edgeLon[1] is the right lon.   
     *    
     * @param ascending If true find edges for the orbit with nadir  
     *     	passing through the given point on the ascending pass.<br>
     *    	Else find the edges for the orbit with nadir 
     *     	passing through the given point on the descending pass.
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
    
    void getCrossSwathEdges(float lat, float lon, 
			    float[] edge_lat, float[] edge_lon, 
			    boolean ascending)
    {
	double theta, numerator, denominator, distance, adjustment;
	double W, phi, lam, dlam, phin, lamn;
	double sin_A, sin_W, cos_W, sin_phin, cos_phin;
	double heading, dist_km;
	float left_edge_lat, left_edge_lon, right_edge_lat, right_edge_lon;
	
	/*
	 *    	Use law of sines to find theta (the angle between the  
	 *		longitude and the track)
	 */
	theta = Math.asin(Math.sin(radians(declination)) / 
			  Math.sin(radians(90 - lat)));
	if(ascending) theta = Math.PI - theta;
	systemLog( "inc: "+declination+" r(inc): "+radians(declination)+" lat: "+
	   lat+" r(90-lat): "+radians(90 - lat), 1);
systemLog("ascending="+ascending+": theta = " + theta + "\n", 1); 
	/*
	 *  	Get lat, lon for left and right swath edges.
	 *
	 *	(phin, lamn) nadir coords. in radians
	 *	A = spacecraft heading
	 */
	
	heading = degrees(Math.PI - theta);
	lamn = radians(lon);
	phin = radians(lat);
	sin_phin = Math.sin(phin);
	cos_phin = Math.cos(phin);
	sin_A = Math.sin(radians(heading));
	if (heading > 90) sin_A = -sin_A;
	
	/*
	 *	W = central angle between nadir and left sample
	 */
	if (heading > 90) dist_km = eq_swath_width / 2.0;
	else dist_km = -eq_swath_width / 2.0;
	W = dist_km/radius;
	sin_W = Math.sin(W);
	cos_W = Math.cos(W);
	
	/*
	 *	phi = left swath edge latitude in radians
	 *	lam = left swath edge longitude in radians
	 */
	phi = Math.asin( sin_phin*cos_W + cos_phin*sin_W*sin_A );
	dlam = Math.acos( (cos_W - sin_phin*Math.sin(phi)) / (cos_phin*Math.cos(phi)) );
	lam = lamn + (dist_km >= 0 ? dlam : -dlam);
	
	left_edge_lat = (float)degrees(phi);
	left_edge_lon = (float)degrees(lam);
	normalize(left_edge_lon);
	
	/*
	 *	W = central angle between nadir and right sample
	 */
	dist_km = -dist_km;
	W = dist_km/radius;
	sin_W = Math.sin(W);
	cos_W = Math.cos(W);
	
	/*
	 *	phi = right swath edge latitude in radians
	 *	lam = right swath edge longitude in radians
	 */
	phi = Math.asin( (sin_phin * cos_W) + (cos_phin * sin_W * sin_A) );
	dlam = Math.acos( (cos_W - (sin_phin * Math.sin(phi))) / (cos_phin * Math.cos(phi)) );
	lam = lamn + (dist_km >= 0 ? dlam : -dlam);
	
	right_edge_lat = (float)degrees(phi);
	right_edge_lon = (float)degrees(lam);
	normalize(right_edge_lon);
	
	/* load the array - we're done */
	edge_lat[0] = left_edge_lat;
	edge_lon[0] = left_edge_lon;
	edge_lat[1] = right_edge_lat;
	edge_lon[1] = right_edge_lon;
	
    }
    
    
    /*************************************************************************
     * Get the distance from the great circle defined by nadir to the small circle 
     * defined by the swath edge in Cartesian space.
     *
     * It's all cartesian - so it's actually the distance between the two planes -
     * and it's constant for any given orbit, so we only do this once, at initialization.<br>
     *
     * @param lat Latitude of the given point in degrees.
     * @param lon Longitude of the given point in degrees.
     * @param ascending If true find edges for the orbit with nadir  
     *     	passing through the given point on the ascending pass.
     *    	Else find the edges for the orbit with nadir 
     *     	passing through the given point on the descending pass.
     *            
     * @return Distance in Cartesian space.
     *
     ************************************************************************/
    
    private double getSCPConstant(float lat, float lon, boolean ascending)
    {
	float crossingLon;
	float[] edgeLat, edgeLon;
	double[] x, y, z;

	double a,b,c,d;
	double numerator, denominator;

	/*
	 * Get the points we need: 
	 * one more point on nadir to define the Great circle,
	 * and one edge point to get the distance. 
	 */
	crossingLon = getNodalCrossing(lat, lon, ascending, false);
	edgeLat = new float[2];
	edgeLon = new float[2];
	getCrossSwathEdges(lat, lon, edgeLat, edgeLon, ascending);
	systemLog("West: "+edgeLon[0]+" Center: "+lon+" East: "+edgeLon[1], 1);

	/* convert an edge point to cartesian coordinates */
	x = new double[4];
	y = new double[4];
	z = new double[4];

	/* given point */
	x[0] = radius * Math.cos(radians(lon)) * Math.cos(radians(lat));
	y[0] = radius * Math.sin(radians(lon)) * Math.cos(radians(lat));
	z[0] = radius * Math.sin(radians(lat));
	systemLog("Given (x,y,z): ("+x[0]+", "+y[0]+", "+z[0]+")", 1);
	
	/* nodal crossing */
	x[1] = radius * Math.cos(radians(crossingLon)) * Math.cos(0.0);
	y[1] = radius * Math.sin(radians(crossingLon)) * Math.cos(0.0);
	z[1] = radius * Math.sin(0.0); 
	systemLog("Crossing (x,y,z): ("+x[1]+", "+y[1]+", "+z[1]+")", 1);
	
	/* edge point */
	x[2] = radius * Math.cos(radians(edgeLon[0])) * Math.cos(radians(edgeLat[0]));
	y[2] = radius * Math.sin(radians(edgeLon[0])) * Math.cos(radians(edgeLat[0]));
	z[2] = radius * Math.sin(radians(edgeLat[0]));
systemLog("L edge (x,y,z): ("+x[2]+", "+y[2]+", "+z[2]+")", 1);
	
	/* edge point */
	x[3] = radius * Math.cos(radians(edgeLon[1])) * Math.cos(radians(edgeLat[1]));
	y[3] = radius * Math.sin(radians(edgeLon[1])) * Math.cos(radians(edgeLat[1]));
	z[3] = radius * Math.sin(radians(edgeLat[1]));
systemLog("R edge (x,y,z): ("+x[3]+", "+y[3]+", "+z[3]+")", 1);
	
	/* 
	 * The great circle defines a plane by ax+by+cz = 0
	 *  so find a, b, and c
	 */
	a = (y[0] * z[1]) - (y[1] * z[0]);
	b = (x[1] * z[0]) - (x[0] * z[1]);
	c = (x[0] * y[1]) - (x[1] * y[0]);
systemLog("Consts (a, b, c): ("+a+", "+b+", "+c+")", 1);
	
	/*  The edge point is on a small circle parallel to the Great Circle
	 *  The small circle defines a plane by ax+by+cz = d 
	 *  Find d.
	 */
	d = (a*x[2])+(b*y[2])+(c*z[2]);
systemLog("Small circle constant is: "+d, 1);	
	systemLog("Cartesian Distance is: "+d/Math.sqrt(Math.pow(a,2)+Math.pow(b,2)+Math.pow(c,2)), 1);
	d = (a*x[3])+(b*y[3])+(c*z[3]);
	systemLog("Other Small circle constant is: "+d, 1);
	
	return (d);

} /* END getSCPConstant */


    /*********************************
     *  Misc. Utility functions.
     *********************************/
    
    /* 
     *  lonDistance - find the directed distance from one longitude
     *  to another.
     */
    public float lonDistance(float lon1, float lon2)
    {
	return(shortestLonDistance(lon1, lon2));
    }

    public float directedLonDistance(float lon1, float lon2)
    {
	/* If the lons are different signs we may be crossing the dateline
	 * normalize the dateline outa there.
	 */
	while((lon1/Math.abs(lon1)) != (lon2/Math.abs(lon2))){
	    lon1 += 360.0;
	    lon2 += 360.0;
	}
	
	/*  Find the distance and normalize it
	 */
	      float distance = Math.abs(lon1 - lon2);
	while(distance >= 360.0)
	    distance -= 360.0;
	while(distance < 0.0)
	    distance += 360.0;

	/*  And give it a sign
	 */
	if(lon1 <= lon2)
	    distance = Math.abs(distance);
	else 
	    distance = -Math.abs(distance);
	
	return(distance);
    } 

    public float shortestLonDistance(float lon1, float lon2)
    {
	/* If the lons are different signs we may be crossing the dateline
	 * normalize the dateline outa there.
	 */
	while((lon1/Math.abs(lon1)) != (lon2/Math.abs(lon2))){
	    lon1 += 360.0;
	    lon2 += 360.0;
	}
	
	/*  Find the distance and normalize it
	 */
	      float distance = Math.abs(lon1 - lon2);
	while(distance >= 360.0)
	    distance -= 360.0;
	while(distance < 0.0)
	    distance += 360.0;
	

	/*  And give it a sign
	 */
	if(lon1 <= lon2)
	    distance = Math.abs(distance);
	else 
	    distance = -Math.abs(distance);
	
	if(distance > 180.0)
	    distance -= 360;
	if(distance < -180.0)
	    distance += 360;

	return(distance);
    }
    
    public float circularLat(float lat, boolean asc)
    {
	if (!asc) lat = 180 - lat;
	while (lat < 0) lat += 360;
	return lat;
    }

    public float circularLat(double lat, boolean asc)
    {
	if (!asc) lat = 180 - lat;
	while (lat < 0) lat += 360;
	return (float)lat;
    }
    
    public float standardLat(double lat)
    {
	if(lat > 90.0 && lat < 270.0)
	    lat = 180.0 - lat;
	else if(lat >= 270.0)
	    lat = lat - 360.0;
	return (float) lat;
    }
    public String toString()
    {
	String string = new String("Orb[declination: "+declination+" period: "+period+
				   " width: "+eq_swath_width+" in degrees:"+eq_swath_width_deg);
	return string;
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
    protected void systemLog(String msg) {
	System.out.println(msg);
    }
    

} /* END Orbit class */
