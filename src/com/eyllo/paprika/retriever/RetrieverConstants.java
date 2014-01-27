/**
 * 
 */
package com.eyllo.paprika.retriever;

/**
 * Class containing constants needed for running
 * the Entity Retriever job.
 * @author renatomarroquin
 */
public class RetrieverConstants {

  /** Default geocoder. */
  public static String DEFAULT_GEOCODER = "google";

  /* Properties for Retriever. */
  /** Number of runs Entity retriever will perform. */
  public static String RET_RUNS_NUM = "retriever.runs.number";
  /** Time between retriever runs (seconds). */
  public static String RET_RUNS_INTERLEAVE = "retriever.runs.interleave";
  /** Default number of runs that Retriever will do.*/
  public static int DEFAULT_NUM_RUNS = 1;
  /** Default time interleaved between runs. */
  public static int DEFAULT_TIME_INTERLEAVED;

  /* Properties for Retriever backend. */
  /** Helps us decide where to store retrieved entities. */
  public static String RET_BACKEND_ENTITIES = "retriever.backend.entities";
  /** Backend server address. */
  public static String RET_BACKEND_SRVR_ADD = "retriever.backend.server";
  /** Backend server address port. */
  public static String RET_BACKEND_SRVR_PORT = "retriever.backend.port";
  /** Default backend used for storing entities. */
  public static String RET_DEF_BACKEND_ENT = "file";

  /* Properties for Parser. */
  public static String RPARSER_NAME = "retriever.parser.name";
  public static String RPARSER_MAXPAGENUM = "retriever.parser.maxpagenum";
  public static String RPARSER_MAXNUMENT = "retriever.parser.maxnument";
  public static String RPARSER_OUTPATH = "retriever.parser.outpath";
  public static String RPARSER_FETCHURL = "retriever.parser.fetchurl";
  public static String RPARSER_LOCALSEARCH = "retriever.parser.localsearch";
  public static String RPARSER_REQPOLITENESS = "retriever.parser.reqpoliteness";
}
