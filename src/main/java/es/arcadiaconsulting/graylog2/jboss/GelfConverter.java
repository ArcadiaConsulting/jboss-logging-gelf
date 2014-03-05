package es.arcadiaconsulting.graylog2.jboss;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.jboss.logging.MDC;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Responsible for formatting a log event into a GELF message
 */
public class GelfConverter {

    private final String facility;
    private final boolean useLoggerName;
    private final boolean useThreadName;
    private final Map<String, String> staticAdditionalFields;
    private final int shortMessageLength;
    private final String hostname;
    private final Gson gson;
    private Formatter messageFormatter;
    private Formatter shortMessageFormatter;

    public GelfConverter(String facility,
                         boolean useLoggerName,
                         boolean useThreadName,
                         Map<String, String> staticAdditionalFields,
                         int shortMessageLength,
                         String hostname,
                         Formatter messageFormatter, Formatter shortMessageFormatter) {

        this.facility = facility;
        this.useLoggerName = useLoggerName;
        this.useThreadName = useThreadName;
        this.staticAdditionalFields = staticAdditionalFields;
        this.shortMessageLength = shortMessageLength;
        this.hostname = hostname;
        this.messageFormatter = messageFormatter;
        this.shortMessageFormatter = shortMessageFormatter;

        // Init GSON for underscores
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
    }

    /**
     * Converts a log event into GELF JSON.
     *
     * @param logEvent The log event we're converting
     * @return The log event converted into GELF JSON
     */
    public String toGelf(LogRecord logEvent) {
        try {
            return gson.toJson(mapFields(logEvent));
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error creating JSON message", e);
        }
    }

    /**
     * Creates a map of properties that represent the GELF message.
     *
     * @param logEvent The log event
     * @return map of gelf properties
     */
    private Map<String, Object> mapFields(LogRecord logEvent) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("facility", facility);

        map.put("host", hostname);

        String message = this.messageFormatter.format(logEvent);

        map.put("full_message", message);
        map.put("short_message", truncateToShortMessage(message, logEvent));
        
        // Ever since version 0.9.6, GELF accepts timestamps in decimal form.
        double logEventTimeTimeStamp = logEvent.getMillis() / 1000.0;
        stackTraceField(map, logEvent);
        map.put("timestamp", logEventTimeTimeStamp);
        map.put("version", "1.0");
        map.put("level", javaLevel2SyslogLevel(logEvent));
        mcdFields(map);
        additionalFields(map, logEvent);
        staticAdditionalFields(map);
        return map;
    }

    /**
     * Method that generated dynamic params based on {@link MDC} context
     * @param map
     */
    protected void mcdFields(Map<String, Object> map) {
		Map<String, Object> mdcMap = MDC.getMap();
		if(MapUtils.isNotEmpty(mdcMap)) {
			for (Map.Entry<String, Object> entry : mdcMap.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
     * Transform java loggin levels into Syslog levels
     * @param logEvent
     * @return
     */
	protected int javaLevel2SyslogLevel(LogRecord logEvent) {
		
		int logLevel = logEvent.getLevel().intValue();		
		
		if(logLevel == Level.SEVERE.intValue()) {
			return SyslogLevel.ERROR_SEVERITY.getNumericValue();
		} else if(logLevel == Level.WARNING.intValue()) {
			return SyslogLevel.WARNING_SEVERITY.getNumericValue();
		}else if(logLevel == Level.INFO.intValue()){
			return SyslogLevel.INFO_SEVERITY.getNumericValue();
		}else if(logLevel == Level.CONFIG.intValue() || 
				logLevel == Level.FINE.intValue() ||
				logLevel == Level.FINER.intValue() ||
				logLevel == Level.FINEST.intValue()) {
			return SyslogLevel.DEBUG_SEVERITY.getNumericValue();
		} else {
			throw new IllegalArgumentException("Level " + logEvent.getLevel()
					+ " is not a valid level for a printing method");
		}
	}
	
	private void stackTraceField(Map<String, Object> map, LogRecord eventObject) {
		Throwable exception = eventObject.getThrown();
		if (exception != null) {
			StackTraceElement[] stackTraces = exception.getStackTrace();
			if (stackTraces != null && stackTraces.length > 0) {
				StackTraceElement lastStack = stackTraces[0];
				map.put("exception_file", lastStack.getFileName());
				map.put("exception_class", lastStack.getClassName());
				map.put("exception_method", lastStack.getMethodName());
				map.put("exception_line",
						String.valueOf(lastStack.getLineNumber()));
			}

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			try {
				exception.printStackTrace(pw);
				map.put("exception", sw.toString());
			} finally {
				try {
					sw.close();
				} catch (IOException e) {
				}
				pw.close();
			}
		}
	}

    /**
     * Converts the additional fields into proper GELF JSON
     *
     * @param map         The map of additional fields
     * @param eventObject The Logging event that we are converting to GELF
     */
    private void additionalFields(Map<String, Object> map, LogRecord eventObject) {

        if (useLoggerName) {
            map.put("_loggerName", eventObject.getLoggerName());
        }

        if (useThreadName) {
            map.put("_threadId", eventObject.getThreadID());
        }
    }

    private void staticAdditionalFields(Map<String,Object> map) {

        for (String key : staticAdditionalFields.keySet()) {
            map.put(key, (staticAdditionalFields.get(key)));
        }
    }

    private String truncateToShortMessage(String fullMessage, LogRecord logEvent) {
    	if(this.shortMessageFormatter != null) {
    		return this.shortMessageFormatter.format(logEvent);
    	}
    	
        if (fullMessage.length() > shortMessageLength) {
            return fullMessage.substring(0, shortMessageLength);
        }
        return fullMessage;
    }
}
