package pandas;

import download.WebService;
import org.junit.Test;
import parsers.WebServiceDescription;

import java.util.*;

import static pandas.Operations.select;

public class OperationsTest {
    private final String query = "getArtistInfoByName(Snoop Dogg, ?artistId, ?beginDate, ?endDate)";
    private final String query2 = "getAlbumsArtistId(?artistId, " +
            "?albumTitle, ?albumId, ?releaseData, ?country)";

    @Test
    public void join() throws Exception {
        List<String> decomposition = Arrays.asList(query2.substring(0, query2.length() - 1).split("[(,]"));
        String webServiceName = decomposition.get(0);
        Table t = new Table(query);

        WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
        String column = ws.headVariables.get(0).substring(1);
        System.out.println(column);

        List<Map<String, String>> sigma = select(t, column);
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get(column)).forEach(l::add);

        String[] input = l.toArray(new String[0]);
        Table u = new Table(query2, input);

        List<Map<String, String>> l1 = t.getData();
        List<Map<String, String>> l2 = u.getData();

        Set<String> inputKeySet = new HashSet<>(t.getData().get(0).keySet());
        inputKeySet.retainAll(u.getData().get(0).keySet());
        String inputKey = inputKeySet.toArray(new String[0])[0];
        System.out.println(inputKey);
        /*
        ArrayList<Map<String, String>> partialResults = new ArrayList<>();
        l1.stream().flatMap(v1 -> l2.stream()
                //.filter(v2 -> Objects.equals(v1.get(inputKey), v2.get(inputKey)))
                .filter(v2 -> Operations.where(v1, v2))
                .map(v2 -> merge(v1,v2)))
                .forEach(partialResults::add);

        partialResults.forEach(System.out::println);
        */
        List<Map<String, String>> jointure = Operations.join(t, u);
        jointure.stream().forEach(System.out::println);
    }


    @Test
    public void where() throws Exception {
        Map<String, String> m1 = new HashMap<String, String>() {{
            put("artistName", "Elvis Presley");
            put("artistId", "1");
            put("beginDate", "2018-02-06");
            put("endDate", "");
        }};
        Map<String, String> m2 = new HashMap<String, String>() {{
            put("albumTitle", "Ready to Die");
            put("albumId", "1");
            put("releaseData", "");
            put("country", "US");
        }};
        assert (Operations.where(m1, m2));
    }
}