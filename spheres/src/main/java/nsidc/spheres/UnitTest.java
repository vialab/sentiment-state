package nsidc.spheres;

import java.lang.*;

/**************************************************************************
 * UnitTest class - Implement tests of the Spheres package.
 * <P>
 * This is by no means an exaughstive set of tests.  Tests are implemented as needed.
 * I generally prefer to test in within an application so the test here are mostly
 * for methods I don't actually use in an interface.     
 * <P>
 * @author 
 * 08-September-2003 R.Swick swick@nsidc.org 303-492-6069 <br>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 ***************************************************************************/


/******************************************************************
 * Important orbit information:
 *
 * Adeos-II, AMSR: 8.62, 100.93, 1445
 * Aqua,   AMSR-E: 8.2, 98.9, 1600
 *
 **************************************************************************/


public class UnitTest extends Sphere
{

    /*******************************************************************
     * instance variables
     *******************************************************************/
    Sphere sphere = new Sphere();

    /* 5 = off, 0 = full on */
    private final int logLevel = 0;

    
/*******************************************************************
     * The constructors.
     *******************************************************************/
    /**
     * Empty constructor 
     */
    protected UnitTest()
    {
	testPolyCompare();
	//testEdgeToCrossing();
	
	//testLLBtoSP();
	//testLLBDensifier();
	//testSPDensifier();
	//testSPCrossings();
	//testLLBBCrossings();
	//testPointRange();
	//testSPContainment();
    }

    public static void main(String[] argv)
    {
	System.out.println("Begin Unit test");

	UnitTest ut = new UnitTest();
	//testLLBtoSP();

	System.out.println("End Unit test");
    }



    /*********************************
     *   Specific tests.
     *********************************/

