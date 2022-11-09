package cz.cvut.kbss.textanalysis;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.SKOS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class Extraction {
    
    private final Pattern possitiveNumberPattern = Pattern.compile("\\d+");
    org.jsoup.nodes.Document document;
    Model slovnik;
    Resource componentRes;
    Resource failureRes;
    private Map<String, String> prefixes = new HashMap<>();
    private Map<String, Integer> invalidPatternsMessages = new HashMap<>();
    String inputDirectory;

    private static Map<String, String> resolvePrefixes(org.jsoup.nodes.Document document) {
        final Map<String, String> map = new HashMap<>(4);
        final Elements prefixElements = document.getElementsByAttribute(Constants.RDFa.PREFIX);
        prefixElements.forEach(element -> {
            final String prefixStr = element.attr(Constants.RDFa.PREFIX);
            final String[] prefixDefinitions = prefixStr.split("[^:] ");
            for (String def : prefixDefinitions) {
                final String[] split = def.split(": ");
                assert split.length == 2;
                map.put(split[0].trim(), split[1].trim());
            }
        });
        return map;
    }

    void init() {
        final InputStream slovnikIS = this.getClass().getResourceAsStream("/vocabulary/slovnik.ttl");
        slovnik = ModelFactory.createOntologyModel().read(slovnikIS, "", FileUtils.langTurtle);
        componentRes = slovnik.getResource("http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/component");
        failureRes = slovnik.getResource("http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/failure");
    }


    public void processDataset(String inputDirectory, String outputPath) throws IOException {
        this.inputDirectory = inputDirectory;
        transformAnnotatedFilesToCSV(getDirectoryFiles(inputDirectory), outputPath);
    }

    private List<String> getDirectoryFiles(String inputDirectory) throws IOException {
        Path directoryPath = Paths.get(
                Objects.requireNonNull(
                        Extraction.class.getClassLoader().getResource("private/csat/" + inputDirectory)
                ).getPath()
        );

        List<String> directoryFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(directoryPath)) {
            stream
                    .filter(f -> !Files.isDirectory(f))
                    .forEach(f -> directoryFiles.add(f.toString()));
        }

        return directoryFiles.stream().sorted().collect(Collectors.toList());
    }
    
    void transformAnnotatedFilesToCSV(List<String> filePaths, String outputFilePath) throws IOException {
        init();

        List<FindingStatement> allFindings = new LinkedList<>();
        for (String fp : filePaths) {
            allFindings.addAll(getFindings(fp));
        }

        // statistics
        Map<String, Integer> statistics = allFindings.stream()
                .collect(
                        groupingBy(
                                fs -> Optional.ofNullable(fs.getErrorMessage()).orElse("Valid values."),
                                summingInt(fs -> 1)
                        )
                );
        System.out.println("Statistics:\n" + statistics);

        BufferedWriter simpleWriter = new BufferedWriter(new FileWriter(outputFilePath, false));

        simpleWriter.append("DocumentId");
        simpleWriter.append(",");
        simpleWriter.append("DocumentLineNumber");
        simpleWriter.append(",");
        simpleWriter.append("WorkOrderId");
        simpleWriter.append(",");
        simpleWriter.append("TaskCardId");
        simpleWriter.append(",");
        simpleWriter.append("ComponentURI");
        simpleWriter.append(",");
        simpleWriter.append("ComponentLabel");
        simpleWriter.append(",");
        simpleWriter.append("ComponentScore");
        simpleWriter.append(",");
        simpleWriter.append("MultipleComponents");
        simpleWriter.append(",");
        simpleWriter.append("FailureURI");
        simpleWriter.append(",");
        simpleWriter.append("FailureLabel");
        simpleWriter.append(",");
        simpleWriter.append("FailureScore");
        simpleWriter.append(",");
        simpleWriter.append("MultipleFailures");
        simpleWriter.append(",");
        simpleWriter.append("AggregateScore");
        simpleWriter.append(",");
        simpleWriter.append("IsConfirmed");
        simpleWriter.append(",");
        simpleWriter.append("OriginalText");
        simpleWriter.append(",");
        simpleWriter.append("AnnotatedText");
        simpleWriter.append(",");
        simpleWriter.append("ErrorMessage");
        simpleWriter.append(",");
        simpleWriter.append("\n");
        allFindings.forEach(fs -> {
                    try {
                        simpleWriter.append(fs.getDocumentId());
                        simpleWriter.append(",");
                        simpleWriter.append(fs.getDocumentLineNumber().toString());
                        simpleWriter.append(",");
                        simpleWriter.append(fs.getWorkOrderId());
                        simpleWriter.append(",");
                        simpleWriter.append(fs.getTaskCardId());
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getComponent()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getComponentLabel()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getComponentScore()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getMultipleComponentsLabels()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getFailure()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getFailureLabel()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getFailureScore()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getMultipleFailuresLabels()));
                        simpleWriter.append(",");
                        simpleWriter.append(getStringValue(fs.getAggregateScore()));
                        simpleWriter.append(",");
                        simpleWriter.append(String.valueOf(fs.isConfirmed()));
                        simpleWriter.append(",");
                        simpleWriter.append(CsvUtils.sanitizeString(fs.getOriginalText()));
                        simpleWriter.append(",");
                        simpleWriter.append(CsvUtils.sanitizeString(fs.getAnnotatedText()));
                        simpleWriter.append(",");
                        simpleWriter.append(CsvUtils.sanitizeString(fs.getErrorMessage()));
                        simpleWriter.append(",");
                        simpleWriter.append("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        simpleWriter.close();

//        System.out.println("Invalid patterns: \n" + invalidPatternsMessages); //TODO: look at this
        System.out.println("Output written to " + outputFilePath);
    }

    private String getStringValue(Object value) {
        return Optional.ofNullable(value).map(Object::toString).orElse("");
    }

    private List<FindingStatement> getFindings(String filePath) throws IOException {

        final InputStream input = loadFile(filePath);
        document = Jsoup.parse(input, StandardCharsets.UTF_8.name(), "");
        prefixes = resolvePrefixes(document);

        int valid = 0;
        int all = 0;

        List<FindingStatement> findings = new LinkedList<>();

        Elements rows = document.getElementsByTag("tr");
        for (Element row : rows) {
            all++;
            Elements columns = row.getElementsByTag("td");
            FindingStatement fs = new FindingStatement();

            fs.setDocumentId(filePath.substring(filePath.lastIndexOf("csat")));

            // skip table header
            if (all == 1) {
                continue;
            }
            fs.setDocumentLineNumber(parsePositiveInteger(
                    columns.get(0).text().trim().replace(".", "")
            ));
            fs.setRelativeWorkStepNumber(1);
            fs.setWorkOrderId(
                    columns.get(1).text().trim()
            );
            fs.setTaskCardId(
                    columns.get(2).text().trim()
            );

            fs.setOriginalText(columns.get(3).text().trim());
            fs.setAnnotatedText(columns.get(3).html().trim());
            Element annotatedText = columns.get(3);
            Map<String, List<Element>> localAnnElements = new LinkedHashMap<>();
            mapRDFaTermOccurrenceAnnotations(annotatedText, localAnnElements);

            Map<Resource, Element> resources =
                    localAnnElements.values().stream()
                            .map(e -> e.get(0))
                            .collect(Collectors.toMap(
                                    (e) -> getResourceFromSpan(e, slovnik),
                                    Function.identity(),
                                    (e1, e2) -> e1.attributes().size() > e2.attributes().size() ? e1 : e2,
                                    LinkedHashMap::new
                            ));
            Map<Resource, Element> components = resources.entrySet().stream()
                    .filter(e -> isNarrowerThan(e.getKey(), componentRes))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map<Resource, Element> failures = resources.entrySet().stream()
                    .filter(e -> isNarrowerThan(e.getKey(), failureRes))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map<Resource, Element> unclassified = resources.entrySet().stream()
                    .filter(e -> !(isNarrowerThan(e.getKey(), componentRes) || isNarrowerThan(e.getKey(), failureRes)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            if (localAnnElements.isEmpty()) {
                fs.setErrorMessage("There are no annotations within this workstep.");

            } else if ((components.size() == 0) || (failures.size() == 0)) {
                fs.setErrorMessage("Missing at least one annotation of either component or failure.");

            } else if ((components.size() > 1) || (failures.size() > 1)) {
                fs.setErrorMessage("There one more than 2 annotations within this workstep.");

                if (components.size() > 1) {
                    fs.setMultipleComponents(new ArrayList<>(components.keySet()));
                }
                if (failures.size() > 1) {
                    fs.setMultipleFailures(new ArrayList<>(failures.keySet()));
                }
            }

            List<Map.Entry<Resource, Element>> sortedComponents = sortByScore(components);
            if (isScoreWinnerUnambiguous(sortedComponents)) {
                fs.setComponent(sortedComponents.get(0).getKey());
                fs.setComponentScore(parseScore(sortedComponents.get(0).getValue()));
            }

            List<Map.Entry<Resource, Element>> sortedFailures = sortByScore(failures);
            if (isScoreWinnerUnambiguous(sortedFailures)) {
                fs.setFailure(sortedFailures.get(0).getKey());
                fs.setFailureScore(parseScore(sortedFailures.get(0).getValue()));
            }

            if (fs.getErrorMessage() == null) {
                valid++;
            }

            findings.add(fs);
        }

        System.out.println("valid = "+ valid + " out of " + all);
        return findings;
    }

    private boolean isScoreWinnerUnambiguous(List<Map.Entry<Resource, Element>> sortedResources) {
        if (sortedResources.isEmpty()) {
            return false;
        }
        if (sortedResources.size() == 1) {
            return true;
        }

        Element e1 = sortedResources.get(0).getValue();
        Element e2 = sortedResources.get(1).getValue();

        return compareScore(e1, e2) < 1;
    }

    private List<Map.Entry<Resource, Element>> sortByScore(Map<Resource, Element> resources) {
        return resources.entrySet()
                .stream().sorted(
                        (e1, e2) -> compareScore(e1.getValue(), e2.getValue())
                ).collect(Collectors.toList());
    }

    private int compareScore(Element e1, Element e2) {
        double e1S = computeScore(e1)*(-1);
        double e2S = computeScore(e2)*(-1);
        return Double.compare(e1S, e2S);
    }

    private double computeScore(Element element) {
        Double res = parseScore(element);
        return (res == null) ? 10 : res;
    }

    private Double parseScore(Element element) {
        return parsePositiveDouble(element.attr("score"));
    }

    private List<List<Node>> splitByTag(Element element, String tagName) {
        List<List<Node>> result = new LinkedList<>();

        List<Node> currentLine = new LinkedList<>();
        for (Node node : element.childNodes()) {
            if (tagName.equals(node.nodeName())) {
                result.add(currentLine);
                continue;
            }
            currentLine.add(node);
        }

        result.add(currentLine);

        return result;
    }

    private Double parsePositiveDouble(String text) {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }

        return Double.parseDouble(trimmedText);
    }

    private Integer parsePositiveInteger(String text) {
        String trimmedText = text.trim();
        if (!possitiveNumberPattern.matcher(trimmedText).matches()) {
            return null;
        }

        return Integer.parseInt(trimmedText);
    }

    private Resource getResourceFromSpan(Element span, Model slovnik) {
        String resourceUri = span.attr("resource");
        return slovnik.getResource(resourceUri);
    }

    private boolean isNarrowerThan(Resource resource, Resource parent) {
        return resource.listProperties(SKOS.broader).toList().stream()
                .anyMatch(st -> st.getObject().equals(parent));

    }

    private void mapRDFaTermOccurrenceAnnotations(Element rootElement, Map<String, List<Element>> annotatedElements) {
        final Elements elements = rootElement.getElementsByAttribute(Constants.RDFa.ABOUT);
        for (Element element : elements) {
            if (isNotTermOccurrence(element)) {
                continue;
            }
            annotatedElements.computeIfAbsent(element.attr(Constants.RDFa.ABOUT), key -> new ArrayList<>())
                    .add(element);
        }
    }

    private boolean isNotTermOccurrence(Element rdfaElem) {
        if (!rdfaElem.hasAttr(Constants.RDFa.RESOURCE) && !rdfaElem.hasAttr(Constants.RDFa.CONTENT)) {
            return true;
        }
        final String typesString = rdfaElem.attr(Constants.RDFa.TYPE);
        final String[] types = typesString.split(" ");
        // Perhaps we should check also for correct property?
        for (String type : types) {
            final String fullType = fullIri(type);
            if (fullType.equals(Constants.VYSKYT_TERMU)) {
                return false;
            }
        }
        return true;
    }

    private String fullIri(String possiblyPrefixed) {
        possiblyPrefixed = possiblyPrefixed.trim();
        final int colonIndex = possiblyPrefixed.indexOf(':');
        if (colonIndex == -1) {
            return possiblyPrefixed;
        }
        final String prefix = possiblyPrefixed.substring(0, colonIndex);
        if (!prefixes.containsKey(prefix)) {
            return possiblyPrefixed;
        }
        final String localName = possiblyPrefixed.substring(colonIndex + 1);
        return prefixes.get(prefix) + localName;
    }

    public InputStream loadFile(String filePath) {
        String file = Paths.get(filePath).getFileName().toString();
        return Extraction.class.getClassLoader().getResourceAsStream("private/csat/" + inputDirectory + "/" + file);
    }
}