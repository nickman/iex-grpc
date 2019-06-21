package com.heliosapm.fix.grpc.socketio;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drivewealth.grpc.api.instrument.LastTrade;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.heliosapm.fix.grpc.config.Configuration;
import com.heliosapm.fix.grpc.config.Constants;
import com.heliosapm.fix.grpc.processing.KafkaConsumer;
import com.heliosapm.fix.grpc.processing.KafkaProcessingQueue;
import com.heliosapm.fix.grpc.serdes.GeneratedMessageV3Serde;
import com.heliosapm.fix.grpc.utils.NetUtils;
import com.heliosapm.fix.grpc.utils.ProtoUtil;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * <p>Title: IEXClient</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.socketioIEXClient</code></p>
 * <p>2019</p>
 */
public class IEXClient implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(IEXClient.class);
	protected static final Manager manager = Configuration.getConfiguredManager();
	protected static final Cache<URI, IEXClient> connections = CacheBuilder.newBuilder()
			.concurrencyLevel(Constants.CORES)
			.initialCapacity(64)
			.maximumSize(1024)
			.weakValues()
			.recordStats()
			.removalListener(new RemovalListener<URI, IEXClient>() {
				@Override
				public void onRemoval(RemovalNotification<URI, IEXClient> n) {
					LOG.info("Cache Removal [{}], cause={}", n.getKey(), n.getCause());		
					try { n.getValue().close(); } catch (Exception x) {}
				}
			})
			.build();
	
	protected final URI uri;
	protected final Socket socket;
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	protected final KafkaProcessingQueue<LastTrade> pQueue = new KafkaProcessingQueue<LastTrade>("iex_last", LastTrade.class, (Class<Serializer<LastTrade>>) GeneratedMessageV3Serde.LAST_TRADE_SERIALIZER.getClass());
	
	public static IEXClient client(String url) {
		if (Strings.isNullOrEmpty(url)) {
			throw new IllegalArgumentException("Null or blank URL");
		}	
		final URI uri = NetUtils.uri(url);
		try {
			return connections.get(uri, () -> new IEXClient(uri));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create IEXClient for [" + uri + "]", ex);
		}		
	}
	
	protected IEXClient(URI uri) {
		this.uri = uri;
		socket = IO.socket(this.uri);
//		socket = manager.socket(uri.toString());
		socket.on(Socket.EVENT_CONNECT, listener(this::onConnect))
		.on(Socket.EVENT_CONNECT_ERROR, listener(this::onConnectError))
		.on(Socket.EVENT_CONNECT_TIMEOUT, listener(this::onConnectTimeout))
		.on(Socket.EVENT_CONNECTING, listener(this::onConnecting))
		.on(Socket.EVENT_DISCONNECT, listener(this::onDisconnect))
		.on(Socket.EVENT_MESSAGE, listener(this::onMessage))
		.on(Socket.EVENT_RECONNECT, listener(this::onReconnect))
		.on(Socket.EVENT_RECONNECT_ATTEMPT, listener(this::onReconnectAttempt))
		.on(Socket.EVENT_RECONNECT_ERROR, listener(this::onReconnectError))
		.on(Socket.EVENT_RECONNECT_FAILED, listener(this::onReconnectFailed))
		.on(Socket.EVENT_RECONNECTING, listener(this::onReconnecting));
		
		socket.connect();		
	}
	
	public void close() throws IOException {
		socket.off();
		socket.close();
		connections.invalidate(uri);
	}
	
	protected void onConnecting(Object...args) {
		LOG.info("IEXClient[{}] Connecting: {}", uri, args);
	}

	protected void onConnect(Object...args) {
		LOG.info("IEXClient[{}] Connected: {}", uri, args);
		//socket.emit("subscribe", "snap,fb,aig+");
		//socket.emit("subscribe", "firehose");
		socket.emit("subscribe", "firehose");
	}
	
	protected void onDisconnect(Object...args) {
		LOG.info("IEXClient[{}] Disconnected: {}", uri, args);
	}
	
	protected void onReconnecting(Object...args) {
		LOG.info("IEXClient[{}] Reconnecting: {}", uri, args);
	}	
	
	protected void onReconnect(Object...args) {
		LOG.info("IEXClient[{}] Reconnected: {}", uri, args);
	}

	protected void onReconnectAttempt(Object...args) {
		LOG.info("IEXClient[{}] ReconnectAttempt: {}", uri, args);
	}
	
	protected void onReconnectError(Object...args) {
		LOG.info("IEXClient[{}] ReconnectError: {}", uri, args);
	}
	
	protected void onReconnectFailed(Object...args) {
		LOG.info("IEXClient[{}] ReconnectFailed: {}", uri, args);
	}
	
	protected void onMessage(Object...args) {
//		LOG.info("IEXClient[{}] Message: ({}) --> {}", uri, args[0].getClass().getName(),  args);
		LastTrade lt = ProtoUtil.parseJson(args[0].toString(), LastTrade.class);
		
		long total = pQueue.send(lt.getSymbol(), lt);
		LOG.info("LastTrade: symbol={}, price={}, size={}, time={}, seq={}, total={}", lt.getSymbol(),
				lt.getPrice(), lt.getSize(), new Date(lt.getTime()), lt.getSeq(), total);
	}

	protected void onConnectError(Object...args) {
		LOG.info("IEXClient[{}] ConnectError: {}", uri, args);
	}
	
	protected void onConnectTimeout(Object...args) {
		LOG.info("IEXClient[{}] ConnectTimeout: {}", uri, args);
	}
	
	protected static Emitter.Listener listener(final Consumer<Object[]> handler) {
		return new Emitter.Listener() {
			@Override
			public void call(Object... args) {				
				handler.accept(args);
			}
		};
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.setProperty("fix.grpc.reconnect.count", "3");
//		System.setProperty(Configuration.KEY_TIMEOUT, "1000");
		
		client(Constants.IEX_LAST);
		try {
			new KafkaConsumer("iex_last", LastTrade.class, GeneratedMessageV3Serde.LastTradeDeserializer.class);
			Thread.currentThread().join();
//			Thread.sleep(20000);
//			LOG.info("Closing...");
//			client.close();
//			LOG.info("Closed");
//			client = null;
//			System.gc();
//			Thread.sleep(5000);
//			System.gc();
//			Thread.sleep(5000);
//			LOG.info("Bye ...");
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
		
	}

}