    /**
     * get some AMSR crossings given a pair of edge points.
     **/
     public void testEdgeToCrossing()
    {
	float lon, corrected;
double minutes, seconds;
	int i;
	Point point1[] = {new Point(10.2, -127.13),
			  new Point(67.08, 153.65),
			  new Point(39.08,  132.91),
			   new Point(49.21, -1.77),
			   new Point(-53.65, -157.89),
			   new Point(-38.83, 149.27),
			  new Point(-53.02, 129.11),
			  new Point(28.91, -23.27),
			  new Point(52.46, 155.98),
			  new Point(38.86, 132.93)};

	Point point2[] = {new Point(7.61, -143.98),
			  new Point(61.17, 116.35),
			  new Point(35.81, 111.97),
			   new Point(53.25, -28.22),
			   new Point(-49.43, -130.84),
			   new Point(-35.53, 170.40),
			  new Point(-57.54, 158.62),
			  new Point(26.02,  -42.01),
			  new Point(48.44, 129.99),
			  new Point(35.63, 112.06)};
	String files[] = {new String("filenameA"), 
			  new String("A2AMS03012528MA"),  
			  new String("A2AMS03012536MD"), 
			   new String("A2AMS03012631MD"), 
			   new String("A2AMS03012821MA"), 
			   new String("A2AMS03012829MA"), 
			  new String("A2AMS03060801MD"), 
			  new String("A2AMS03070757MA"), 
			  new String("A2AMS03070828MA"), 
			  new String("A2AMS03070832MA")};
	
	GreatCircleArc test_arc = new GreatCircleArc (10.2, -127.13, 7.61, -143.98);
	Point test_center = test_arc.center();
	Point crossing = new Point(0,0);
	//systemLog("CENTER  "+ test_center.toString(), 1);
	Orbit test_orbit = new Orbit(8.62, 100.93, 1445);
	

	for (i=0; i<files.length; i++) {
	    test_arc = new GreatCircleArc (point1[i], point2[i]);
	    test_center = test_arc.center();
	    systemLog("\nFILE    "+ files[i], 1);
	    systemLog("CENTER  "+ test_center.toString(), 1);
	    lon = test_orbit.getNodalCrossing((float)test_center.lat, 
					      (float)test_center.lon, true, true);
	    crossing = new Point(0.0, lon);
	    test_arc = new GreatCircleArc (test_center, crossing);
	    minutes = test_orbit.period * test_arc.arc_length_deg /360.0;
	    seconds = (minutes * 60) % 60;
	    systemLog("ASCENDING  "+lon+" Distance: "+ test_arc.arc_length_deg+" Minutes: "+(int)minutes+" Seconds: "+(int)seconds, 1);


	    lon = test_orbit.getNodalCrossing((float)test_center.lat, 
					      (float)test_center.lon, false, true);
	    
	    crossing = new Point(0.0, lon);
	    test_arc = new GreatCircleArc (test_center, crossing);
	    minutes = test_orbit.period * test_arc.arc_length_deg /360.0;
	    seconds = (minutes * 60) % 60;
	    //systemLog("DESCENDING (previous asc) "+lon, 1);	
	    //systemLog("DESCENDING (included des) "+corrected+" Distance: "+ test_arc.arc_length_deg+" Minutes: "+minutes, 1);	
	    systemLog("UNCORRECTED "+lon+" Distance: "+ test_arc.arc_length_deg+" Minutes: "+(int)minutes+" Seconds: "+(int)seconds, 1);

	    corrected = normalize(lon + (float)180.0 - (float)(test_orbit.period * 15.0 / 120.0)); 

	    crossing = new Point(0.0, corrected);
	    test_arc = new GreatCircleArc (test_center, crossing);
	    minutes = test_orbit.period * test_arc.arc_length_deg /360.0;
	    seconds = (minutes * 60) % 60;
	    //systemLog("DESCENDING (previous asc) "+lon, 1);	
	    //systemLog("DESCENDING (included des) "+corrected+" Distance: "+ test_arc.arc_length_deg+" Minutes: "+minutes, 1);	
	    systemLog("DESCENDING "+corrected+" Distance: "+ test_arc.arc_length_deg+" Minutes: "+(int)minutes+" Seconds: "+(int)seconds, 1);

	    	
}
    }
public void testPolyCompare()
    {
	LatLonBoundingBox interestBox, compareBox;
	SphericalPolygon interestSP;
	int i;

	interestBox = new LatLonBoundingBox(-5.0, 5.0, -5.0, 5.0);
	interestSP = interestBox.toSphericalPolygon(50);
interestSP = interestBox.toSphericalPolygon(100);
compareBox = new LatLonBoundingBox(-9.0, -2.0, 2.0, 9.0);

	for(i=0; i<10; i++)
	    {
		
		compareBox = new LatLonBoundingBox(-9.0, -2.0, 2.0, 9.0);
		systemLog("UL: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(2.0, 9.0, 2.0, 9.0);
		systemLog("UR: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(2.0, 9.0, -2.0, -9.0);
		systemLog("LR: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-9.0, -2.0, -9.0, -2.0);
		systemLog("LL: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-9.0, 9.0, 2.0, 9.0);
		systemLog("T: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-9.0, 9.0, -9.0, -2.0);
		systemLog("B: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-9.0, -2.0, -9.0, 9.0);
		systemLog("L: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(2.0, 9.0, -9.0, 9.0);
		systemLog("R: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-2.0, 2.0, -2.0, 2.0);
		systemLog("I: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		compareBox = new LatLonBoundingBox(-9.0, 9.0, -9.0, 9.0);
		systemLog("O: "+interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		
		compareBox = new LatLonBoundingBox(-9.0, -6.0, 6.0, 9.0);
		systemLog(interestSP.overlaps(compareBox.toSphericalPolygon(50))+" ",3);
		
	    }
    }


  public void testPointRange()
    {
	// Pittsburgh example.

	Point test_point = new Point(40.5, -80.22);
	Orbit test_orbit = new Orbit(8.78, 101, 1400);
systemLog("ASCENDINGASCENDINGASCENDINGASCENDING", 1); 
	test_orbit.getPointCrossingRange(test_point, true);
	systemLog("DESCENDINGDESCENDINGDESCENDINGDESCENDING", 1); 
	test_orbit.getPointCrossingRange(test_point, false);
				     
    }

    public void testSPContainment()
    {
	LatLonBoundingBox box;
	SphericalPolygon sp, dsp;
	
	Point test_point;
double lat;

	box = new LatLonBoundingBox(40.0, 60.0, -135.0, -105.0);
	systemLog(box.toString(), 3); 
	sp = box.toSphericalPolygon(10);
	systemLog(sp.toString(), 3); 
    
	    
	/* definitely in */
	test_point = new Point(45, -130);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 

	systemLog("EXTERNAL: ", 3); 
	/* the external point itself */
	test_point = new Point(sp.external_point.lat, sp.external_point.lon);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	
	test_point = new Point(sp.external_point.x, sp.external_point.y, sp.external_point.z);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	test_point.reset(sp.external_point.x, sp.external_point.y, sp.external_point.z);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3);

	/* corners*/
	systemLog(" Corners: ", 3); 
	test_point = new Point(40.0, -135.0);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	test_point = new Point(60.0, -135.0);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	test_point = new Point(40.0, -105.0);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	test_point = new Point(60.0, -105.0);
	systemLog(test_point.toString()+" is inside? "+
		  sp.contains(test_point), 3); 
	
	/* along a longitude edge */
	systemLog("Longitude edge: ", 3);
	for(lat = 30.0; lat < 70.0; lat +=1.0)
	    { 
		test_point = new Point(lat, -135.0);
		systemLog(test_point.toString()+" is inside? "+
			  sp.contains(test_point), 3); 
	    }
    }



	public void testSPDensifier()
    {

	LatLonBoundingBox box;
	SphericalPolygon sp, dsp;

	box = new LatLonBoundingBox(40.0, 60.0, -135.0, -105.0);
	sp = box.toSphericalPolygon(5);

	dsp = sp.densify(sphere.radians(1.0));
	systemLog(sp.toString(), 3); 
	systemLog(dsp.toString(), 3); 
    }


	public void testLLBtoSP()
	    {
		
	LatLonBoundingBox box;
	SphericalPolygon sp;

	box = new LatLonBoundingBox(40.0, 60.0, -135.0, -105.0);

	sp = box.toSphericalPolygon(20);
	systemLog(sp.toString(), 3); 
    }
 
    public void testLLBBCrossings()
    {

	LatLonBoundingBox box;
	SphericalPolygon sp, dsp;
	Orbit orbit;
	double i, adjustment, swath_width = 1445.0;
	int density;
	double ROTATION_RATE = (2.0*Math.PI/(24.0*60.0));
	LonRange[] range;
	i = swath_width;

	//box = new LatLonBoundingBox(-10.0, 10.0, -10.0, 20.0);
	//box = new LatLonBoundingBox(latmin, latmax, lonmin, lonmax);
	box = new LatLonBoundingBox(34.85, 38.19, -120.30, -117.97);
	//box = new LatLonBoundingBox(36.00, 39.18, -109.16, -105.56);
	//box = new LatLonBoundingBox(38.5, 42.00, -108.5, -103.50);
	//box = new LatLonBoundingBox(25.00, 35.00, 75.00, 95.00);
		systemLog(box.toString(), 3); 
		orbit = new Orbit(98.2, 100.0, swath_width);
		systemLog(orbit.toString(), 3); 
		range = orbit.getAreaCrossingRange(box, true);
		systemLog(box.toString(), 3); 
		systemLog("There are "+range.length+" ranges using "+i, 3); 
		systemLog("The first asc is "+range[0].toString(), 3);
		/**********/
		range = orbit.getAreaCrossingRange(box, false);
		systemLog("There are "+range.length+" ranges using "+i, 3); 
		systemLog("The first des is "+range[0].toString(), 3);
		
		//try to adjust for des included.
		adjustment = orbit.period * Math.PI * ROTATION_RATE / (2 * Math.PI);
		range[0].min = normalize(range[0].min - degrees(adjustment) + 180.0);
		range[0].max = normalize(range[0].max - degrees(adjustment) + 180.0);
	systemLog(box.toString(), 3); 
		systemLog("INCLUDED des is "+range[0].toString(), 3);
		/******************/
		//orbit.pole_to_pole = false;
		//range = orbit.getAreaCrossingRange(box, true);
		//systemLog("There are "+range.length+" ranges using "+i, 3); 
		//systemLog("The first asc is "+range[0].toString(), 3);

    }


    public void testSPCrossings()
    {

	LatLonBoundingBox box;
	SphericalPolygon sp, dsp;
	Orbit orbit;
	double i, swath_width = 1445.0;
	int density;
	
	LonRange[] range;

	for(i=swath_width; i>0; i-=1500)
	    {
		box = new LatLonBoundingBox(-10.0, 10.0, -10.0, 20.0);
		systemLog(box.toString(), 3); 
		orbit = new Orbit(98.2, 98.8, i);
		systemLog(orbit.toString(), 3); 
		sp = box.toSphericalPolygon(10);
		systemLog(sp.toString(), 3); 
		
		//range = orbit.getAreaCrossingRange(sp, true);
		//systemLog("There are "+range.length+" ranges using "+i, 3); 
		//systemLog("The first asc is "+range[0].toString(), 3); 
	   //range = orbit.getAreaCrossingRange(sp, false);
	   //systemLog("There are "+range.length+" ranges using "+i, 3); 
	   //systemLog("The first des is "+range[0].toString(), 3); 
	    

		Point sp_points[] = new Point[4];
		sp_points[0] = new Point(-10.0, 10.0);
		sp_points[1] = new Point(10.0, 20.0);
		sp_points[2] = new Point(10.0, -10.0);
		sp_points[3] = new Point(-10.0, -10.0);
		range = orbit.getAreaCrossingRange(sp, true);
		systemLog("There are "+range.length+" ranges using "+i, 3); 
		systemLog("The first asc is "+range[0].toString(), 3);
	    }
    }
public void testLLBDensifier()
    {

	LatLonBoundingBox box;
	Orbit orbit;
	double i, swath_width = 1180.0;
	int density;
	
	LonRange[] range;

	box = new LatLonBoundingBox(40.0, 60.0, -135.0, -105.0);
	systemLog(box.toString(), 3); 
	orbit = new Orbit(8.78, 101.0, swath_width);
	systemLog(orbit.toString(), 3); 

	for(i=swath_width; i>0; i-=250)
	    {
		density = (int) ((box.perimeter)/(float)i/((2.0*Math.PI*sphere.Re_km)/360.0));
		systemLog("Width: "+i+" density: "+density+" circ: "+((2.0*Math.PI*sphere.Re_km)/360)+
			  " perimeter: "+box.perimeter+" i/c: "+(float)i/((2.0*Math.PI*sphere.Re_km)/360.0)+
" float: "+((box.perimeter)/((float)i/((2.0*Math.PI*sphere.Re_km)/360.0))), 3); 
	    }

	//range = orbit.getAreaCrossingRange(box, true);
	//systemLog("There are "+range.length+" ranges.", 3); 

    }

    /*********************************
     *  Misc. Utility functions.
     *********************************/
    
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
    


}  /* end UnitTest class */
