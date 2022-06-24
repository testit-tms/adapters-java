package ru.testit.testit.models.request;

import java.util.LinkedList;
import java.util.List;

public class TestResultsRequest
{
    private List<TestResultRequest> testResults;
    
    public TestResultsRequest() {
        this.testResults = new LinkedList<TestResultRequest>();
    }
    
    public List<TestResultRequest> getTestResults() {
        return this.testResults;
    }
}
