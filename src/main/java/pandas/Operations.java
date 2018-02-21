package pandas;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Operations {
    public static List<Map<String, String>> join(Table t, Table u) {
        //Mettre le contenu du dernier forEach dans une map
        //Boucler
        List<Map<String, String>> l1 = t.getData();
        List<Map<String, String>> l2 = u.getData();
        ArrayList<Map<String, String>> partialResults = new ArrayList<>();
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

    public static List<Map<String, String>> select(Table from, String... columns) {
        return from.getData().stream().map(tuple -> Arrays.stream(columns)
                .filter(tuple::containsKey)
                .collect(Collectors.toMap(Function.identity(), tuple::get)))
                .collect(Collectors.toList());
    }

    static Map<String, String> merge(Map<String, String> m1, Map<String, String> m2) {
        LinkedHashMap<String, String> mx = new LinkedHashMap<>(m1);
        mx.putAll(m2);
        return mx;
    }
}
