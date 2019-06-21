package com.heliosapm.fix.grpc.config;

/**
 * <p>Title: Constants</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpcConstants</code></p>
 * <p>2019</p>
 */
public class Constants {
	public static final String CORES_OVERRIDE_KEY = "heliosapm.cores";
	public static final int ACTUAL_CORES = Runtime.getRuntime().availableProcessors();
	public static final int CORES;
	public static final int DEFAULT_INSTRUMENT_COUNT = 10000;
	
	public static final String IEX_TOPS = "https://ws-api.iextrading.com/1.0/tops";
	public static final String IEX_LAST = "https://ws-api.iextrading.com/1.0/last";
	
	
	static {
		String strCores = System.getProperty(CORES_OVERRIDE_KEY, "" + ACTUAL_CORES);
		int cores = ACTUAL_CORES;
		try {
			cores = Integer.parseInt(strCores);
		} catch (Exception ex) {
			cores = ACTUAL_CORES;
		}
		CORES = cores;
	}
}
