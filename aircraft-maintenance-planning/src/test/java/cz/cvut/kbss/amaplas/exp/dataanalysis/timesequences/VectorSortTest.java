package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

//import org.eclipse.rdf4j.query.algebra.In;
//import org.eclipse.rdf4j.query.algebra.evaluation.function.numeric.Rand;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

//public class VectorSortTest extends TestCase {
@Ignore
public class VectorSortTest{
//    @Test
//    public void testSortWithDimMatchDistance(){
//        double[][] sorted = new double[3][];
//        sorted[0] = new double[]{0.1,0,0};
//        sorted[1] = new double[]{0.1,0.1,0};
//        sorted[2] = new double[]{0.1,0.1,0.2};
//        double[][] toSort = Arrays.copyOf(sorted, sorted.length);
//        toSort[1] = sorted[2];
//        toSort[2] = sorted[1];
//        double[][] beforeSort = Arrays.copyOf(toSort, toSort.length);
//        VectorSort.sort(toSort, (r1, r2) -> VectorSort.dimMatchDistance(r1,r2,10, 10));
//
//        assertFalse(Arrays.equals(beforeSort, toSort));
//        assertTrue(Arrays.equals(sorted, toSort));
//    }

    @Ignore
    @Test
    public void testBruteSortWithDimMatchDistance(){

        double[][] sorted = new double[1<<10][10];
        for(int i = 0; i < sorted.length; i ++){
            for(int j = 0; j < sorted[0].length; j++){
                int b = 1 << j;
                sorted[i][j] = (i & b) > 0 ? 1 : 0;
            }
        }

        // randomize order
        double[][] toSort = Arrays.copyOf(sorted, sorted.length);
        Random r = new Random(0);
        Collections.shuffle(Arrays.asList(toSort), r);
        VectorSort.sort(toSort, (r1, r2) -> VectorSort.dimMatchDistance(r1,r2,10, 10));
        assertTrue(true);
    }

    @Ignore
    @Test
    public void testBruteSortWithDimMatchDistance2(){
        double[][] sorted = new double[1<<10][10];
        Random r = new Random(0);
        for(int i = 0; i < sorted.length; i ++){
            for(int j = 0; j < sorted[0].length; j++){
                sorted[i][j] = r.nextBoolean() ? r.nextDouble() : 0;
            }
        }

        // randomize order
        double[][] toSort = Arrays.copyOf(sorted, sorted.length);
        r = new Random(0);
        Collections.shuffle(Arrays.asList(toSort), r);
        VectorSort.sort(toSort, (r1, r2) -> VectorSort.dimMatchDistance(r1,r2,10, 10));
        assertTrue(true);
    }

    @Test
    public void bitsetCompTest_FindSeedsThatShuffleListToThrowExceptionWhenSorted() {
        Random r = new Random(0);
        List<Integer> allAsInt = IntStream.range(0, 32).map(i -> i | 8).mapToObj(i -> i).collect(Collectors.toList());
        List<BitSet> allBS = allAsInt.stream().sorted().map(VectorSortTest::toBitSet).collect(Collectors.toList());
        for(int i = 0; i < 100000; i ++){
            List<BitSet> bsl = new ArrayList<>(allBS);
            long seed = r.nextLong();
            Collections.shuffle(bsl, new Random(seed));
            boolean throwsException = sortThrowsException(bsl);
            if(throwsException){
                System.out.println(i);
                System.out.println(seed);
            }
        }
    }

    @Test
    public void bitsetCompTest_CheckSeedShufflesListToThrowExceptionWhenSorted() {
        Random r = new Random(6427218635535763657L);
        List<Integer> allAsInt = IntStream.range(0, 32).map(i -> i | 32).mapToObj(i -> i).collect(Collectors.toList());
        List<BitSet> allBS = allAsInt.stream().sorted().map(VectorSortTest::toBitSet).collect(Collectors.toList());
        Collections.shuffle(allBS, r);
        List<Integer> shuffledInts = new ArrayList<>();
        for(BitSet bs : allBS){
            shuffledInts.add((int)toLong(bs));
        }
        shuffledInts.forEach(i -> System.out.println(i));
        System.out.println(sortThrowsException(allBS));


    }

