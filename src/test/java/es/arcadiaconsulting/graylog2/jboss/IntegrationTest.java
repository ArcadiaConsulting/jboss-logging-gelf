package es.arcadiaconsulting.graylog2.jboss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import me.moocar.logbackgelf.TestServer;
import me.moocar.logbackgelf.util.InternetUtils;

import org.jboss.logging.MDC;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import es.arcadiaconsulting.graylog2.jboss.Graylog2Handler;
import es.arcadiaconsulting.graylog2.jboss.SyslogLevel;

public class IntegrationTest {

    private static final String longMessage = createLongMessage();
    private TestServer server;
    private String ipAddress;
    private String requestID;
    private String host;
    private ImmutableSet<String> fieldsToIgnore = ImmutableSet.of("level", "timestamp");
    private ImmutableMap<String, String> lastRequest = null;

    private static String createLongMessage() {
        Random rand = new Random();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            char theChar = (char) (rand.nextInt(30) + 65);
            for (int j = 0; j < 80; j++) {
                str.append(theChar);
            }
            str.append('\n');
        }
        return str.toString();
    }
    
    @BeforeClass
    public static void init(){
    }

    @Before
    public void setup() throws SocketException, UnknownHostException {
        server = TestServer.build();
        server.start();
        host = "Test";
        lastRequest = null;
    }
    
    @After
    public void teardown() {
        server.shutdown();
    }

    @Test
    public void test() throws IOException {

        Logger logger = Logger.getLogger(IntegrationTest.class.getName());
        logger.setLevel(java.util.logging.Level.ALL);
        Graylog2Handler handler = new Graylog2Handler();
        handler.setGraylog2ServerPort(6789);
        handler.setFormatter(new SimpleFormatter());
        handler.setStaticAdditionalFields(buildStaticFields());
        logger.addHandler(handler);

        // Basic Request
        String message = "Test general fields";
        logger.fine(message);
        sleep();
        lastRequest = server.lastRequest();
        assertTrue(lastRequest.containsKey("timestamp"));
        assertEquals((double)SyslogLevel.DEBUG_SEVERITY.getNumericValue(), lastRequest.get("level"));
        assertEquals(InternetUtils.getLocalHostName(), lastRequest.get("host"));
        assertTrue(lastRequest.get("full_message").endsWith("Test general fields\n"));
        assertTrue(lastRequest.get("short_message").endsWith("Test general fields\n"));
        
        // Test log levels
        message = "level test";
        logger.severe(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.ERROR_SEVERITY.getNumericValue(), lastRequest.get("level"));
        message = "level test";
        logger.warning(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.WARNING_SEVERITY.getNumericValue(), lastRequest.get("level"));
        message = "level test";
        logger.info(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.INFO_SEVERITY.getNumericValue(), lastRequest.get("level"));
        message = "level test";
        logger.fine(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.DEBUG_SEVERITY.getNumericValue(), lastRequest.get("level"));
        message = "level test";
        logger.finer(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.DEBUG_SEVERITY.getNumericValue(), lastRequest.get("level"));
        message = "level test";
        logger.finest(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals((double)SyslogLevel.DEBUG_SEVERITY.getNumericValue(), lastRequest.get("level"));


        // Test substitution works
        logger.log(java.util.logging.Level.FINE, "this is a test with ({0}) parameter", "this");
        sleep();
        lastRequest = server.lastRequest();
        assertTrue(lastRequest.get("full_message").endsWith("this is a test with (this) parameter\n"));
        assertTrue(lastRequest.get("short_message").endsWith("this is a test with (this) parameter\n"));

        // Test file and line are output for stack trace
        try {
            new URL("app://asdfs");
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "expected error", new IllegalStateException(e));
            sleep();
        }
        lastRequest = server.lastRequest();
        assertTrue(lastRequest.containsKey("exception"));
        assertTrue(lastRequest.get("exception").indexOf("MalformedURLException") >= 0);
        
        // Test static additional field
        message = "Testing with a static additional field";
        logger.fine(message);
        sleep();
        lastRequest = server.lastRequest();
        assertEquals("value1", lastRequest.get("field1"));
        
        
        // The shorten message
        message = "this is a very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooog message";
        logger.log(java.util.logging.Level.FINE, message);
        sleep();
        lastRequest = server.lastRequest();
        assertFalse(lastRequest.get("short_message").endsWith("message\n"));
        
        
        // Test MCD context
        message = "A message with contet params";
        MDC.put("aDynProperty", "aDynValue");
        logger.log(java.util.logging.Level.FINE, message);
        sleep();
        lastRequest = server.lastRequest();
        assertTrue(lastRequest.containsKey("aDynProperty"));
        assertEquals("aDynValue", lastRequest.get("aDynProperty"));
        
        
    }
    
    @Test
    public void testLogLevels() throws IOException {
    	
        Logger logger = Logger.getLogger(IntegrationTest.class.getName());
        logger.setLevel(java.util.logging.Level.WARNING);
        Graylog2Handler handler = new Graylog2Handler();
        handler.setGraylog2ServerPort(6789);
        handler.setFormatter(new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				return record.getMessage();
			}
		});
        logger.addHandler(handler);

        // Test a log levels (only warn and error shall appear) 
        logger.finest("This message shall not appears");
        sleep();
        lastRequest = server.lastRequest();
        assertNull(lastRequest);
        
        logger.finer("This message shall not appears");
        sleep();
        lastRequest = server.lastRequest();
        assertNull(lastRequest);
        
        logger.fine("This message shall not appears");
        sleep();
        lastRequest = server.lastRequest();
        assertNull(lastRequest);
        
        logger.info("This message shall not appears");
        sleep();
        lastRequest = server.lastRequest();
        assertNull(lastRequest);
        
        logger.warning("This warn message shall appears");
        sleep();
        lastRequest = server.lastRequest();
        assertEquals("This warn message shall appears", lastRequest.get("full_message"));
        
        logger.severe("This error message shall appears");
        sleep();
        lastRequest = server.lastRequest();
        assertEquals("This error message shall appears", lastRequest.get("full_message"));
    	
    }
    
    @Test
    public void testLogFilters() throws IOException {
    	
        Logger logger = Logger.getLogger(IntegrationTest.class.getName());
        logger.setLevel(java.util.logging.Level.ALL);
        Graylog2Handler handler = new Graylog2Handler();
        handler.setGraylog2ServerPort(6789);
        handler.setFormatter(new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				return record.getMessage();
			}
		});
        logger.addHandler(handler);
        logger.setFilter(new Filter() {
			
			@Override
			public boolean isLoggable(LogRecord record) {
				return record.getMessage().startsWith("!");
			}
		});
        
        
        logger.severe("This warn message shall appears");
        sleep();
        lastRequest = server.lastRequest();
        assertNull(lastRequest);
        
        logger.severe("!This warn message shall appears");
        sleep();
        lastRequest = server.lastRequest();
        assertEquals("!This warn message shall appears", lastRequest.get("full_message"));
        
        
    }
    
    

    private Map<String, String> buildStaticFields() {
    	Map<String, String> staticFileds = new HashMap<String, String>();
    	staticFileds.put("field1", "value1");
    	return staticFileds;
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
