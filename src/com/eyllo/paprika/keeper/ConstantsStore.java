package com.eyllo.paprika.keeper;

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;

public class ConstantsStore {

	/**
	 * Default distance unit to be used for querying entities.
	 */
	final public static DistanceUnit DEFAULT_DIST_UNIT = DistanceUnit.KILOMETERS;

	/**
	 * How to compute the distance. Can either be arc (better precision) or plane (faster). Defaults to arc
	 */
	final public static GeoDistance DEFAULT_GEO_DIST = GeoDistance.ARC;
	
	/**
	 * Optimization of using first a bounding box check will be used.
	 */
	final public static String BOUNDING_BOX_MEMORY = "memory";
	final public static String BOUNDING_BOX_INDEXED = "indexed";
	final public static String BOUNDING_BOX_NONE = "none";
}
