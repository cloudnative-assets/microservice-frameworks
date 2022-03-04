package com.ibm.epricer.svclib.devops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class CodeQualityAnalyzer {

	private static final Logger LOG = LoggerFactory.getLogger(CodeQualityAnalyzer.class);
	private static final String EQUALS_SYMBOL = "=";
	private static final String SONAR_REPORT_TASK_FILE = "target/sonar/report-task.txt";
	private static final String PROJECT_STATUS_ENDPOINT = "/api/qualitygates/project_status?analysisId=";
	private static final String TASK_URL_KEY = "ceTaskUrl";
	private static final String SERVER_URL_KEY = "serverUrl";
	private static final int WAIT_TIME_BETWEEN_RETRY = 25000;
	private static final String CODE_ANALYSIS_FAILED_MSG = "Code analysis failed";

	private static RestTemplate restTemplate = new RestTemplate();
	private static Map<String, String> reportTaskDetails = new HashMap<>();

	static {
		readReportTaskDetails();
	}

	/*
	 * main method
	 */
	public static void main(String[] args) {

		try {
			if (validateQuality()) {
				LOG.info("Congratulations, code quality analysis passed!");
			} else {
				throw new IllegalStateException("Code quality could be better.");
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(CODE_ANALYSIS_FAILED_MSG, e);
			throw new IllegalStateException(CODE_ANALYSIS_FAILED_MSG);
		}
	}

	/*
	 * Wait for milliseconds
	 */
	private static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/*
	 * Fetches the report data from sonar
	 */
	private static Optional<QualityReportResponse> fetchQualityReport(String analysisId) {

		String endpoint = reportTaskDetails.get(SERVER_URL_KEY).concat(PROJECT_STATUS_ENDPOINT).concat(analysisId);
		return Optional.of(restTemplate.getForObject(endpoint, QualityReportResponse.class));

	}

	/*
	 * Calls the task api and returns the result
	 */
	private static Optional<TaskDetailsResponse> fetchTaskDetails() {
		String taskEndpoint = reportTaskDetails.get(TASK_URL_KEY);
		return Optional.of(restTemplate.getForObject(taskEndpoint, TaskDetailsResponse.class));
	}

	/*
	 * Retrieve Sonar analysis id
	 */
	private static String getAnalysisId() {

		Optional<TaskDetailsResponse> taskDetailsOpt = fetchTaskDetails();
		TaskDetailsResponse taskDetails = taskDetailsOpt.orElseThrow();

		LOG.info("Task Details: {}", taskDetails);

		Task task = taskDetails.getTask();
		switch (task.getStatus()) {
		case "IN_PROGRESS":
		case "PENDING": {
			LOG.info("Code analysis is still in progress, waiting...");
			wait(WAIT_TIME_BETWEEN_RETRY);
			return getAnalysisId();
		}
		case "SUCCESS": {
			LOG.info("Code analysis finished successfully.");
			return task.getAnalysisId();
		}
		default:
			// Any other status is a failure
			throw new IllegalStateException(CODE_ANALYSIS_FAILED_MSG);
		}
	}

	/*
	 * Read report-task.txt file from target/sonar folder
	 */
	private static void readReportTaskDetails() {

		try {
			Path path = Paths.get(SONAR_REPORT_TASK_FILE);

			List<String> lines = Files.readAllLines(path);
			reportTaskDetails = lines.stream().map(str -> str.split(EQUALS_SYMBOL, 2))
					.collect(Collectors.toMap(str -> str[0], str -> str[1]));

		} catch (IOException e) {
			LOG.error("Error while reading {}", SONAR_REPORT_TASK_FILE, e);
		}
	}

	/*
	 * Returns true if it passes the quality gate check
	 */
	private static boolean validateQuality() {
		Optional<QualityReportResponse> qualityReportOpt = fetchQualityReport(getAnalysisId());
		QualityReportResponse qualityReport = qualityReportOpt.orElseThrow();
		LOG.info("Quality Gate Response: {}", qualityReport);
		return StringUtils.equals(qualityReport.getProjectStatus().getStatus(), "OK");
	}

}
