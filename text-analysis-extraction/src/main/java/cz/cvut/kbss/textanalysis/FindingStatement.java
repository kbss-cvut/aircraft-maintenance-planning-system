package cz.cvut.kbss.textanalysis;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Objects;

public class FindingStatement {

    final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    String errorMessage;
    String documentId;
    Integer documentLineNumber;
    int relativeWorkStepNumber;
    String workOrderId;
    String taskCardId;
    String originalText;
    String annotatedText;
    Resource component;
    Double componentScore;
    Resource failure;
    Double failureScore;
    List<Resource> multipleFailures;
    List<Resource> multipleComponents;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getDocumentLineNumber() {
        return documentLineNumber;
    }

    public void setDocumentLineNumber(Integer documentLineNumber) {
        this.documentLineNumber = documentLineNumber;
    }

    public int getRelativeWorkStepNumber() {
        return relativeWorkStepNumber;
    }

    public void setRelativeWorkStepNumber(int relativeWorkStepNumber) {
        this.relativeWorkStepNumber = relativeWorkStepNumber;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getTaskCardId() {
        return taskCardId;
    }

    public void setTaskCardId(String taskCardId) {
        this.taskCardId = taskCardId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getAnnotatedText() {
        return annotatedText;
    }

    public void setAnnotatedText(String annotatedText) {
        this.annotatedText = annotatedText;
    }

    public Resource getComponent() {
        return component;
    }

    public void setComponent(Resource component) {
        this.component = component;
    }

    public Double getComponentScore() {
        if (component != null){
            return Objects.requireNonNullElse(componentScore, 1.0);
        }
        return null;
    }

    public void setComponentScore(Double componentScore) {
        this.componentScore = componentScore;
    }

    public Resource getFailure() {
        return failure;
    }

    public void setFailure(Resource failure) {
        this.failure = failure;
    }

    public Double getFailureScore() {
        if (failure != null){
            return Objects.requireNonNullElse(failureScore, 1.0);
        }
        return null;
    }

    public void setFailureScore(Double failureScore) {
        this.failureScore = failureScore;
    }

    public List<Resource> getMultipleFailures() {
        return multipleFailures;
    }

    public void setMultipleFailures(List<Resource> multipleFailures) {
        this.multipleFailures = multipleFailures;
    }

    public List<Resource> getMultipleComponents() {
        return multipleComponents;
    }

    public void setMultipleComponents(List<Resource> multipleComponents) {
        this.multipleComponents = multipleComponents;
    }

    public String getComponentLabel() {
        if(component != null)
            return component
                    .getProperty(component.getModel().getProperty(PREF_LABEL))
                    .getObject().asLiteral().getValue().toString();
        else
            return "";
    }

    public String getMultipleComponentsLabels() {
        StringBuilder sb = new StringBuilder();
        getMultipleLabels(multipleComponents, sb);
        return sb.toString();
    }

    public String getFailureLabel() {
        if (failure != null)
            return failure
                    .getProperty(failure.getModel().getProperty(PREF_LABEL))
                    .getObject().asLiteral().getValue().toString();
        else
            return "";
    }

    public String getMultipleFailuresLabels() {
        StringBuilder sb = new StringBuilder();
        getMultipleLabels(multipleFailures, sb);
        return sb.toString();
    }

    private void getMultipleLabels(List<Resource> multipleFailures, StringBuilder sb) {
        if (multipleFailures != null && multipleFailures.size() > 1) {
            for (int i = 0; i < multipleFailures.size(); i++) {
                Resource f = multipleFailures.get(i);
                if (i > 0) sb.append("; ");
                sb.append(f.getProperty(f.getModel()
                                .getProperty(PREF_LABEL))
                        .getObject().asLiteral().getValue().toString());
            }
        }
    }

    public Double getAggregateScore() {
        return Objects.requireNonNullElse(componentScore, 1.0)
                * Objects.requireNonNullElse(failureScore, 1.0);
    }

    public String isConfirmed() {
        return (component != null && componentScore == null)
                || (failure != null && failureScore == null)
                ? "TRUE" : "FALSE";
    }
}
