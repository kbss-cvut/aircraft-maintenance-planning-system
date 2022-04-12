package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeDiagram {
    protected Map<String, Color> borderColorMap;
    protected Map<String, Color> colorMap;

    public void createImage(List<Result> list, String out, Function<List<Result>, List<BufferedImage>> f ){
        try {

            // into integer pixels
            List<BufferedImage> images = f.apply(list);
            if(images.size() == 1) {
                ImageIO.write(images.get(0), "PNG", new File(out + ".png"));
            }else {
                for (int i = 0; i < images.size(); i++) {
                    ImageIO.write(images.get(i), "PNG", new File(out + "-" + i + ".png"));
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Draw a time line of task types, each task type has its own lane given by the y coordinate. The x coordinate is
     * time, where each pixels is 10 min.
     * @param list
     */
    public List<BufferedImage> draw(List<Result> list){
        int stepInMinutes = 10;
        int step = stepInMinutes*60*1000; // minutes in pixel
        int h = 5; // pixels
        int gap = 2; // pixels
        int height = h + gap;

        // calculate constants
        // calculate y for each type
        Map<TaskType,Integer> ymap = new HashMap<>();
        int maxRows = 0;
        for(Result r : list){
            if(!ymap.containsKey(r.taskType)){
                ymap.put(r.taskType, maxRows++);
            }
        }
        // calculate minimum time
        long minTime = list.stream().mapToLong(r -> r.start.getTime()).min().getAsLong();
        long maxTime = list.stream().mapToLong(r -> r.end.getTime()).max().getAsLong();

        int totalWidth = (int)((maxTime - minTime)/step);
        // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
        BufferedImage bi = new BufferedImage(totalWidth, maxRows * height, BufferedImage.TYPE_INT_ARGB);

        System.out.println(String.format("creating image with size %d, %d", bi.getWidth(), bi.getHeight()));
        Graphics2D g = bi.createGraphics();

        // draw the diagram
        setColors(list);

        g.setBackground(Color.WHITE);
        for (Result r : list) {
            long s = r.start.getTime();
            long e = r.end.getTime();
            g.setColor(colorMap.get(r.wp));
            drawRectShape(
                    g,
                    r,
                    (int) ((s - minTime) / step),
                    ymap.get(r.taskType) * height,
                    (int) (e - s) / step,
                    h
            );
        }

        return breakDownImage(bi, 2000);
    }



    protected List<BufferedImage> breakDownImage(BufferedImage bi, int maxWidth){
        int totalWidth = bi.getWidth();
        int imageCount = totalWidth/maxWidth + 1;
        List<BufferedImage> ret = new ArrayList<>(imageCount);
        if(bi.getWidth() > maxWidth) {
            for (int j = 0; j < imageCount; j++) {
                int x = maxWidth * j;
                int segmentWidth = totalWidth - x;
                if (segmentWidth > maxWidth) {
                    segmentWidth = maxWidth;
                }
                BufferedImage biPart = bi.getSubimage(x,0, segmentWidth, bi.getHeight());
                ret.add(biPart);
            }
        }else{
            ret.add(bi);
        }
        return ret;
    }


    public List<BufferedImage> drawByDay(List<Result> list){
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<Result>> byDays = list.stream().collect(Collectors.groupingBy(r -> day.format(r.start)));

        byDays.entrySet().stream().filter(e ->{
            Result rr = e.getValue().stream().filter(r -> {
                Calendar c = Calendar.getInstance();
                c.setTime(r.start);
                String d = String.format("%d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
                boolean ret = !d.equals(e.getKey());
                return ret;
            }).findAny().orElse(null);
            return true;
        }).forEach(r -> {});

        int stepInMinutes = 5;
        int dayPixels = 24*60/stepInMinutes;
//        int step = stepInMinutes*60*1000; // milliseconds in pixel
        int h = 5; // pixels
        int gap = 2; // pixels
        int height = h + gap;
        int textHeight = 20;

        // calculate constants
        // calculate y for each type
        Map<TaskType,Integer> ymap = new HashMap<>();
        int maxRows = 0;
        List<Result> orderedTypes = byDays.entrySet().stream().sorted(Comparator.comparing(l -> l.getKey()))
                .flatMap(e -> e.getValue().stream().sorted(Comparator.comparing(r -> r.start))).collect(Collectors.toList());

        for(Result r : orderedTypes){
            if(!ymap.containsKey(r.taskType)){
                ymap.put(r.taskType, maxRows++);
            }
        }

        int totalWidth = dayPixels*byDays.size();
        int totalHeight = maxRows * height + textHeight;

        // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
        BufferedImage bi = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        setColors(list);
        g.setBackground(Color.WHITE);
        g.clearRect(0,0, totalWidth, totalHeight);
        List<Map.Entry<String, List<Result>>> days = byDays.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).collect(Collectors.toList());

        // paint rows

//        Color lightGrey = n
        g.setColor(Color.lightGray);
        for(int i = 0; i < ymap.size(); i++){
            if((i % 2) == 0)
                g.fillRect(0, textHeight + height*i - 1, totalWidth, h + 1);
        }

        Calendar c = Calendar.getInstance();
        for(int i = 0; i < byDays.size(); i ++){
            Map.Entry<String, List<Result>> e = days.get(i);

            // draw a day
            // write day
            g.setColor(Color.GRAY);
            int xOffset = dayPixels * i;
            g.drawLine(xOffset, 0, xOffset, totalHeight);
            g.drawLine(xOffset + dayPixels, 0,xOffset + dayPixels, totalHeight);
            g.setColor(Color.BLACK);
            centerDrawString(e.getKey(), g, xOffset, 0, dayPixels, textHeight);
            for (Result r : e.getValue()){
                c.setTime(r.start);
                int start = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE))/stepInMinutes + 1 ;
                c.setTime(r.end);
                int end =   (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE))/stepInMinutes + 1;
                drawRectShape(g, r, xOffset + start, textHeight + ymap.get(r.taskType) * height, end - start, h);
            }
        }

        return breakDownImage(bi, dayPixels*40);
    }

    protected void drawRectShape(Graphics2D g, Result r, int x, int y, int w, int h){
         drawRectShape(g, x, y, w, h,
                borderColorMap.get(r.taskType.getTaskcat()),
                colorMap.get(r.wp));
    }

    protected void drawRectShape(Graphics2D g, int x, int y, int w, int h, Color border, Color fill){
        if(border == null)
            border = Color.DARK_GRAY;
        if(fill == null)
            fill = Color.GRAY;
        g.setColor(fill);
        g.fillRect(x, y, w, h);
        g.setColor(border);
        g.drawRect(x, y, w, h);
    }

    protected void centerDrawString(String message, Graphics2D g, int x, int y, int w, int h){
        FontMetrics fontMetrics = g.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(message);
        int stringHeight = fontMetrics.getAscent();
        g.drawString(message, x + (w - stringWidth) / 2, (y + h) / 2 + stringHeight / 4);

    }

    protected void setColors(List<Result> list){
        borderColorMap = new HashMap<>();
        borderColorMap.put("task_card", Color.BLUE);
        borderColorMap.put("scheduled_work_order", Color.YELLOW);
        borderColorMap.put("maintenance_work_order", Color.RED);

        List<Color> colorPalette = Arrays.asList(
                Color.BLACK,
                new Color(0,0,220),
                new Color(0,150,150));
        List<String> wps = list.stream().map(r -> r.wp).distinct().collect(Collectors.toList());
        colorMap = new HashMap<>();
        for(int j = 0; j < wps.size(); j++){
            colorMap.put(wps.get(j), colorPalette.get(j));
        }
    }

}
