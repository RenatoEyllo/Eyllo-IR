/**
 * 
 */
package com.eyllo.paprika.keeper;

import org.elasticsearch.common.unit.DistanceUnit;

/**
 * @author renato
 *
 */
public class UtilsStore {

	/**
	 * Gets the specific distance unit according a string.
	 * @param pDistanceUnit string representing distance unit.
	 * @return DistanceUnit used inside ElasticSearch.
	 */
	public static DistanceUnit getDistanceUnit(String pDistanceUnit) { 
		DistanceUnit disUnit = KeeperProperties.DEFAULT_DIST_UNIT;
		  if (pDistanceUnit.equals("km"))
			  disUnit = DistanceUnit.KILOMETERS;
		  else if (pDistanceUnit.equals("cm"))
			  disUnit = DistanceUnit.CENTIMETERS;
		  else if (pDistanceUnit.equals("inch"))
			  disUnit = DistanceUnit.INCH;
		  else if (pDistanceUnit.equals("mi"))
			  disUnit = DistanceUnit.MILES;
		  else if (pDistanceUnit.equals("m"))
			  disUnit = DistanceUnit.METERS;
		  else if (pDistanceUnit.equals("mm"))
			  disUnit = DistanceUnit.MILLIMETERS;
		  else if (pDistanceUnit.equals("yd"))
			  disUnit = DistanceUnit.YARD;
		  return disUnit;
	}
}
