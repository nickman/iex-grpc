package com.heliosapm.fix.grpc.loading;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import com.heliosapm.fix.grpc.instrument.ShortInstr;
import com.heliosapm.fix.grpc.instrument.ShortInstrCache;

/**
 * <p>Title: ShortInstrumentLoader</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.loadingShortInstrumentLoader</code></p>
 * <p>2019</p>
 */
public class ShortInstrumentLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShortInstrumentLoader.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final int MAX_BATCH_SIZE = 25;
	private static final AtomicInteger batchSerial = new AtomicInteger();
	
	public static ShortInstr[] load(File f) {
		try {
			return mapper.readerFor(ShortInstr[].class).readValue(Files.readAllBytes(f.toPath()));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to parse file [" + f + "]", ex);
		}
	}
	
	public static boolean filterIn(ShortInstr si) {
		String x = si.getName();
		return x != null && !x.isEmpty();
	}
	
	public static void writeToDb(DynamoDB db, String tableName, ShortInstr...instrs) {
		final long startTime = System.currentTimeMillis();
		Stream<Item> stream = Arrays.stream(instrs).parallel()
				.filter(ShortInstrumentLoader::filterIn)
				.map(ShortInstr::toItem);
		Iterator<List<Item>> iter = Iterators.partition(stream.iterator(), MAX_BATCH_SIZE);
		List<List<Item>> itemsList = new ArrayList<>();
		while(iter.hasNext()) {
			itemsList.add(iter.next());
		}
		
		
//		Stream<List<Item>> insertStream = StreamSupport.stream(
//		          Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED),
//		          false);

		itemsList.parallelStream().map(batch -> writeBatch(db, batch, tableName))
			.forEach(o -> {
				if (!o.getUnprocessedItems().isEmpty()) {
					LOG.warn("UNPROCESSED ITEMS: {}", o);
				}
			});
		
//		Iterators.partition(stream.parallel().iterator(), MAX_BATCH_SIZE)			
//			.forEachRemaining(batch -> writeBatch(db, batch, tableName));
		LOG.info("\n\n\tAll records loaded in {} ms", System.currentTimeMillis() - startTime);
	}
	
	public static BatchWriteItemOutcome writeBatch(DynamoDB db, List<Item> batch, String tableName) {
		final long batchId = batchSerial.incrementAndGet();
		final long startTime = System.currentTimeMillis();
		LOG.info("Writing {} Batch #{}, size={}", tableName, batchId, batch.size());
		TableWriteItems writeItems = new TableWriteItems(tableName)
			.withItemsToPut(batch);
		BatchWriteItemOutcome outcome = db.batchWriteItem(writeItems);
		LOG.info("Batch #{} completed in {} ms", batchId, System.currentTimeMillis() - startTime);
		return outcome;
	}
	
	public static Iterator<Item> query(DynamoDB db, String tableName, QuerySpec spec) {
		Table table = db.getTable(tableName);
		return table.query(spec).iterator();
	}
	
	public static Iterator<Item> scan(DynamoDB db, String tableName, ScanSpec spec) {
		Table table = db.getTable(tableName);		
		return table.scan(spec).iterator();
	}
	
	
//	java.util.List<AttributeDefinition> attributeDefinitions, 
//	String tableName, 
//	java.util.List<KeySchemaElement> keySchema,
//    ProvisionedThroughput provisionedThroughput	
	
	public static void checkInstrumentTable(AmazonDynamoDB db) {
		CreateTableRequest ctr = new CreateTableRequest(
				Arrays.asList(new AttributeDefinition("iexId", ScalarAttributeType.N), new AttributeDefinition("symbol", ScalarAttributeType.S)),
				"Instruments",
				Arrays.asList(new KeySchemaElement("iexId", KeyType.HASH), new KeySchemaElement("symbol", KeyType.RANGE)),
				new ProvisionedThroughput(10L, 10L)
		);
		if (TableUtils.createTableIfNotExists(db, ctr)) {
			LOG.info("Instruments table created");
		} else {
			LOG.info("Instruments table exists");
		}		
	}
	
	public static void checkPricesTable(AmazonDynamoDB db) {
		CreateTableRequest ctr = new CreateTableRequest(
				Arrays.asList(new AttributeDefinition("iexId", ScalarAttributeType.N), new AttributeDefinition("symbol", ScalarAttributeType.S)),
				"Instruments",
				Arrays.asList(new KeySchemaElement("iexId", KeyType.HASH), new KeySchemaElement("symbol", KeyType.RANGE)),
				new ProvisionedThroughput(10L, 10L)
		);
		if (TableUtils.createTableIfNotExists(db, ctr)) {
			LOG.info("Instruments table created");
		} else {
			LOG.info("Instruments table exists");
		}		
	}
	
	
//	  {
//		    "symbol": "SNAP",
//		    "price": 111.76,
//		    "size": 5,
//		    "time": 1480446905681
//		  },
	  
	
	public static void dropTable(AmazonDynamoDB db, String tableName) {
		if (TableUtils.deleteTableIfExists(db, new DeleteTableRequest(tableName))) {
			LOG.info("Dropped table {}", tableName);
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration("http://localhost:7902", "us-west"))
				.build();
		DynamoDB db = new DynamoDB(client); 
		File f = new File("/home/nwhitehead/hprojects/dynamo/symbols.json");
//		ShortInstr[] instrs = load(f);
//		dropTable(client, "Instruments");
//		checkInstrumentTable(client);
//		writeToDb(db, "Instruments", instrs);
		
		
//		QuerySpec spec = new QuerySpec() 
//				   .withKeyConditionExpression("instrtype = :nn") 
//				.withValueMap(new ValueMap()						
//				   .withInt(":nn", ShortType.CRYPTO.ordinal()));
//		ScanSpec scanSpec = new ScanSpec()
//				.withFilterExpression("instrtype = :nn")
//				.withValueMap(new ValueMap()						
//				   .withInt(":nn", ShortType.CRYPTO.ordinal()));
//
//				
//		Iterator<Item> itemIter = scan(db, "Instruments", scanSpec);
//		while(itemIter.hasNext()) {
//			LOG.info(itemIter.next().toJSONPretty());
//		}
		
		ShortInstrCache.getInstance(db);

	}

}
