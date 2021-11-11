package cz.cvut.kbss.amaplas.exp.dataanalysis.io;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.validate.CheckImportedData;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ExcelDataReader {

        protected List<Result> readData(String excelFiles){
            List<Result> ret = Collections.emptyList();
            try(FileInputStream file = new FileInputStream(new File(excelFiles))) {
                Workbook wb = new XSSFWorkbook(file);
                System.out.println("Excel workbook read in memory.");
                Sheet s = wb.getSheetAt(0);
                Iterator<Row> iter = s.rowIterator();
                ret = new ArrayList<>(s.getLastRowNum());
                while(iter.hasNext()){
                    Row r = iter.next();
                    if(isDataRow(r)){
                        Result result = convert(r);
                        ret.add(result);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        protected boolean isDataRow(Row row){
            return row.getCell(0).getCellType() == CellType.NUMERIC;
//        String id = getNumericCellValue();
//        try{
//            Long.parseLong(id);
//        }catch (NumberFormatException ex){
//            return false;
//        }
//        return true;
        }


        protected Result convert(Row row){
            Function<Integer,String> r = val(row);
            Result res = new Result();
            res.wp = r.apply(15);
            res.scope = r.apply(8);
            res.taskType = new TaskType(r.apply(11));
            res.start = CheckImportedData.date(r.apply(3), r.apply(4)) ;
            res.end = CheckImportedData.date(r.apply(5), r.apply(6)) ;
            return res;
        }

        public Function<Integer, String> val(Row row){
            return i -> row.getCell(i).getStringCellValue();
        }

//        public String strval(Row row, int index){
//            return row.getCell(index).getStringCellValue();
//        }

//    public static void main(String[] args) {
//        String dataDir = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\input\\data_2020-02\\";
//        ExcelDataReader reader = new ExcelDataReader();
//        List<Result> r = reader.readData(dataDir + "2017_1_2.xlsx");
//        System.out.println(r.size());
//    }
}
