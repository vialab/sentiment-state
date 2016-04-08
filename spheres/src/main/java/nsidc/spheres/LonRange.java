package nsidc.spheres;

import java.lang.*;
/********************************************************************
 * LonRange class - provide services and information relating to a 
 * longitude range.  Compensates for the dateline and whatnot.
 * <P>
 * This class assumes a spherical earth.
 *
 * @author 
 * 08-September-2000 R.Swick swick@chukchi.colorado.edu 303-492-6069 <BR>
 * National Snow & Ice Data Center, University of Colorado, Boulder <BR>
 * Copyright 2003 University of Colorado.
 ********************************************************************/
public class LonRange extends Sphere
{
    
    /*******************************************************************
     * instance variables
     *******************************************************************/
    
    /**
     * minimum longitude in range.
     */
    public double min = 999999;
    
    /**
     * maximum longitude in range.
     */
    public double max = -9999999;
    
    /*******************************************************************
     * The constructors.
     *******************************************************************/
    
    /** 
     * Assumes West to East orientation.  So if min > max that just means
     * you're taking the long way round.
     */
    public LonRange(double givenMin, double givenMax){
	min = givenMin;
	max = givenMax;
	
	/* Assume the input is correct
	 */
	while(max < min)
	    max += 360.0;
	
	/*  Eliminate dateline problems 
	 */
	while(540.0 < min || 540.0 < max){
	    min -= 360.0;
	    max -= 360.0;
	}
	while(-180.0 > min || -180.0 > max){
	    min += 360.0;
	    max += 360.0;
	}
    }
    
    /** 
     * Assumes West to East orientation.  So if min > max that just means
     * you're taking the long way round.
     */
    public LonRange(float givenMin, float givenMax){
	min = (double)givenMin;
	max = (double)givenMax;
	
	/* Assume the input is correct
	 */
	while(max < min)
	    max += 360.0;
	
	/*  Eliminate dateline problems 
	 */
	while(540.0 < min || 540.0 < max){
	    min -= 360.0;
	    max -= 360.0;
	}
	while(-180.0 > min || -180.0 > max){
	    min += 360.0;
	    max += 360.0;
	}
    }
    
   
    /**
     * Determine if the given longitude is within the range.
     */
    public boolean contains(float lon){
	return(contains((double)lon));
    }
    
    /**
     * Determine if the given longitude is within the range.
     */
    public boolean contains(double lon){
	while(-180.0 > lon)
	    lon += 360.0;
	
	if(min <= lon && lon <= max)
	    return true;
	
	lon += 360.0;
	
	if(min <= lon && lon <= max)
	    return true;
	    
	return false;
    }    
    
    /**
     * Determine if this range overlaps another.
     */
    public boolean overlaps(LonRange other_range)
    {
	/* 
	 * Check for nothing  
	 */
	if(null == other_range)
	    return false;

	/*  
	 * Check for everything
	 */
	if((max - min) >= 360.0)
	    return true;
	
	if((other_range.max - other_range.min) >= 360.0)
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
	 * Check for overlap near the dateline.
	 */
	if((min <= other_range.min + 360.0 && other_range.min + 360.0 <= max) ||
	   (min <= other_range.max + 360.0 && other_range.max + 360.0 <= max) ||
	   (other_range.min <= min + 360.0 && min + 360.0 <= other_range.max) ||
	   (other_range.min <= max + 360.0 && max + 360.0 <= other_range.max)) 
	    return true;
	
	/*  
	 * Else the ranges do not overlap.
	 */
	return false;
    }

    /**
     * Combine two ranges if possible.  
     *
     * If the two ranges overlap this range is adjusted to include the 
     * other and true is returned. The other range can then be discarded 
     * by the calling method.\n
     *
     * If the two ranges do not overlap no changes are made and 
     * false is returned.
     */
    public boolean meldRange(LonRange other_range){
	
	/* Check for nothing 
	   
	 */
	if(null == other_range)
	    return true;
	
	if(Double.isNaN(other_range.min) || Double.isNaN(other_range.max))
	    return true;
	
	if(Double.isNaN(min) || Double.isNaN(max)){
	    min = other_range.min;
	    max = other_range.max;
	    return true;
	}
	

	/*  Check for everything
	 */
	if((max - min) >= 360.0)
	    return true;
	
	if((other_range.max - other_range.min) >= 360.0){
	    min = -180.0;
	    max = 180.0;
	    return true;
	}
	
	/* 
	 * Check for overlap near the dateline and adjust if necessary
	 */
	if((min <= other_range.min + 360.0 && other_range.min + 360.0 <= max) ||
	   (min <= other_range.max + 360.0 && other_range.max + 360.0 <= max)) {
	    other_range.min += 360.0;
	    other_range.max += 360.0;
	}
	
	if((other_range.min <= min + 360.0 && min + 360.0 <= other_range.max) ||
	   (other_range.min <= max + 360.0 && max + 360.0 <= other_range.max)) {
	    min += 360.0;
	    max += 360.0;
	}
	
	/*  
	 *Check for any overlap and merge if there is overlap.
	 */	    
	if((min <= other_range.min && other_range.min <= max) ||
	   (min <= other_range.max && other_range.max <= max) ||
	   (other_range.min <= min && min <= other_range.max) ||
	   (other_range.min <= max && max <= other_range.max)) {
	    min = Math.min(min, other_range.min);
	    max = Math.max(max, other_range.max);
	    
	    if(max - min >= 360.0){
		min = -180.0;
		max = 180.0;
	    }
	    return true;
	}
	/*  Else the ranges do not overlap - do nothing.
	 */
	return false;
	
    } /* END meldRange */
    
   /**
     * Combine two ranges if possible.  
     *
     * If the two ranges overlap this range is adjusted to include the 
     * other and true is returned. <code>\n</code>
     *
     * If the two ranges do not overlap no changes are made and 
     * false is returned.
     */ 
    public boolean meldRange(double otherMin, double otherMax){
	return meldRange(new LonRange(otherMin, otherMax));
    }
    public String toString()
    {
	return("LonRange[min: "+min+", max: "+max+"]");
    }
} /* END  LonRange */

