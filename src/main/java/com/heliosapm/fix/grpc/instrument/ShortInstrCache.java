package com.heliosapm.fix.grpc.instrument;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.heliosapm.fix.grpc.config.Constants;


/**
 * <p>Title: ShortInstrCache</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.instrumentShortInstrCache</code></p>
 * <p>2019</p>
 */
public class ShortInstrCache {
	private static final Logger LOG = LoggerFactory.getLogger(ShortInstrCache.class);
	public static final String CACHE_SPEC_KEY = "instrument.cache.spec";
	public static final String DEFAULT_CACHE_SPEC = 
		"concurrencyLevel=" + Constants.CORES + "," + 
		"initialCapacity=" + Constants.DEFAULT_INSTRUMENT_COUNT + "," + 
		"maximumSize=100000," + 
		"recordStats";
	private static volatile ShortInstrCache instance = null;
	private static final Object lock = new Object();
			
	private final Cache<String, ShortInstr> cache;
	
	public static ShortInstrCache getInstance(DynamoDB db) {
		if (instance == null) {
			synchronized(lock) {
				if (instance == null) {
					instance = new ShortInstrCache(db);
				}
			}
		}
		return instance;
	}
	
	public static ShortInstrCache getInstance() {
		if (instance == null) {
			synchronized(lock) {
				if (instance == null) {
					throw new IllegalStateException("Cache not initialized");
				}
			}
		}
		return instance;
		
	}
	
	private ShortInstrCache(DynamoDB db) {
		String spec = System.getProperty(CACHE_SPEC_KEY, DEFAULT_CACHE_SPEC);
		cache = CacheBuilder.from(spec).build();
		long startTime = System.currentTimeMillis();
		Table t = db.getTable("Instruments");
		Iterator<Item> iter = t.scan().iterator();
		while(iter.hasNext()) {
			Item item = iter.next();
			ShortInstr si = ShortInstr.fromItem(item);
			cache.put(si.getSymbol(), si);			
		}
		LOG.info("Cache loaded {} values in {} ms.", cache.size(), System.currentTimeMillis() - startTime);
	}
	
	public ShortInstr get(String symbol) {
		try {
			return cache.get(symbol, new Callable<ShortInstr>() {
				@Override
				public ShortInstr call() throws Exception {
					throw new UnsupportedOperationException("Lookup not implemented");					
				}
			});
		} catch (Exception ex) {
			throw new RuntimeException("Failed to resolve symbol [" + symbol + "]", ex);
		}		
	}
}


//concurrencyLevel=[integer]: sets CacheBuilder.concurrencyLevel.
//initialCapacity=[integer]: sets CacheBuilder.initialCapacity.
//maximumSize=[long]: sets CacheBuilder.maximumSize.
//maximumWeight=[long]: sets CacheBuilder.maximumWeight.
//expireAfterAccess=[duration]: sets CacheBuilder.expireAfterAccess.
//expireAfterWrite=[duration]: sets CacheBuilder.expireAfterWrite.
//refreshAfterWrite=[duration]: sets CacheBuilder.refreshAfterWrite.
//weakKeys: sets CacheBuilder.weakKeys.
//softValues: sets CacheBuilder.softValues.
//weakValues: sets CacheBuilder.weakValues.
//recordStats: sets CacheBuilder.recordStats.

