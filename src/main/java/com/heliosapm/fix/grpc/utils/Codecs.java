package com.heliosapm.fix.grpc.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;

import reactor.core.publisher.Flux;

/**
 * <p>Title: Codecs</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.utilsCodecs</code></p>
 * <p>2019</p>
 */
public class Codecs {
	  /** Method handle lookup for acquiring <code>GeneratedMessageV3.newBuilder()</code> handles */
	  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	  /** Decoder to decode from JSON to Proto Messages */
	  private static final JsonToProtoDecoder<? extends GeneratedMessageV3> jsonDecoder = new JsonToProtoDecoder<>();
	  /** Decoder to decode from x-protobuf to Proto Messages */
	  private static final ProtobufDecoder protoDecoder = new ProtobufDecoder();
	  /** Proto's own JSON parser */
	  private static final Parser jsonParser = JsonFormat.parser();
	  /** JSON mime type */
	  private static final MediaType JSON_TYPE = new MediaType("application", "json");
	  /** Proto mime type */
	  private static final MediaType PROTO_TYPE = new MediaType("application", "x-protobuf");
	  /** A cache for builder-getter method handles */
	  private static final ConcurrentMap<Class<? extends GeneratedMessageV3>, MethodHandle> methodHandleCache 
	  = new ConcurrentReferenceHashMap<>();

	  /**
	   * Acquires a builder for the passed proto message type
	   * @param type the proto message type to get a builder for 
	   * @return the builder
	   */
	  @SuppressWarnings({"unchecked" })
	  public static <T extends GeneratedMessageV3, E extends T.Builder<E>> E builder(Class<T> type) {
	    MethodHandle mh = methodHandleCache.computeIfAbsent(type, t -> buildMethodHandle(t));
	    try {      
	      Object o = mh.invokeExact();
	      return (E)o;
	    } catch (Throwable ex) {
	      throw new RuntimeException("Failed to create builder for type: " + type.getName(), ex);
	    }
	  }

	  /**
	   * Builds a method handle for the static method <code>type.newBuilder()</code>
	   * @param type The proto message type
	   * @return the method handle
	   */
	  private static MethodHandle buildMethodHandle(Class<?> type) {
	    try {
	      Class<?> returnType = type.getDeclaredMethod("newBuilder").getReturnType();
	      return lookup.findStatic(type, "newBuilder", MethodType.methodType(returnType))
	          .asType(MethodType.methodType(Object.class));
	    } catch (Exception ex) {
	      throw new RuntimeException("Failed to create MethodHandle for type: " + type.getName(), ex);
	    }
	  }

	  /**
	   * Parses a JSON string into a proto message
	   * @param content The JSON to parse
	   * @param messageType The proto message type
	   * @return the proto message
	   */
	  @SuppressWarnings({ "rawtypes", "unchecked" })
	  public static <T extends GeneratedMessageV3> T parseJson(String content, Class<T> messageType) {
	    try {
	      GeneratedMessageV3.Builder builder = builder(messageType);
	      jsonParser.merge(content, builder);
	      return (T)builder.build();
	    } catch (Exception ex) {
	      throw new RuntimeException("Failed to render message", ex);
	    }
	  }

	  /**
	   * Parses an incoming flux of JSON encoded data buffers into proto messages
	   * @param buffers The flux of deferred data buffers
	   * @param messageType The proto message type to decode as
	   * @return A flux of deferred parsed proto messages
	   */
	  public static <T extends GeneratedMessageV3> Flux<T> parseJson(Flux<DataBuffer> buffers, Class<T> messageType) {
	    return Flux.create(sink -> {
	      try {        
	        jsonDecoder.decode(buffers, ResolvableType.forClass(messageType), null, null)
	          .cast(messageType)
	          .subscribe(
	              msg -> sink.next(msg),
	              err -> sink.error(err),
	              () -> sink.complete()
	            );
	      } catch (Exception ex) {
	        sink.error(ex);
	      }
	    });
	  }

	  /**
	   * Parses an incoming flux of x-protobuf encoded data buffers into proto messages
	   * @param buffers The flux of deferred data buffers
	   * @param messageType The proto message type to decode as
	   * @return A flux of deferred parsed proto messages
	   */
	  public static <T extends GeneratedMessageV3> Flux<T> parseProto(Flux<DataBuffer> buffers, Class<T> messageType) {
	    return Flux.create(sink -> {
	      try {
	        protoDecoder.decode(buffers, ResolvableType.forClass(messageType), null, null)
	            .cast(messageType).subscribe(
	                msg -> sink.next(msg),
	                err -> sink.error(err),
	                () -> sink.complete()
	            );
	      } catch (Exception ex) {
	        sink.error(ex);
	      }
	    });
	  }
	  
	  /**
	   * Parses the body of an incoming http request into proto messages
	   * @param reactiveRequest The http request
	   * @param messageType The proto message type to decode as
	   * @return A flux of deferred parsed proto messages
	   */
	  public static <T extends GeneratedMessageV3> Flux<T> readProtoPayload(ServerHttpRequest reactiveRequest, Class<T> messageType) {
	    MediaType mediaType = reactiveRequest.getHeaders().getContentType();
	    if (JSON_TYPE.isCompatibleWith(mediaType)) {
	      return parseJson(reactiveRequest.getBody(), messageType);
	    } else if (PROTO_TYPE.isCompatibleWith(mediaType)) {
	      return parseProto(reactiveRequest.getBody(), messageType);
	    } else {
	      throw new IllegalArgumentException("Unsupported content type: [" + mediaType + "]");
	    }
	  }

	  private Codecs() {}

}
