package ru.hzerr.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.StringJoiner;

public class ExecutionContextCollection {

    @JsonProperty("targetInfos")
    private ExecutionContext[] executionContexts;

    public ExecutionContext[] getExecutionContexts() {
        return executionContexts;
    }

    public void setExecutionContexts(ExecutionContext[] executionContexts) {
        this.executionContexts = executionContexts;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExecutionContextCollection.class.getSimpleName() + "[", "]")
                .add("executionContexts=" + Arrays.toString(executionContexts))
                .toString();
    }
}
