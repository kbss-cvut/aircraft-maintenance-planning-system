package cz.cvut.kbss.amaplas.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WorkPackageLog {
    protected String wpId;
    protected List<Result> logs;

    public WorkPackageLog() {
    }

    public WorkPackageLog(String wpId, List<Result> logs) {
        this.wpId = wpId;
        this.logs = logs;
    }

    public String getWpId() {
        return wpId;
    }

    public void setWpId(String wpId) {
        this.wpId = wpId;
    }

    public List<Result> getLogs() {
        return logs;
    }

    public void setLogs(List<Result> logs) {
        this.logs = logs;
    }

    public static List<WorkPackageLog> from(Collection<Result> logs){
        return logs.stream().collect(Collectors.groupingBy(r -> r.wp)).entrySet().stream()
                .map(e -> new WorkPackageLog(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
