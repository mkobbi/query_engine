package pandas;

import download.WebService;
import org.junit.Test;
import parsers.ParseResultsForWS;
import parsers.WebServiceDescription;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.fail;
import static pandas.Operations.select;

public class ViewTest {
    //private final String query = "getAlbumsArtistId(381086ea-f511-4aba-bdf9-71c753dc5077, " +
    //private final String query = "getAlbumsArtistId(?artistId, " +
    //        "?albumTitle, ?albumId, ?releaseData, ?country)";

    private final String query = "getArtistInfoByName(Snoop Dogg, ?artistId, ?beginDate, ?endDate)";
    private final String query2 = "getAlbumsArtistId(?artistId, " +
            "?albumTitle, ?albumId, ?releaseData, ?country)";
    //private final String query = "getArtistInfoByName(Elvis Presley, ?artistId, ?beginDate, ?endDate)";
    private final WebService ws = WebServiceDescription.loadDescription("mb_getAlbumsArtistId");
    private final ArrayList<String> keys = Objects.requireNonNull(ws).headVariables;

    public ViewTest() throws IOException {
    }

    @org.junit.Test
    public void toStringTest() throws Exception {
        View view = new View(query, keys);
        System.out.println(view);

    }

    @org.junit.Test
    public void getType() {
        fail("Not implemented yet.");

    }

    @org.junit.Test
    public void getInput() throws Exception {
        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1).split("[(,]"));
        String input = decomposition.get(1);
        View t = new View(query);
        System.out.println(input);
    }

    @Test
    public void getOutput() throws Exception {
        String query = "getArtistInfoByName(Elvis Presley, ?artistId, ?beginDate, ?endDate)";
        WebService ws = WebServiceDescription.loadDescription("mb_getArtistInfoByName");
        ArrayList<String> keys = ws.headVariables;
        //System.out.println(keys);
        View view = new View(query, keys);
        //System.out.println(view.getOutput().keySet());
        //System.out.println(new HashSet<>(keys).remove("?artistName"));
        assert (view.getOutput().keySet().equals(new HashSet<>(keys)));
    }

    @Test
    public void setOutput() {
        fail("Not implemented yet.");
    }

    @Test
    public void setData() throws Exception {
        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1).split("[(,]"));
        String webServiceName = decomposition.get(0);
        WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
        ArrayList<LinkedHashMap<String, String>> listOfTupleResult = new ArrayList<>();
        String[] inputs = new String[]{"eeb1195b-f213-4ce1-b28c-8565211f8e43",
                "381086ea-f511-4aba-bdf9-71c753dc5077"};
        for (String input : inputs) {
            String fileWithCallResult = ws.getCallResult(input);
            String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);
            for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                LinkedHashMap<String, String> toAdd = new LinkedHashMap<String, String>() {{
                    put(ws.headVariables.get(0).substring(1), tuple[0].contains("NODEF") ? input : tuple[0]);
                }};
                for (int i = 1; i < ws.headVariables.size(); i++) {
                    toAdd.put(ws.headVariables.get(i).substring(1), tuple[i]);
                }
                listOfTupleResult.add(toAdd);
            }
        }
        listOfTupleResult.stream().forEach(System.out::println);
    }

    @Test
    public void setDataWithPreviousResults() throws Exception {
        List<String> decomposition = Arrays.asList(query2.substring(0, query2.length() - 1).split("[(,]"));
        String webServiceName = decomposition.get(0);
        View t = new View(query);

        WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
        String column = ws.headVariables.get(0).substring(1);
        System.out.println(column);

        List<Row> sigma = select(t, column);
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get(column)).forEach(l::add);

        String[] input = l.toArray(new String[0]);
        View u = new View(query2, input);
        u.forEach(System.out::println);
    }

    @Test
    public void columnUniverse() throws Exception {
        View t = new View(query);
        List<Row> data = t;
        //data.stream().forEach(System.out::println);
        data.stream().map(tuple -> tuple.get("artistId")).forEach(System.out::println);
    }

    @Test
    public void getData() throws Exception {
        View view = new View(query);
        view.stream().forEach(System.out::println);
    }

    @Test
    public void joinConstructor() throws Exception {
        List<String> decomposition = Arrays.asList(query2.substring(0, query2.length() - 1).split("[(,]"));
        String webServiceName = decomposition.get(0);
        View t = new View(query);

        WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
        String column = ws.headVariables.get(0).substring(1);
        System.out.println(column);

        List<Row> sigma = select(t, column);
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get(column)).forEach(l::add);

        String[] input = l.toArray(new String[0]);
        View u = new View(query2, input);

        /*Queries type = Queries.mb_PartialResults;
        List<Map<String, String>> data = join(t, u);
        data.forEach(System.out::println);
        Set<String> inputKeySet = new HashSet<>(t.getData().get(0).keySet());
        inputKeySet.retainAll(u.getData().get(0).keySet());
        String inputKey = inputKeySet.toArray(new String[0])[0];
        String[] inputValues = data.stream().
                map(entry -> entry.get(inputKey)).collect(Collectors.toList()).toArray(new String[0]);
        Map<String, String> output = data.get(0).entrySet().stream().
                filter(map -> !map.getKey().equals(inputKey))
                .collect(Collectors.toMap(p -> p.getKey(), p -> ""));
        System.out.println("Type: " + type + "\nInput Key: " + inputKey +
                "\nOutput: " + output);
        */
        View jointure = new View(t, u);
        jointure.forEach(System.out::println);
        System.out.println(jointure.getType());
        System.out.println(jointure.getOutput());
        Arrays.asList(jointure.getInputValues()).forEach(System.out::println);
    }

}