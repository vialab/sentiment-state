package nsidc.spheres;

import java.lang.*;
/*******************************************************************
 * SmallCircleArc class - provides services and information relating 
 * to a small circle arc.
 * <P>
 * A small circle arc is a piece of a small circle.  For purposes of 
 * this package we always define small circles relative the parallel  
 * great circle.  This seems reasonable since the small circles used  
 * in this package are all edges of orbits and hence a known distance  
 * from the great circle defined by nadir.
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
 * @see nsidc.spheres.SmallCircle
 *******************************************************************/

public class SmallCircleArc extends SmallCircle
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    

    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    
    /*******************************************************************
     * Intersect methods.
     *******************************************************************/
    

    /**
     * Determine if this small circle arc intersects another.
     *
     * @param other Another small circle arc.
     *
     * @result true - if the two arcs intersect.<br>
     *         false - if the two arcs do not intersect.
     */
    public boolean intersects(SmallCircleArc other)
    {
	
	return false;
    }




} /* END SmallCircleArc class */
