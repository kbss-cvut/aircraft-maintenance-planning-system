package cz.cvut.kbss.amaplas.exp.dataanalysis.io;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AircraftType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.validate.CheckImportedData;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVDataReader {

    protected String[] EMPTY = new String[]{};

    protected CSVParser parser = new CSVParserBuilder()
            .withSeparator(',')
            .build();

    public List<Result> readData(String excelFiles){
        List<Result> ret = new ArrayList<>();

        try(CSVReader r = new CSVReaderBuilder(new FileReader(new File(excelFiles)))
                .withCSVParser(parser)
                .withSkipLines(1)
                .build()
        ){
            Iterator<String[]> records = r.iterator();
            while(records.hasNext()){
                String[] rec = records.next();
                if(isDataRow(rec)){
                    Result res = convert(rec);
                    if(isValidRecord(res)) {
                        ret.add(res);
                    }
                }
            }
//            Result.normalizeTaskTypeLabels(ret);
            Result.normalizeTaskTypes(ret);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    protected String[] parse(String line){
        try {
            return parser.parseLine(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EMPTY;
    }

    protected boolean isDataRow(String[] r){
        String id = r != null && r.length > 0 ? r[0] : null;
        try{
            if(id != null)
                Long.parseLong(id);
        }catch (NumberFormatException ex){
            return false;
        }
        return true;
    }

    protected Result convert(String[] r){

        TaskType taskType = new TaskType(
                r[11], // WO/TC - the code of the task
                r[14], // description
                r[10], // Type - task category
                r[16]  // aircraft model
        );

        Result res = new Result();
        res.id = r[0];
        res.wp = r[15];
        res.acmodel = taskType.getAcmodel();
        res.acType = AircraftType.getTypeLabelForModel(res.acmodel);
        res.scope = r[8];// + "_group";
        res.shiftGroup = r[9];
        res.taskType = taskType;
        res.start = CheckImportedData.date(r[3], r[4]) ;
        res.end = CheckImportedData.date(r[5], r[6]) ;
        return res;
    }

    protected boolean isValidRecord(Result r){
        return r.wp != null && r.acmodel != null && r.start != null && r.scope != null &&
                r.taskType != null &&
                r.taskType.getCode() != null &&
                !r.taskType.getCode().isEmpty();
    }

    public static void main(String[] args) {
        String dataDir = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\input\\data_2020-02\\";
        CSVDataReader reader = new CSVDataReader();
        List<Result> r = reader.readData(dataDir + "2019_1_2.csv");
//        List<Result> r = reader.readData(dataDir + "2017_1_2.csv");
        System.out.println(r.size());
    }
}
