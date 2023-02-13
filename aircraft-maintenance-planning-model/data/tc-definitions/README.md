# Processing task definitions
Describes the processing of task definitions. In summary process is composed of these steps:

1. preprocess the input Excel files into suitable CSV files  
2. convert the CSV files into RDF using tc-definitions.sms.ttl pipeline  

## Preprocessing
**Input** 
* <code>MPD</code> and <code>ACCESS</code> sheets from file MP_A32F_General_UPRAVENO.xlsx (A32F)
* <code>MPD</code> and <code>OPEN-CLOSE</code> sheets from file MP_B737_GENERAL_UPRAVENO.xlsx (B737)

In these input sheets each row is a task step and to which task it belongs.   

**Preprocessing**
1. Align columns, there are several columns which do not have the same names across the input workbooks and sheets
For example:
    * in <code>MPD</code> sheet in file A32F had a column "MPD" while other sheets had column "MPD NUMBER" 
    * in <code>MPD</code> sheet in file B737 had a column "E**L**. power" while MPD in A32F had column "E**l**. power"
2. Normalization of data - some cells in the tables contain empty strings or strings only with spaces. Delete such
   values.
3. Add a _step_ column that explicitly specifies the order of the task step in the overal task. This task is done for 
all sheets, but the <code>MPD</code> in file A32F needed some additional work.  
    * for sheet <code>MPD</code> in file A32F, **1.** Create a new column "code" and set the value to "TASKNUMBER" if 
   its value is not blank, otherwise, set it to "MPD NUMBER". Use formula <code>=IF(ISBLANK(P2),IF(ISBLANK(Q2),"",Q2),P2)</code> 
   to set the values of the new column "code"". In the formulas, P is "TASKNUMBER", Q is "MPD NUMBER" and G is new column 
   "code". **2.** Create a new column "ignore" and set some arbitrary value (e.g. "ignore") if new column "code" is empty.
   You can use a formula such as <code>=IF(ISBLANK(G2),"ignore", "")</code>.  **3.** order the sheet according columns 
   "code".
    * after sheet <code>MPD</code>  in file A32F is processed as described above, continue with all the sheets. For each 
   sheet, add a new column "step" and use the formula <code>=IF(G2<>G1,1,B1+1)</code> to generate the step order, where
   "G" is new column "code" and B is new column "step".
4. Export sheets into CSV files


## Conversion into RDF
**Input**
- CSV files created by the **Preprocessing** step


**Conversion**
The conversion into RDF is done with the tc-definitions.sms.ttl pipeline which can be executed properly deployed in an 
SPipes instance.   