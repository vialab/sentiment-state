package nsidc.spheres;

import java.lang.*;
/********************************************************************
 * LatRange class - provide services and information relating to a 
 * latitude range.  At the moment it's all fairly basic, but at some 
 * point we may need to add functionality for circular latitudes.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author 
 * 08-September-2000 R.Swick swick@chukchi.colorado.edu 303-492-6069 <br>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 ********************************************************************/
public class LatRange extends Sphere
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     * minimum latitude in range.
     */
    public double min = -90.0;
    
    /**
     * maximum latitude in range.
     */
    public double max = 90.0;
     
    /**
     * temporary variable.
     */
    private double tmp_lat = 90.0;
    
    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    public LatRange(double givenMin, double givenMax){
	min = givenMin;
	max = givenMax;
	
	if(max < min){
	    tmp_lat = max;
	    max = min;
	    min = tmp_lat;   
	}
    }
    
    public LatRange(float givenMin, float givenMax){
	min = (double)givenMin;
	max = (double)givenMax;
	
	if(max < min){
	    tmp_lat = max;
	    max = min;
	    min = tmp_lat;   
	}
    }
    
   
    /**
     * Determine if the given latitude is within the range.
     */
    public boolean contains(float lat){
	return(contains((double)lat));
    }
    
    /**
     * Determine if the given latgitude is within the range.
     */
    public boolean contains(double lat){
	if(min <= lat && lat <= max)
	    return true;
	return false;
    }    
    
    /**
     * Determine if this range overlaps another.
     */
    public boolean overlaps(LatRange other_range)
    {
	/* 
	 * Check for nothing  
	 */
	if(null == other_range)
	    return false;

	/*  
	 * Check for everything
	 */
	if((max - min) >= 180.0)
	    return true;
	
	if((other_range.max - other_range.min) >= 180.0)
	    return true;

	/*  
	 * Check for any overlap.
	 */	    
	if((min <= other_range.min && other_range.min <= max) ||
	   (min <= other_range.max && other_range.max <= max) ||
	   (other_range.min <= min && min <= other_range.max) ||
	   (other_range.min <= max && max <= other_range.max)) 
	    return true;

	/*  
	 * Else the ranges do not overlap.
	 */
	return false;
    }

    /**
     * Combine two ranges if possible.  
     * <br>
     * If the two ranges overlap this range is adjusted to include the 
     * other and true is returned. The other range can then be discarded 
     * by the calling method.
     * <br>
     * If the two ranges do not overlap no changes are made and 
     * false is returned.
     */
    public boolean meldRange(LatRange other_range){
	
	/*   Check for nothing    
	 */
	if(null == other_range)
	    return true;
	
	if(Double.isNaN(other_range.min) || Double.isNaN(other_range.max))
	    return false;
	
	if(Double.isNaN(min) || Double.isNaN(max)){
	    min = other_range.min;
	    max = other_range.max;
	    return true;
	}
	

	/*  Check for everything
	 */
	if((max - min) >= 180.0)
	    return true;
	
	if((other_range.max - other_range.min) >= 180.0){
	    min = -90.0;
	    max = 90.0;
	    return true;
	}
	
	/*  
	 *  Check for any overlap and merge if there is overlap.
	 */	    
	if((min <= other_range.min && other_range.min <= max) ||
	   (min <= other_range.max && other_range.max <= max) ||
	   (other_range.min <= min && min <= other_range.max) ||
	   (other_range.min <= max && max <= other_range.max)) {
	    min = Math.min(min, other_range.min);
	    max = Math.max(max, other_range.max);
	    
	    if(max - min >= 180.0){
		min = -90.0;
		max = 90.0;
	    }
	    return true;
	}
	/*  Else the ranges do not overlap - do nothing.
	 */
	return false;
	
    } /* END meldRange */
    
   /**
     * Combine two ranges if possible.  
     * <br>
     * If the two ranges overlap this range is adjusted to include the 
     * other and true is returned. <code>\n</code>
     * <br>
     * If the two ranges do not overlap no changes are made and 
     * false is returned.
     */ 
    public boolean meldRange(double otherMin, double otherMax){
	return meldRange(new LatRange(otherMin, otherMax));
    }

    public String toString()
    {
	return("LatRange[min: "+min+", max: "+max+"]\n");
    }


} /* END  LatRange */

