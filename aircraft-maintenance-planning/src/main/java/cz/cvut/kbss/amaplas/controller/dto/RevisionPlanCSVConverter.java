package cz.cvut.kbss.amaplas.controller.dto;

import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.values.DateUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RevisionPlanCSVConverter {
    protected StringBuilder sb = new StringBuilder();
    protected Map<TaskType, TaskExecution> taskExecutionMap;
    protected CSVPrinter printer;
    protected char delimiter = ',';

    public String convert(Workpackage workpackage, RevisionPlan plan){
        init();
        taskExecutionMap = new HashMap<>();
        workpackage.getTaskExecutions().stream()
                .filter(te -> te.getTaskType() != null)
                .forEach(te -> taskExecutionMap.put(te.getTaskType(), te));

        header();
        body(workpackage, plan);
        finish();

        return sb.toString();
    }

    protected void init(){
        sb = new StringBuilder();
        try {
            printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void finish(){
        try {
            printer.flush();
            printer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void header(){
        _row(
            new Row().listColumns().stream().map(Column::getName)
        );
    }

    protected void body(Workpackage workpackage, RevisionPlan plan){
        for(TaskPlan taskPlan : plan.streamPlanParts()
                .filter(p -> p instanceof TaskPlan).map(p -> (TaskPlan)p).collect(Collectors.toList())){
            Row row = createRow(workpackage, plan, taskPlan);
            _row(row.listColumns().stream().map(Column::getValue));
        }
    }

    protected Row createRow(Workpackage wp, RevisionPlan plan, TaskPlan task) {
        TaskType taskType = task.getTaskType();
        TaskExecution taskExecution = taskExecutionMap.get(taskType);
        Row row = new Row();
        row.wp.setValue(plan.getTitle());
        row.wp_start_planned_by_csat.setValue(
                wp == null ? null : wp.getPlannedStartTime()
        );
        row.wp_end_planned_by_csat.setValue(
                wp == null ? null : wp.getPlannedEndTime()
        );
        row.wp_duration_planned_by_csat.setValue(
                wp == null || wp.getPlannedStartTime() == null || wp.getPlannedEndTime() == null ?
                        "" :
                        (Duration.between(wp.getPlannedStartTime().atStartOfDay(), wp.getPlannedEndTime().atStartOfDay()).toDays() + 1) * 24  + ""
        );

        Date wp_start_session_based = wp.getTaskExecutions() == null || wp.getTaskExecutions().isEmpty() ?
                null:
                wp.getTaskExecutions().stream()
                        .map(te -> te.getStart())
                        .filter(s -> s != null)
                        .min(Date::compareTo)
                        .orElse(null);
        row.wp_start_session_based.setValue(wp_start_session_based);

        Date wp_end_session_based = wp.getTaskExecutions() == null || wp.getTaskExecutions().isEmpty() ?
                null:
                wp.getTaskExecutions().stream()
                        .map(te -> te.getEnd())
                        .filter(s -> s != null)
                        .max(Date::compareTo)
                        .orElse(null);

        row.wp_end_session_based.setValue(wp_end_session_based);

        row.wp_start_planned_by_alg.setValue(
                plan == null ? null : plan.getPlannedStartTime()
        );
        row.wp_end_planned_by_alg.setValue(
                plan == null ? null : plan.getPlannedEndTime()
        );
        row.wp_duration_planned_by_alg.setValue(
                plan == null || plan.getPlannedStartTime() == null || plan.getPlannedEndTime() == null ?
                        "" :
                        (plan.getPlannedEndTime().getTime() - plan.getPlannedStartTime().getTime())/1000./3600. + ""
        );
        row.task_code.setValue(taskType == null || taskType.getCode() == null ? "" : taskType.getCode());
        row.general_task_type.setValue(
                taskType == null || taskType.getDefinition() == null ?
                        null :
                        taskType.getDefinition().getTaskType()
        );
        row.task_description.setValue(
                taskType == null ? null : taskType.getTitle()
        );
        row.task_category.setValue(
                taskType == null ? null : taskType.getTaskcat()
        );
        row.task_main_scope.setValue(
                taskType == null || taskType.getScope() == null ? null : taskType.getScope().getAbbreviation()
        );

        row.referenced_task_type.setValue(Optional.ofNullable(taskExecution.getReferencedTasks())
                .map(c -> c.stream().map(rt -> rt.getTaskType().getCode())
                        .filter(s -> s!= null).collect(Collectors.joining(";"))
                ).orElse(null)
        );

        row.task_start_session_base.setValue(
                taskExecution == null ? null : taskExecution.getStart()
        );
        row.task_end_session_base.setValue(
                taskExecution == null ? null : taskExecution.getEnd()
        );
        row.task_work_time_session_based.setValue(
                taskExecution == null || taskExecution.getWorkSessions() == null || taskExecution.getWorkSessions().isEmpty() ?
                        "" :
                        taskExecution.getWorkSessions().stream().filter(s -> s.getDur() != null).mapToLong(s-> s.getDur()).sum()/1000./3600. + ""
        );
        row.est_min.setValue(
                taskExecution == null ? null : taskExecution.getEstMin() + ""
        );
        row.task_start_planned_by_alg.setValue(
                task == null ? null : task.getPlannedStartTime()
        );
        row.task_end_planned_by_alg.setValue(
                task == null ? null : task.getPlannedEndTime()
        );
        row.task_duration_planned_by_alg.setValue(
                task == null || task.getPlannedStartTime() == null ||  task.getPlannedEndTime() == null ?
                        "":
                        (task.getPlannedEndTime().getTime() - task.getPlannedStartTime().getTime())/1000./3600. + ""
        );
        row.task_work_time_planned_by_alg.setValue(
                task == null || task.getPlannedWorkTime() == null ?
                    "" :
                    task.getPlannedWorkTime()/1000./3600. + ""
        );
        return row;
    }

    protected void _row(Stream<String> columns){
        try {
            printer.printRecord(columns.collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    class Column {
        String name;
        String value = null;

        public Column(String name) {
            this.name = name;
        }
        public Column(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }


        public void setValue(Date date){
            setValue(date == null ? "" : DateUtils.formatDate(date));
        }

        public void setValue(LocalDate date){
            setValue(date == null ? "" : DateUtils.formatDate(date));
        }

        public void setValue(String value) {
            this.value = value == null ? "" : value;
        }
    }
    class Row {
        Column wp = new Column("wp");
        Column wp_start_planned_by_csat = new Column("wp_start_planned_by_csat");
        Column wp_duration_planned_by_csat = new Column("wp_duration_planned_by_csat");
        Column wp_end_planned_by_csat = new Column("wp_end_planned_by_csat");
        Column wp_start_session_based = new Column("wp_start_session_based");
        Column wp_end_session_based = new Column("wp_end_session_based");
        Column wp_start_planned_by_alg = new Column("wp_start_planned_by_alg");
        Column wp_end_planned_by_alg = new Column("wp_end_planned_by_alg");
        Column wp_duration_planned_by_alg = new Column("wp_duration_planned_by_alg");
        Column task_code = new Column("task_code");
        Column general_task_type = new Column("general_task_type");
        Column task_description = new Column("task_description");
        Column task_category = new Column("task_category");
        Column task_main_scope = new Column("task_main_scope");
        Column referenced_task_type = new Column("referenced_task_type");
        Column task_start_session_base = new Column("task_start_session_base");
        Column task_end_session_base = new Column("task_end_session_base");
        Column task_work_time_session_based = new Column("task_work_time_session_based");
        Column est_min = new Column("est_min");
        Column task_start_planned_by_alg = new Column("task_start_planned_by_alg");
        Column task_end_planned_by_alg = new Column("task_end_planned_by_alg");
        Column task_duration_planned_by_alg = new Column("task_duration_planned_by_alg");
        Column task_work_time_planned_by_alg = new Column("task_work_time_planned_by_alg");


        List<Column> columns;
        public Row() {
            columns = Arrays.asList(
                    wp, wp_start_planned_by_csat, wp_duration_planned_by_csat, wp_end_planned_by_csat, wp_start_session_based,
                    wp_end_session_based, wp_start_planned_by_alg, wp_end_planned_by_alg, wp_duration_planned_by_alg,
                    task_category, general_task_type, task_code, task_main_scope, referenced_task_type, task_description, task_start_session_base,
                    task_end_session_base, task_work_time_session_based, est_min, task_start_planned_by_alg,
                    task_end_planned_by_alg, task_duration_planned_by_alg, task_work_time_planned_by_alg
                    );
        }

        public List<Column> listColumns(){
            return Collections.unmodifiableList(columns);
        }
        void validate(){
            List<Column> invalidColumns = new ArrayList<>();
            for(Column c : listColumns()){
                if(c.getValue() == null)
                    throw new RuntimeException(String.format("Column"));
            }
        }
    }

    public static void main(String[] args) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);

        System.out.println((Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays() + 1) * 24  + "");
        System.out.println((Duration.between(start.atStartOfDay(), end.atStartOfDay()).toHours() + 1)  + "");
        System.out.println(Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays() * 24  + "");
    }
}
