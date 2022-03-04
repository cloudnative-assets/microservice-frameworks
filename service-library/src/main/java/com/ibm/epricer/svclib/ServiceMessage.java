package com.ibm.epricer.svclib;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * RPC message structure to hold service calls information. This logical structure will be mapped to
 * physical structure of actual wire protocol artifacts used to implement inter-service
 * communication, ex. rest, grpc, amqp, etc.
 * 
 * @author Kiran Chowdhury
 */

public class ServiceMessage {
    /* Well-known headers */
    public static final String SVCID_HEADER = "epricer-service-id";
    public static final String EPID_HEADER = "epricer-endpoint-id";
    public static final String EPVER_HEADER = "epricer-endpoint-ver";
    public static final String THROWS_HEADER = "epricer-throws";
    public static final String STATUS_HEADER = "epricer-status";

    public enum Status {
        SUCCESS(0), BUSINESS_EXCEPTION(1);

        public final int code;

        private Status(int code) {
            this.code = code;
        }

        public static Status fromString(@Nullable String code) {
            if (code == null) {
                return null;
            }
            for (Status s : values()) {
                if (s.code == Integer.valueOf(code)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("The status code is invalid: " + code);
        }

        @Override
        public String toString() {
            return String.valueOf(this.code);
        }
    }

    private Map<String, String> headers;
    private String payload;

    public ServiceMessage() {
        this.headers = new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH);
    }

    public void importResponseHeaders(Map<String, String> incomingHeaders) {
        String canThrow = incomingHeaders.get(THROWS_HEADER);
        if (canThrow != null) {
            this.headers.put(THROWS_HEADER, canThrow);
        }
        String status = incomingHeaders.get(STATUS_HEADER);
        if (status != null) {
            this.headers.put(STATUS_HEADER, status);
        }
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setServiceInfo(String serviceId, String endpointId, int endpointVer) {
        headers.put(SVCID_HEADER, serviceId);
        headers.put(EPID_HEADER, endpointId);
        headers.put(EPVER_HEADER, String.valueOf(endpointVer));
    }

    public String getServiceId() {
        return headers.get(SVCID_HEADER);
    }

    public String getEndpointId() {
        return headers.get(EPID_HEADER);
    }

    public int getEndpointVer() {
        return Integer.valueOf(headers.getOrDefault(EPVER_HEADER, "1"));
    }

    public Optional<Boolean> getThrows() {
        return Optional.ofNullable(toBooleanObject(headers.get(THROWS_HEADER)));
    }

    public void setThrows(boolean thro) {
        headers.put(THROWS_HEADER, String.valueOf(thro));
    }

    public Optional<Status> getStatus() {
        return Optional.ofNullable(Status.fromString(headers.get(STATUS_HEADER)));
    }

    public void setStatus(Status status) {
        headers.put(STATUS_HEADER, status.toString());
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public String toString() {
        return "ServiceMessage -> headers=" + headers + ", payload=\n" + payload;
    }
}
