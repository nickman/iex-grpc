package com.heliosapm.fix.grpc.utils;

import java.net.URI;

import com.google.common.base.Strings;

/**
 * <p>Title: NetUtils</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.utilsNetUtils</code></p>
 * <p>2019</p>
 */
public class NetUtils {
	
	public static URI uri(String u) {
		if (Strings.isNullOrEmpty(u)) {
			throw new IllegalArgumentException("Null or blank URI");
		}
		try {
			return new URI(u.trim());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid URI: [" + u + "]", ex);
		}
	}
}
