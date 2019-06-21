package com.heliosapm.fix.grpc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heliosapm.fix.grpc.utils.ConfigUtil;

import io.socket.client.Manager;



/**
 * <p>Title: Configuration</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpcConfiguration</code></p>
 * <p>2019</p>
 */
public class Configuration {
	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

	public static final String KEY_RECONNECT_ENABLED = "fix.grpc.reconnect.enabled";
	public static final boolean DEFAULT_RECONNECT_ENABLED = true;
	
	public static final String KEY_RECONNECT_COUNT = "fix.grpc.reconnect.count";
	public static final int DEFAULT_RECONNECT_COUNT = 64;

	public static final String KEY_RECONNECT_DELAY_MIN = "fix.grpc.reconnect.delay.min";
	public static final long DEFAULT_RECONNECT_DELAY_MIN = 1000;

	public static final String KEY_RECONNECT_DELAY_MAX = "fix.grpc.reconnect.delay.max";
	public static final long DEFAULT_RECONNECT_DELAY_MAX = 60000;
	
	public static final String KEY_TIMEOUT = "fix.grpc.connect.timeout";
	public static final long DEFAULT_TIMEOUT = 10000;
	
	public static boolean isReconnectEnabled() {
		return ConfigUtil.getBooleanSystemThenEnvProperty(KEY_RECONNECT_ENABLED, DEFAULT_RECONNECT_ENABLED);
	}
	
	public static int getReconnectCount() {
		return ConfigUtil.getIntSystemThenEnvProperty(KEY_RECONNECT_COUNT, DEFAULT_RECONNECT_COUNT);
	}
	
	public static long getReconnectDelayMin() {
		return ConfigUtil.getLongSystemThenEnvProperty(KEY_RECONNECT_DELAY_MIN, DEFAULT_RECONNECT_DELAY_MIN);
	}

	public static long getReconnectDelayMax() {
		return ConfigUtil.getLongSystemThenEnvProperty(KEY_RECONNECT_DELAY_MAX, DEFAULT_RECONNECT_DELAY_MAX);
	}
	
	public static long getTimeout() {
		return ConfigUtil.getLongSystemThenEnvProperty(KEY_TIMEOUT, DEFAULT_TIMEOUT);
	}
	
	public static Manager getConfiguredManager() {
		Manager.Options opts = new Manager.Options();
		opts.reconnection = isReconnectEnabled();
		opts.reconnectionAttempts = getReconnectCount();
		opts.reconnectionDelay = getReconnectDelayMin();
		opts.reconnectionDelayMax = getReconnectDelayMax();
		opts.timeout = getTimeout();		
		return new Manager(opts);
	}
	

}
