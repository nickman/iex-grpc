package com.heliosapm.fix.grpc.instrument;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.drivewealth.grpc.api.instrument.ShortInstrument;
import com.drivewealth.grpc.api.instrument.ShortType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.protobuf.Timestamp;


/**
 * <p>Title: ShortInstr</p>
 * <p>Description: Simple pojo for ShortInstrument storage</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.instrumentShortInstr</code></p>
 * <p>2019</p>
 */
@JsonDeserialize(using=ShortInstr.ShortInstrDeserializer.class)
public class ShortInstr {	
	protected String symbol = null;
	protected String name = null;
	protected long date = -1;
	protected boolean enabled = false;
	protected ShortType type = null;
	protected int iexId = -1;
	
	// [{"symbol":"A","name":"Agilent Technologies Inc.","date":"2019-01-23","isEnabled":true,"type":"cs","iexId":"2"}]
	/**
	 * Returns the symbol
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}
	/**
	 * Sets the symbol
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	/**
	 * Returns the name
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Returns the date
	 * @return the date
	 */
	public long getDate() {
		return date;
	}
	/**
	 * Sets the date
	 * @param date the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}
	/**
	 * Returns the enabled
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * Sets the enabled
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * Returns the type
	 * @return the type
	 */
	public ShortType getType() {
		return type;
	}
	/**
	 * Sets the type
	 * @param type the type to set
	 */
	public void setType(ShortType type) {
		this.type = type;
	}
	/**
	 * Returns the iexId
	 * @return the iexId
	 */
	public int getIexId() {
		return iexId;
	}
	/**
	 * Sets the iexId
	 * @param iexId the iexId to set
	 */
	public void setIexId(int iexId) {
		this.iexId = iexId;
	}
	
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ShortInstr [\n\tsymbol=" + symbol + "\n\tname=" + name + "\n\tdate=" + new Date(date) + "\n\tenabled=" + enabled + "\n\ttype="
				+ type + "\n\tiexId=" + iexId + "\n]";
	}

	public Item toItem() {
		return new Item().withPrimaryKey("iexId", iexId, "symbol", symbol)
				.withLong("date", date)
				.withString("name", name)
				.withBoolean("isEnabled", enabled)
				.withInt("instrtype", type.ordinal());				
	}
	
	public static ShortInstr fromItem(Item item) {
		ShortInstr si = new ShortInstr();
		si.symbol = item.getString("symbol");
		si.iexId = item.getInt("iexId");
		si.date = item.getLong("date");
		si.enabled = item.getBoolean("isEnabled");
		si.type = ShortType.forNumber(item.getInt("instrtype"));
		si.name = item.getString("name");
		return si;
	}
	
	public ShortInstrument toShortInstrument() {
		return ShortInstrument.newBuilder()
			.setDate(Timestamp.newBuilder().setSeconds(TimeUnit.MILLISECONDS.toSeconds(date)))
			.setEnabled(enabled)
			.setIexId(iexId)
			.setName(name)
			.setSymbol(symbol)
			.setType(type)			
			.build();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + iexId;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShortInstr other = (ShortInstr) obj;
		if (iexId != other.iexId)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}



	private static class ShortInstrDeserializer extends JsonDeserializer<ShortInstr> {
		private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd");
			}
		};
		
		private Date parse(String v) {
			try {
				return sdf.get().parse(v);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid date string: [" + v + "]", ex);
			}
		}
		
		private ShortType parseType(String v) {
			return ShortType.valueOf(v.replace("/", "").toUpperCase());
		}
		
		/**
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public ShortInstr deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ShortInstr instr = new ShortInstr();
			JsonNode node = p.readValueAsTree();
			instr.setSymbol(node.get("symbol").textValue());
			instr.setName(node.get("name").textValue());
			instr.setDate(parse(node.get("date").textValue()).getTime());
			instr.setEnabled(node.get("isEnabled").asBoolean());
			instr.setType(parseType(node.get("type").textValue()));
			instr.setIexId(node.get("iexId").asInt());
			return instr;
		}
		
	}
	
}

//[{"symbol":"A","name":"Agilent Technologies Inc.","date":"2019-01-23","isEnabled":true,"type":"cs","iexId":"2"}]
//string symbol = 10;
//string name = 20;
//google.protobuf.Timestamp date = 30;
//bool enabled = 40;
//ShortType type = 50;
//int32 iexId = 60;	