    @Test
    public void bitsetCompTest(){

        Random r = new Random(6427218635535763657L);
        List<Integer> allAsInt = IntStream.range(0, 32).map(i -> i | 32).mapToObj(i -> i).collect(Collectors.toList());
        List<BitSet> allBS = allAsInt.stream().sorted().map(VectorSortTest::toBitSet).collect(Collectors.toList());
        Set<Integer> unnecessary = new HashSet<>();
        Collections.shuffle(allBS, r);


//        allBS.sort((p1, p2) -> VectorSort.bitsetComp(p1, p2, 2, 10));

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        BitSet mask = new BitSet();
        BitSet maxMask = new BitSet();
//        int lastMax = 0;

//        Set<Integer> set = new HashSet<>();

        while(!stack.empty()){
            int i = stack.peek();
            mask.set(i);
            // create a selection of the bitset list based on the mask
            List<BitSet> bsl = new ArrayList<>();
            for(int j = 0; j < allBS.size(); j++){
                if(!mask.get(j))
                    bsl.add(allBS.get(j));
            }
            // check if when sorted there is an exception
            boolean throwsException = sortThrowsException(bsl);
            boolean lastIndex = i + 1 >= allBS.size();

            // store biggest mask
            if(throwsException) {
                if (maxMask.cardinality() < mask.cardinality()) {
                    maxMask = (BitSet) mask.clone();
                    System.out.println(mask.cardinality());
                    System.out.println(mask);
                }
            }

            // stack manipulation
            if(lastIndex) {// backtrack
                stack.pop(); // pop i
                if(stack.empty())
                    continue;
                // progress the element before i
                int j = stack.pop(); // pop the element before i
                stack.push(j + 1); // push next j
                // clear i and j from the mask
                mask.clear(i);
                mask.clear(j);
            }else {
                stack.push(i + 1);
//                if(throwsException) {// push a new stack element
//                    stack.push(i + 1);
//                }else {// increment the current stack element
//                    stack.pop(); // pop the i value
//                    stack.push(i + 1);
//                    mask.clear(i); // clear the index from the mask
//                }
            }

        }
        System.out.println(maxMask);

//        while(true) {
//
//            int max = maxLBWithException(bsl);
//            if(max == 0)
//                break;
//            mask.set(lastMax, max);
//            lastMax = max;
//
//        }





//        List<List<Integer>> badPairs = new ArrayList<>();

//        for(int i = 0; i<)

//        for(int i = 0; i < allBS.size() - 1; i ++){
//            for(int j = i + 1; j < allBS.size(); j ++) {
//                List<BitSet> bitSets = Stream.of(j,i).map(allBS::get)
//                        .collect(Collectors.toList());
////                Collections.shuffle(bitSets, r);
//                try {
//                    bitSets.sort((p1, p2) -> VectorSort.bitsetComp(p1, p2, 2, 10));
//                }catch (IllegalArgumentException e){
//                    System.out.println(e.getMessage());
//                    System.out.println(String.format("%d, %d", i, j));
//                    System.out.println(String.format("%d, %d", allAsInt.get(i), allAsInt.get(j)));
//                    System.out.println(String.format("%s, %s", allBS.get(i), allBS.get(j)));
//
//                }
//            }
//        }

    }

    protected boolean sortThrowsException(List<BitSet> bs){
        try {
            bs.sort((p1, p2) -> VectorSort.bitsetComp(p1, p2, 2, 10));
        }catch (IllegalArgumentException e){
            return true;
        }
        return false;
    }

    protected int maxLBWithException(List<BitSet> bs){
        List<BitSet> bsl = new ArrayList<>(bs);

        for(int i = 0; i < bsl.size(); i ++){
            try {
                bsl = bs.subList(i, bs.size());
                bsl.sort((p1, p2) -> VectorSort.bitsetComp(p1, p2, 2, 10));
            }catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
                System.out.println(i);
                System.out.println(bsl.get(i));
                System.out.println(toLong(bsl.get(i)));
                return i;
            }
        }
        return 0;
    }

    public static long toLong(BitSet bs){
        return bs.toLongArray()[0];
    }

    public static BitSet toBitSet(int i){
        return BitSet.valueOf(new long[]{i});
    }
}