package com.traceops.traceops.service;

import com.traceops.traceops.model.DetectedIncident;
import com.traceops.traceops.model.RootCauseAnalysis;
import org.springframework.stereotype.Service;

@Service
public class ProjectRcaAnalyzerService {

    public RootCauseAnalysis analyze(
            DetectedIncident incident) {

        String message =
                incident.getMessage() == null
                        ? ""
                        : incident.getMessage();

        String lower =
                message.toLowerCase();

        String issue =
                message;

        String likelyCause =
                "The code contains a condition that can cause an exception.";

        String trigger =
                "The detected code path is executed.";

        String suggestedResolution =
                "Handle the exceptional condition explicitly and return an appropriate response.";

        /*
         * 1. Broad exception handling.
         *
         * Example:
         *
         * catch (Exception e)
         */
        if ("BROAD_EXCEPTION_HANDLING"
                .equalsIgnoreCase(incident.getType())) {

            issue =
                    "The method catches the generic Exception type instead of handling specific exceptions.";

            likelyCause =
                    "The application uses broad exception handling, which can hide the actual type and cause of an error.";

            trigger =
                    "An exception occurs inside the try block and is caught by the generic catch (Exception e) block.";

            suggestedResolution =
                    "Catch specific exceptions such as SQLException, DataAccessException, or other expected exception types and handle them separately. Log the original exception details when appropriate.";
        }

        /*
         * 2. findById().orElseThrow()
         */
        else if (lower.contains("findbyid")
                && lower.contains("orelsethrow")) {

            likelyCause =
                    "The requested entity may not exist in the database.";

            trigger =
                    "findById(id) can return an empty Optional when the requested ID does not exist.";

            suggestedResolution =
                    "Check whether the entity exists and return an appropriate HTTP 404 response instead of throwing a generic RuntimeException.";
        }

        /*
         * 3. NullPointerException
         */
        else if (lower.contains("nullpointerexception")) {

            likelyCause =
                    "An object may be null when the code attempts to use it.";

            trigger =
                    "A null reference is accessed during execution.";

            suggestedResolution =
                    "Validate the object before using it and handle the null case explicitly.";
        }

        /*
         * 4. SQLException
         */
        else if (lower.contains("sqlexception")) {

            likelyCause =
                    "A database operation may have failed.";

            trigger =
                    "The SQL operation can fail because of an invalid query, connection problem, constraint violation, or database issue.";

            suggestedResolution =
                    "Check the database operation, validate inputs, handle SQL exceptions properly, and verify database connectivity.";
        }

        /*
         * 5. TimeoutException
         */
        else if (lower.contains("timeoutexception")
                || lower.contains("timeout")) {

            likelyCause =
                    "An external operation may not have completed within the expected time.";

            trigger =
                    "The external operation exceeded its configured timeout.";

            suggestedResolution =
                    "Review timeout configuration, external service availability, and retry handling.";
        }

        /*
         * 6. ConnectionException
         */
        else if (lower.contains("connectexception")
                || lower.contains("connection refused")) {

            likelyCause =
                    "The application may be unable to connect to an external service.";

            trigger =
                    "The target service may be unavailable or the configured host/port may be incorrect.";

            suggestedResolution =
                    "Verify service availability, host, port, network configuration, and connection settings.";
        }

        /*
         * 7. IllegalArgumentException
         */
        else if (lower.contains("illegalargumentexception")) {

            likelyCause =
                    "A method may have received an invalid argument.";

            trigger =
                    "The supplied argument does not satisfy the method's expected conditions.";

            suggestedResolution =
                    "Validate input parameters before processing them and provide a meaningful validation response.";
        }

        /*
         * 8. NumberFormatException
         */
        else if (lower.contains("numberformatexception")) {

            likelyCause =
                    "A string may contain a value that cannot be converted to the expected numeric type.";

            trigger =
                    "The application attempts to parse an invalid numeric value.";

            suggestedResolution =
                    "Validate the input before parsing and handle invalid numeric values gracefully.";
        }

        /*
         * 9. FileNotFoundException
         */
        else if (lower.contains("filenotfoundexception")) {

            likelyCause =
                    "The application may be trying to access a file that does not exist.";

            trigger =
                    "The requested file path cannot be resolved to an existing file.";

            suggestedResolution =
                    "Validate the file path and existence before reading the file, and handle missing files appropriately.";
        }

        return new RootCauseAnalysis(
                incident.getType(),
                incident.getClassName(),
                incident.getMethodName(),
                issue,
                likelyCause,
                trigger,
                suggestedResolution
        );
    }
}