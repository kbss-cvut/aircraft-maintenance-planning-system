package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Sparql;

import java.net.URI;

@OWLClass(iri = "http://www.w3.org/ns/csvw#Row")
public class FailureAnnotation extends AbstractEntity {

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/AnnotatedText")
    protected String annotatedText;

    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/FinalComponentUri")
    protected URI componentUri;
    @Sparql(query = "SELECT ?componentLabel {?this " +
            "<http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/FinalComponentUri>/" +
            "<http://www.w3.org/2004/02/skos/core#prefLabel> ?componentLabel . }")
//    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/componentLabel")
    protected String componentLabel;

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/ComponentScore")
    protected Double componentScore;

    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/FinalFailureUri")
    protected URI failureUri;
    @Sparql(query = "SELECT ?failureLabel {?this " +
            "<http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/FinalFailureUri>/" +
            "<http://www.w3.org/2004/02/skos/core#prefLabel> ?failureLabel . }")
//    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/failureLabel")
    protected String failureLabel;

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/FailureScore")
    protected Double failureScore;
//
    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/AggregateScore")
    protected Double aggregateScore;
//
    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/csat/enhance-wo-text-0.1/IsConfirmed")
    protected String isConfirmed;

    public String getAnnotatedText() {
        return annotatedText;
    }

    public void setAnnotatedText(String annotatedText) {
        this.annotatedText = annotatedText;
    }

    public URI getComponentUri() {
        return componentUri;
    }

    public void setComponentUri(URI componentUri) {
        this.componentUri = componentUri;
    }

    public String getComponentLabel() {
        return componentLabel;
    }

    public void setComponentLabel(String componentLabel) {
        this.componentLabel = componentLabel;
    }

    public Double getComponentScore() {
        return componentScore;
    }

    public void setComponentScore(Double componentScore) {
        this.componentScore = componentScore;
    }

    public URI getFailureUri() {
        return failureUri;
    }

    public void setFailureUri(URI failureUri) {
        this.failureUri = failureUri;
    }

    public String getFailureLabel() {
        return failureLabel;
    }

    public void setFailureLabel(String failureLabel) {
        this.failureLabel = failureLabel;
    }

    public Double getFailureScore() {
        return failureScore;
    }

    public void setFailureScore(Double failureScore) {
        this.failureScore = failureScore;
    }

    public Double getAggregateScore() {
        return aggregateScore;
    }

    public void setAggregateScore(Double aggregateScore) {
        this.aggregateScore = aggregateScore;
    }

    public String getConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(String confirmed) {
        isConfirmed = confirmed;
    }
}
