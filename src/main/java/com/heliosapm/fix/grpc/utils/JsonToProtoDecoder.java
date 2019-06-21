package com.heliosapm.fix.grpc.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.protobuf.GeneratedMessageV3;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>Title: JsonToProtoDecoder</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpc.utilsJsonToProtoDecoder</code></p>
 * <p>2019</p>
 */
public class JsonToProtoDecoder <T extends GeneratedMessageV3> implements Decoder<T>  {
	  private static final MimeType JSON_TYPE = new MimeType("application", "json");
	  private static final MimeType PROTO_TYPE = new MimeType("application", "x-protobuf");

	  private static final List<MimeType> OUT_MIME_TYPES = Arrays.asList(PROTO_TYPE);
	  private static final List<MimeType> IN_MIME_TYPES = Arrays.asList(JSON_TYPE);
	  
	  private static final Logger LOG = LoggerFactory.getLogger(JsonToProtoDecoder.class);
	  
	  private final ObjectMapper objectMapper;
	  private final JsonFactory jsonFactory;
	  private final ObjectReader reader;
	  
	  
	  
	  
	  /**
	   * Creates a new JsonToProtoDecoder
	   * @param objectMapper A provided ObjectMapper. If null, a new one
	   * is created using default settings.
	   */
	  public JsonToProtoDecoder(ObjectMapper objectMapper) {
	    this.objectMapper = objectMapper==null ? new ObjectMapper() : objectMapper;
	    jsonFactory = this.objectMapper.getFactory().copy();
	    reader = this.objectMapper.readerFor(JsonNode.class);
	  }
	  
	  /**
	   * Creates a new JsonToProtoDecoder which uses a default settings ObjectMapper
	   */
	  public JsonToProtoDecoder() {
	    this(null);
	  }

	  /**
	   * {@inheritDoc}
	   * @see org.springframework.core.codec.Decoder#canDecode(org.springframework.core.ResolvableType, org.springframework.util.MimeType)
	   */
	  @Override
	  public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
	    return GeneratedMessageV3.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
//	    return true;
	  }
	  
	  /**
	   * Determines if this decoder supports decoding requests of the passed mime type.
	   * <b>NOTE</b>: If the mime type is null, we return true. This is because at initialization time,
	   * Webflux scans all the decoders calling {@link #canDecode(ResolvableType, MimeType)} on each decoder with
	   * a null mime type to index supported decoders for all known input types in the application. 
	   * @param mimeType The mime type to test for
	   * @return true if supported, false otherwise
	   */
	  protected boolean supportsMimeType(@Nullable MimeType mimeType) {
	    return (mimeType == null || OUT_MIME_TYPES.stream().anyMatch(m -> m.isCompatibleWith(mimeType))); 
	  }

	  private Flux<JsonNode> decodeInternal(Flux<TokenBuffer> tokens, ResolvableType elementType) {

	    Assert.notNull(tokens, "'tokens' must not be null");
	    return tokens.map(tokenBuffer -> {
	      try {
	        return reader.readTree(tokenBuffer.asParser(objectMapper));
	      }
	      catch (InvalidDefinitionException ex) {
	        throw new CodecException("Type definition error: " + ex.getType(), ex);
	      }
	      catch (JsonProcessingException ex) {
	        throw new DecodingException("JSON decoding error: " + ex.getOriginalMessage(), ex);
	      }
	      catch (IOException ex) {
	        throw new DecodingException("I/O error while parsing input stream", ex);
	      }
	    });
	  }
	  

	  /**
	   * {@inheritDoc}
	   * @see org.springframework.core.codec.Decoder#decode(org.reactivestreams.Publisher, org.springframework.core.ResolvableType, org.springframework.util.MimeType, java.util.Map)
	   */
	  @Override
	  public Flux<T> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType,
	      Map<String, Object> hints) {    
	    @SuppressWarnings("unchecked")
	    final Class<T> messageType = (Class<T>)elementType.toClass();
	    Flux<TokenBuffer> tokens = JsonTokenizer.tokenize(Flux.from(inputStream), jsonFactory, true);
	    Flux<JsonNode> nodes = decodeInternal(tokens, elementType);
	    return Flux.create(sink -> {
	      nodes.subscribe(
	          node -> {
	            T message = Codecs.parseJson(node.toString(), messageType);
	            sink.next(message);
	          },
	          err -> sink.error(err),
	          () -> sink.complete()
	      );
	    });
	  }

	  /**
	   * {@inheritDoc}
	   * @see org.springframework.core.codec.Decoder#decodeToMono(org.reactivestreams.Publisher, org.springframework.core.ResolvableType, org.springframework.util.MimeType, java.util.Map)
	   */
	  @Override
	  public Mono<T> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType,
	      Map<String, Object> hints) {
	    LOG.info("Mono Reading {}", elementType.toClass().getName());
	    @SuppressWarnings("unchecked")
	    final Class<T> messageType = (Class<T>)elementType.toClass();
	    Flux<TokenBuffer> tokens = JsonTokenizer.tokenize(Flux.from(inputStream), this.jsonFactory, true);
	    Mono<JsonNode> node = decodeInternal(tokens, elementType).singleOrEmpty();
	    return Mono.create(sink -> {
	      node.subscribe(
	          n -> sink.success(Codecs.parseJson(node.toString(), messageType)),
	          err -> sink.error(err)
	      );
	    });
	  }

	  /**
	   * {@inheritDoc}
	   * @see org.springframework.core.codec.Decoder#getDecodableMimeTypes()
	   */
	  @Override
	  public List<MimeType> getDecodableMimeTypes() {
	    return IN_MIME_TYPES;
	  }
}
