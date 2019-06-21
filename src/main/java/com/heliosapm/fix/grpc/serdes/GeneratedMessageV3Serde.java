package com.heliosapm.fix.grpc.serdes;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.drivewealth.grpc.api.instrument.LastTrade;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * <p>Title: GeneratedMessageV3Serde</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.serdesGeneratedMessageV3Serde</code></p>
 * <p>2019</p>
 */
public class GeneratedMessageV3Serde {
	public static final Serializer<LastTrade> LAST_TRADE_SERIALIZER = new LastTradeSerializer();
	/**
	 * <p>Title: LastTradeSerializer</p>
	 * <p>Description: </p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>com.heliosapm.fix.grpc.serdesLastTradeSerializer</code></p>
	 * <p>2019</p>
	 */
	public static class LastTradeSerializer implements Serializer<LastTrade> { 
		/**
		 * @see org.apache.kafka.common.serialization.Serializer#configure(java.util.Map, boolean)
		 */
		@Override
		public void configure(Map<String, ?> configs, boolean isKey) {
			/* No Op */			
		}

		/**
		 * @see org.apache.kafka.common.serialization.Serializer#serialize(java.lang.String, java.lang.Object)
		 */
		@Override
		public byte[] serialize(String topic, LastTrade data) {
			return data.toByteArray();
		}

		/**
		 * @see org.apache.kafka.common.serialization.Serializer#close()
		 */
		@Override
		public void close() {
			/* No Op */			
		}		
	}
	
	/**
	 * <p>Title: LastTradeDeserializer</p>
	 * <p>Description: </p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>com.heliosapm.fix.grpc.serdesLastTradeDeserializer</code></p>
	 * <p>2019</p>
	 */
	public static class LastTradeDeserializer implements Deserializer<LastTrade> {

		/**
		 * @see org.apache.kafka.common.serialization.Deserializer#configure(java.util.Map, boolean)
		 */
		@Override
		public void configure(Map<String, ?> configs, boolean isKey) {
			/* No Op */
		}

		/**
		 * @see org.apache.kafka.common.serialization.Deserializer#deserialize(java.lang.String, byte[])
		 */
		@Override
		public LastTrade deserialize(String topic, byte[] data) {			
			try {
				return LastTrade.parseFrom(data);
			} catch (InvalidProtocolBufferException e) {
				throw new RuntimeException("Failed to deserialize LastTrade", e);
			}
		}

		/**
		 * @see org.apache.kafka.common.serialization.Deserializer#close()
		 */
		@Override
		public void close() {
			/* No Op */			
		}
		
	}
}
