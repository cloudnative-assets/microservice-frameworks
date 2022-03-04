package com.ibm.epricer.svclib;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Local representation of a remote service un-handled exception with REMOTE stack trace in a
 * communication-technology-neutral form.
 * 
 * @author Kiran Chowdhury
 */
public class RemoteUnhandledTechnicalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private RemoteUnhandledTechnicalException(String errorMessageBody) {
        super(extractMessage(errorMessageBody));
        this.setStackTrace(extractStackTrace(errorMessageBody));
    }

    /**
     * Factory method
     *  
     * @param errorMessageBody - error message body containing remote error message and remote stack trace
     * @return - exception created from the error message body
     */
    public static RemoteUnhandledTechnicalException deserialize(String errorMessageBody) {
        return new RemoteUnhandledTechnicalException(errorMessageBody);    
    }
    
    /*
     * Serialize exception into a string that can be used as a error message body. First line of the
     * resulting string has url-encoded exception message prefixed with the service id to show service
     * invocation history as the exception travels back to the client. All other lines are serialized
     * exception stack array using "|" delimiter.
     */
    public static String serialize(Throwable exception, String serviceId) {
        StringBuilder builder = new StringBuilder();
        String encodedMessage = URLEncoder.encode("@" + serviceId + " " + exception.getMessage(), StandardCharsets.UTF_8);
        builder.append(encodedMessage + "\n");
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            builder.append(String.format("%s|%s|%s|%d\n", 
                    stackTraceElement.getClassName(), 
                    stackTraceElement.getMethodName(), 
                    stackTraceElement.getFileName(), 
                    stackTraceElement.getLineNumber()));
        }
        return builder.toString();        
    }

    private static String extractMessage(String errorMessageBody) {
        return URLDecoder.decode(errorMessageBody.split("\n")[0], StandardCharsets.UTF_8);
    }
    
    private static StackTraceElement[] extractStackTrace(String errorMessageBody) {
        String[] errorBodyLines = errorMessageBody.split("\n");
        String[] stackTraceRows = Arrays.copyOfRange(errorBodyLines, 1, errorBodyLines.length);
        StackTraceElement[] stackTraceElements = new StackTraceElement[stackTraceRows.length];
        for (int i = 0; i < stackTraceRows.length; i++) {
            String[] stackTraceRow = stackTraceRows[i].split("\\|");
            stackTraceElements[i] = new StackTraceElement(stackTraceRow[0], stackTraceRow[1], 
                    stackTraceRow[2], Integer.parseInt(stackTraceRow[3]));
        }
        return stackTraceElements;
    }
}