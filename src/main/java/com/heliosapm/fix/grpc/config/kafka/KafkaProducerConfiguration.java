package com.heliosapm.fix.grpc.config.kafka;

import java.util.List;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.serialization.Serializer;

/**
 * <p>Title: KafkaProducerConfiguration</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.config.kafkaKafkaProducerConfiguration</code></p>
 * <p>2019</p>
 */
public class KafkaProducerConfiguration {
	protected Class<? extends Serializer<?>> keySerializer = null;
	protected Class<? extends Serializer<?>> valueSerializer = null;
	protected String acks = "1";
	protected String bootstrapServers = null;
	protected long bufferMemory = 33554432;
	protected String compressionType = "none";
	protected int retries = 2147483647;
	protected String sslKeyString = null;
	protected String sslKeystoreLocation = null;
	protected String sslKeystoreString = null;
	protected String sslTruststoreLocation = null;
	protected String sslTruststoreString = null;
	protected int batchSize = 16384;
	protected String clientDnsLookup = "default";
	protected String clientId = "";
	protected long connectionsMaxIdleMs = 540000;
	protected int deliveryTimeoutMs = 120000;
	protected int lingerMs = 0;
	protected long maxBlockMs = 60000;
	protected int maxRequestSize = 1048576;
	protected Class<? extends Partitioner> partitionerClass = org.apache.kafka.clients.producer.internals.DefaultPartitioner.class;
	protected int receiveBufferBytes = 32768;
	protected int requestTimeoutMs = 30000;
	protected Class saslClientCallbackHandlerClass = null;
	protected String saslJaasConfig = null;
	protected String saslKerberosServiceName = null;
	protected Class saslLoginCallbackHandlerClass = null;
	protected Class saslLoginClass = null;
	protected String saslMechanism = "GSSAPI";
	protected String securityProtocol = "PLAINTEXT";
	protected int sendBufferBytes = 131072;
	protected String sslEnabledProtocols = "TLSv1.2,TLSv1.1,TLSv1";
	protected String sslKeystoreType = "JKS";
	protected String sslProtocol = "TLS";
	protected String sslProvider = null;
	protected String sslTruststoreType = "JKS";
	protected boolean enableIdempotence = false;
	protected String interceptorClasses = "";
	protected int maxInFlightRequestsPerConnection = 5;
	protected long metadataMaxAgeMs = 300000;
	protected String metricReporters = "";
	protected int metricsNumSamples = 2;
	protected String metricsRecordingLevel = "INFO";
	protected long metricsSampleWindowMs = 30000;
	protected long reconnectBackoffMaxMs = 1000;
	protected long reconnectBackoffMs = 50;
	protected long retryBackoffMs = 100;
	protected String saslKerberosKinitCmd = "/usr/bin/kinit";
	protected long saslKerberosMinTimeBeforeRelogin = 60000;
	protected double saslKerberosTicketRenewJitter = 0.05;
	protected double saslKerberosTicketRenewWindowFactor = 0.8;
	protected short saslLoginRefreshBufferSeconds = 300;
	protected short saslLoginRefreshMinPeriodSeconds = 60;
	protected double saslLoginRefreshWindowFactor = 0.8;
	protected double saslLoginRefreshWindowJitter = 0.05;
	protected String sslCipherSuites = null;
	protected String sslEndpointIdentificationAlgorithm = "https";
	protected String sslKeymanagerAlgorithm = "SunX509";
	protected String sslSecureRandomImplementation = null;
	protected String sslTrustmanagerAlgorithm = "PKIX";
	protected int transactionTimeoutMs = 60000;
	protected String transactionalId = null;

}
