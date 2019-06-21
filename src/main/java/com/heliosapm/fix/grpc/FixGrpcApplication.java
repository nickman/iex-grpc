package com.heliosapm.fix.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;



/**
 * <p>Title: FixGrpcApplication</p>
 * <p>Description: </p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.fix.grpcFixGrpcApplication</code></p>
 * <p>2019</p>
 */
@SpringBootApplication(scanBasePackages = { 
	    "com.heliosapm.fix.grpc"
})
public class FixGrpcApplication {
	private static final Logger LOG = LoggerFactory.getLogger(FixGrpcApplication.class);
	private static ApplicationContext ctx; 
	
	public static void main(String[] args) {
		System.setProperty("spring.main.allow-bean-definition-overriding", "true");
		System.setProperty("management.endpoints.web.exposure.include", "*");
		ctx = new SpringApplicationBuilder(FixGrpcApplication.class).web(WebApplicationType.REACTIVE).run(args);
	}

}
