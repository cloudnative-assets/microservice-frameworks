package com.ibm.epricer.svclib.devops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Condition {

	private String status;
	private String metricKey;
	private String comparator;
	private String errorThreshold;
	private String actualValue;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMetricKey() {
		return metricKey;
	}

	public void setMetricKey(String metricKey) {
		this.metricKey = metricKey;
	}

	public String getComparator() {
		return comparator;
	}

	public void setComparator(String comparator) {
		this.comparator = comparator;
	}

	public String getErrorThreshold() {
		return errorThreshold;
	}

	public void setErrorThreshold(String errorThreshold) {
		this.errorThreshold = errorThreshold;
	}

	public String getActualValue() {
		return actualValue;
	}

	public void setActualValue(String actualValue) {
		this.actualValue = actualValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Condition [status=").append(status).append(", metricKey=").append(metricKey)
				.append(", comparator=").append(comparator).append(", errorThreshold=").append(errorThreshold)
				.append(", actualValue=").append(actualValue).append("]");
		return builder.toString();
	}

}
