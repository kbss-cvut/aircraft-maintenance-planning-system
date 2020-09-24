package cz.cvut.kbss.amaplas.exp.dataanalysis.model;

public class Task {
    public String task;
    public long start;
    public String wp;

    public Task(String task, long start, String wp) {
        this.task = task;
        this.start = start;
        this.wp = wp;
    }

    @Override
    public String toString() {
        return "Task{" +
                "task='" + task + '\'' +
                ", start=" + start +
                ", wp='" + wp + '\'' +
                '}';
    }
}
