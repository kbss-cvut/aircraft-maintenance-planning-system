package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphics;

import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class GMatrix {

    protected BiFunction<Integer, Integer, Float> shadeFunction;
    protected Rectangle dimension;


    public GMatrix() {
    }

    public GMatrix(GMatrix gMatrix) {
        this(gMatrix.shadeFunction, gMatrix.dimension);
    }

    public GMatrix(double[][] matrix) {
        this((i,j) -> (float)matrix[i][j], new Rectangle(matrix[0].length, matrix.length));
    }


    public GMatrix(Function<Integer, Function<Integer, Float>> shadeFunction, Rectangle dimension){
        this((i,j) -> shadeFunction.apply(i).apply(j), dimension);
    }

    public void draw(Graphics2D g, int psize, Function<Float, Color> coloring){
        drawMatrix(g, shadeFunction, dimension, psize, coloring);
    }

    public GMatrix(BiFunction<Integer, Integer, Float> shadeFunction, Rectangle dimension) {
        this.shadeFunction = shadeFunction;
        this.dimension = dimension;
    }

    public BiFunction<Integer, Integer, Float> getShadeFunction() {
        return shadeFunction;
    }

    public void setShadeFunction(BiFunction<Integer, Integer, Float> shadeFunction) {
        this.shadeFunction = shadeFunction;
    }

    public Rectangle getDimension() {
        return dimension;
    }

    public void setDimension(Rectangle dimension) {
        this.dimension = dimension;
    }

    public double getX() {
        return dimension.getX();
    }

    public double getY() {
        return dimension.getY();
    }

    public double getWidth() {
        return dimension.getWidth();
    }

    public double getHeight() {
        return dimension.getHeight();
    }

    public double getMaxX() {
        return dimension.getMaxX();
    }

    public double getMaxY() {
        return dimension.getMaxY();
    }

    public GMatrix transpose(){
        return new GMatrix((i, j) -> shadeFunction.apply(j, i), new Rectangle(
                dimension.y, dimension.x,
                dimension.height, dimension.width
                ));
    }

    public static void drawMatrix(Graphics2D g, double[][] matrix, int psize, Function<Float, Color> coloring){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                float shade = (float) matrix[i][j];
                g.setColor(coloring.apply(shade));
                g.fillRect(j*psize, i*psize, psize, psize);
            }
        }
    }

    public static void drawMatrix(Graphics2D g, BiFunction<Integer, Integer, Float> shadeFunction, Rectangle r, int psize, Function<Float, Color> coloring){
        for(int i = r.y; i < r.y + r.height; i++){
            for(int j = r.x; j < r.x + r.width; j++){
                float shade = shadeFunction.apply(i,j);
                g.setColor(coloring.apply(shade));
                g.fillRect(j*psize, i*psize, psize, psize);
            }
        }
    }
    // TODO - finish the implementation

    /**
     * This prints the m parameter as CSV.
     * @param fileName
     * @param x
     * @param y
     * @param m
     */
    public static void toTable(String fileName, Function<Integer, String> x, Function<Integer, String> y, GMatrix m) {
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            // print header
            StringBuilder sb = new StringBuilder();
            IntStream.range((int)m.getX(), (int)m.getMaxX())
                    .forEach(i -> sb.append(';').append(x.apply(i)));

            ps.println(sb);
            int[] yyy = new int[1];
            for (int i = (int)m.getY(); i < m.getY() + m.getHeight(); i++) {
                yyy[0] = i;
                String yValue = y.apply(i);
                StringBuilder sb2 = new StringBuilder().append(yValue);
                IntStream.range((int)m.getX(), (int)m.getMaxX())
                        .forEach(j -> sb2.append(';').append(m.getShadeFunction().apply(yyy[0], j)));
                ps.println(sb2.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * writes the input gMatrix in a three column table, two columns for the coordinates and one column for the function
     * value. The coordinates are translated from integer to string values using the parameter x and y functions
     * @param fileName
     * @param xLabel
     * @param yLabel
     * @param valueLabel
     * @param x
     * @param y
     * @param m
     */
    public static void linearTable(String fileName, String xLabel, String yLabel, String valueLabel, Function<Integer, String> x, Function<Integer, String> y, GMatrix m){
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println(xLabel + ";" + yLabel + ";" + valueLabel);
            for (int i = (int)m.getY(); i < m.getMaxY(); i++) {
                for (int j = (int)m.getX(); j < m.getMaxX(); j++) {
                    float shade = m.getShadeFunction().apply(i, j);
                    StringBuilder sb = new StringBuilder()
                            .append(x.apply(j))
                            .append(';')
                            .append(y.apply(i))
                            .append(';')
                            .append(shade);
                    ps.println(sb.toString());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * Similar to <code>linearTable</code>. It prints only a diagonal matrix where the coordinates satisfy x > y.
     * This is useful to write symmetric matrices.
     * @param fileName
     * @param xLabel
     * @param yLabel
     * @param valueLabel
     * @param x
     * @param y
     * @param m
     */
    public static void linearTableNonSymmetric(String fileName, String xLabel, String yLabel, String valueLabel, Function<Integer, String> x, Function<Integer, String> y, GMatrix m){
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println(xLabel + ";" + yLabel + ";" + valueLabel);
            for (int i = (int)m.getY(); i < m.getMaxY() - 1; i++) {
                for (int j = i + 1; j < m.getMaxX(); j++) {
                    float shade = m.getShadeFunction().apply(i, j);
                    StringBuilder sb = new StringBuilder()
                            .append(x.apply(j))
                            .append(';')
                            .append(y.apply(i))
                            .append(';')
                            .append(shade);
                    ps.println(sb.toString());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
