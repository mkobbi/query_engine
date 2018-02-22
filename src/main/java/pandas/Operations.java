package pandas;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Operations {
    public static ArrayList<Row> join(View t, View u) {
        //Mettre le contenu du dernier forEach dans une map
        //Boucler
        List<Row> l1 = t.getData();
        List<Row> l2 = u.getData();
        ArrayList<Row> partialResults = new ArrayList<>();
        l1.stream().flatMap(v1 -> l2.stream().filter(v2 -> where(v1, v2))
                .map(v2 -> merge(v1, v2)))
                .forEach(partialResults::add);
        System.out.println(partialResults.get(0));
        return partialResults;
    }


    public static boolean where(Map<String, String> v1, Map<String, String> v2) {
        Set<String> intersection = new HashSet<>(v1.keySet());
        intersection.retainAll(v2.keySet());
        boolean test = true;
        for (String col : intersection) {
            test = test && v2.get(col).equals(v1.get(col));
        }
        return test;
    }

    public static ArrayList<Row> select(View from, String... columns) {
        return from.getData().stream().map(tuple -> Arrays.stream(columns)
                .filter(tuple::containsKey)
                .collect(Collectors.toMap(Function.identity(), tuple::get, (w, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", w));
                }, Row::new)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static Row merge(Row m1, Row m2) {
        Row mx = new Row(m1);
        mx.putAll(m2);
        return mx;
    }
}
