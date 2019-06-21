package com.heliosapm.fix.grpc.utils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;


/**
 * <p>Title: ProtoUtil</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.utilsProtoUtil</code></p>
 * <p>2019</p>
 */
public class ProtoUtil {
	private static final Printer jsonPrinter = JsonFormat.printer();
	private static final Parser jsonParser = JsonFormat.parser();

	public static String printProto(GeneratedMessageV3 msg) {
		try {
			return jsonPrinter.print(msg);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to render message", ex);
		}
	}
	
	public static <T extends GeneratedMessageV3, E extends T.Builder<E>> T parseJson(CharSequence cs, Class<T> type) {
		E builder = Codecs.builder(type);
		try {
			jsonParser.merge(cs.toString(), builder);
			return (T)builder.build();
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
}
