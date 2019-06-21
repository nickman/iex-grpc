package com.heliosapm.fix.grpc.processing;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;

import com.heliosapm.fix.grpc.utils.ConfigUtil;

/**
 * <p>Title: KafkaProcessingQueue</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.processingKafkaProcessingQueue</code></p>
 * <p>2019</p>
 */
public class KafkaProcessingQueue<T> {
	public static final String KEY_BOOTSTRAP_SERVERS = "bootstrap.servers";
	public static final String DEFAULT_BOOTSTRAP_SERVERS = "127.0.0.1:9092";

	public static final String KEY_ACKS = "acks";
	public static final String DEFAULT_ACKS = "all";
	
	public static final String KEY_DELIVERY_TIMEOUT_MS = "delivery.timeout.ms";
	public static final long DEFAULT_DELIVERY_TIMEOUT_MS = 30000;
	
	public static final String KEY_BATCH_SIZE = "batch.size";
	public static final int DEFAULT_BATCH_SIZE = 16384;
	
	public static final String KEY_BUFFER_MEMORY = "buffer.memory";
	public static final int DEFAULT_BUFFER_MEMORY = 33554432;
	
	public static final String KEY_KEY_SERIALIZER = "key.serializer";
	public static final String DEFAULT_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	
	public static final String KEY_VALUE_SERIALIZER = "value.serializer";
	public static final String DEFAULT_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	
	public static Properties getProducerProperties(Properties cfg) {
		if (cfg==null) {
			cfg = System.getProperties();
		}
		Properties p = new Properties();
		p.setProperty(KEY_BOOTSTRAP_SERVERS, ConfigUtil.getSystemThenEnvProperty(
				KEY_BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS, cfg));
		p.setProperty(KEY_ACKS, ConfigUtil.getSystemThenEnvProperty(
				KEY_ACKS, DEFAULT_ACKS, cfg));
		p.setProperty(KEY_DELIVERY_TIMEOUT_MS, "" + ConfigUtil.getLongSystemThenEnvProperty(
				KEY_DELIVERY_TIMEOUT_MS, DEFAULT_DELIVERY_TIMEOUT_MS, cfg));
		p.setProperty(KEY_BATCH_SIZE, "" + ConfigUtil.getLongSystemThenEnvProperty(
				KEY_BATCH_SIZE, DEFAULT_BATCH_SIZE, cfg));
		p.setProperty(KEY_BUFFER_MEMORY, "" + ConfigUtil.getLongSystemThenEnvProperty(
				KEY_BUFFER_MEMORY, DEFAULT_BUFFER_MEMORY, cfg));
		p.setProperty(KEY_KEY_SERIALIZER, ConfigUtil.getSystemThenEnvProperty(
				KEY_KEY_SERIALIZER, DEFAULT_KEY_SERIALIZER, cfg));
		p.setProperty(KEY_VALUE_SERIALIZER, ConfigUtil.getSystemThenEnvProperty(
				KEY_VALUE_SERIALIZER, DEFAULT_VALUE_SERIALIZER, cfg));
		return p;
	}
	
	public static Properties getProducerProperties() {
		return getProducerProperties(System.getProperties());
	}
	
	public static Properties getProducerProperties(String valueSerializer) {
		Properties p = new Properties();
		p.putAll(getProducerProperties(System.getProperties()));
		p.setProperty(KEY_VALUE_SERIALIZER, valueSerializer);
		return getProducerProperties(p);
	}
	
	private final Class<T> type;
	private final Producer<String, T> producer;
	private final String topic;
	private final AtomicLong seq = new AtomicLong();
	
	public KafkaProcessingQueue(String topic, Class<T> type, Class<Serializer<T>> valueSerializer) {
		Properties p = getProducerProperties(valueSerializer.getName());
		producer = new KafkaProducer<>(p);
		this.topic = topic;
		this.type = type;
		
	}
	
	public long send(String key, T value) {
		producer.send(new ProducerRecord<String, T>(topic, key, value));		
		return seq.incrementAndGet();
	}
	
	
	

	
}

//Properties props = new Properties();
//props.put("bootstrap.servers", "localhost:9092");
//props.put("acks", "all");
//props.put("delivery.timeout.ms", 30000);
//props.put("batch.size", 16384);
//props.put("linger.ms", 1);
//props.put("buffer.memory", 33554432);
//props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");