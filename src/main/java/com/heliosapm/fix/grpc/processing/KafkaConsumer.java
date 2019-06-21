package com.heliosapm.fix.grpc.processing;

import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drivewealth.grpc.api.instrument.LastTrade;
import com.heliosapm.fix.grpc.config.Constants;
import com.heliosapm.fix.grpc.serdes.GeneratedMessageV3Serde;
import com.heliosapm.fix.grpc.utils.ConfigUtil;

import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * <p>Title: KafkaConsumer</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.processingKafkaConsumer</code></p>
 * <p>2019</p>
 */
public class KafkaConsumer<T> {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);
	
	public static final String KEY_BOOTSTRAP_SERVERS = "bootstrap.servers";
	public static final String DEFAULT_BOOTSTRAP_SERVERS = "127.0.0.1:9092";

	public static final String KEY_GROUP_ID = "group.id";
	public static final String DEFAULT_GROUP_ID = "fix-grpc";
	
	public static final String KEY_KEY_DESERIALIZER = "key.deserializer";
	public static final String DEFAULT_KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
	
	public static final String KEY_VALUE_DESERIALIZER = "value.deserializer";
	public static final String DEFAULT_VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	
	public static Properties getConsumerProperties(Properties cfg) {
		if (cfg==null) {
			cfg = System.getProperties();
		}
		Properties p = new Properties();
		p.setProperty(KEY_BOOTSTRAP_SERVERS, ConfigUtil.getSystemThenEnvProperty(
				KEY_BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS, cfg));
		p.setProperty(KEY_GROUP_ID, ConfigUtil.getSystemThenEnvProperty(
				KEY_GROUP_ID, DEFAULT_GROUP_ID, cfg));		
		p.setProperty(KEY_KEY_DESERIALIZER, ConfigUtil.getSystemThenEnvProperty(
				KEY_KEY_DESERIALIZER, DEFAULT_KEY_DESERIALIZER, cfg));
		p.setProperty(KEY_VALUE_DESERIALIZER, ConfigUtil.getSystemThenEnvProperty(
				KEY_VALUE_DESERIALIZER, DEFAULT_VALUE_DESERIALIZER, cfg));
		return p;
	}
	
	public static Properties getConsumerProperties(String valueDeserializer) {
		Properties p = new Properties();
		p.putAll(getConsumerProperties(System.getProperties()));
		p.setProperty(KEY_VALUE_DESERIALIZER, valueDeserializer);
		return getConsumerProperties(p);
	}
	
	private final Class<T> type;
	private final String topic;
	private final AtomicLong seq = new AtomicLong();
	
	public KafkaConsumer(String topic, Class<T> type, Class<Deserializer<T>> valueDeserializer) {
		Properties p = getConsumerProperties(valueDeserializer.getName());		
		this.topic = topic;
		this.type = type;
		ReceiverOptions<String, T> receiverOptions =
			    ReceiverOptions.<String, T>create(p)         
			                   .subscription(Collections.singleton(topic)); 
		Flux<ReceiverRecord<String, T>> inboundFlux =
			    KafkaReceiver.create(receiverOptions)
			                 .receive();
		inboundFlux.subscribe(r -> {
			long total = seq.incrementAndGet();
		    r.receiverOffset().acknowledge();  
		    LastTrade lt = (LastTrade)r.value();
			LOG.info("Received: symbol={}, price={}, size={}, time={}, seq={}, total={}", lt.getSymbol(),
					lt.getPrice(), lt.getSize(), new Date(lt.getTime()), lt.getSeq(), total);

		});		
		
	}
	

	public static void main(String[] args) {
//		System.setProperty("fix.grpc.reconnect.count", "3");
//		System.setProperty(Configuration.KEY_TIMEOUT, "1000");
		
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
