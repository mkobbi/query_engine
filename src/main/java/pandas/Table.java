package pandas;

import download.WebService;
import jdk.nashorn.internal.objects.annotations.Getter;
import parsers.ParseResultsForWS;
import parsers.WebServiceDescription;

import java.util.*;
import java.util.stream.Collectors;

import static pandas.Operations.join;

public class Table {
    private Queries type;
    private final String inputKey;
    private final String[] inputValues;
    private List<Map<String, String>> data;
    private Map<String, String> output;

    /*    public Table(String query) throws Exception {
        this(query, Objects.requireNonNull(WebServiceDescription.
                loadDescription("mb_" + Arrays.
                        asList(query.substring(0, query.length() - 1).
                                split("[(,]")).get(0))).headVariables);
    }*/

    /**
     * @param query       atom query to load in the table
     * @param inputValues list of input values to be considered for the primary key
     * @throws Exception when the webService can't be correctly loaded
     */
    public Table(String query, String... inputValues) throws Exception {
        this(query, Objects.requireNonNull(WebServiceDescription.
                        loadDescription("mb_" + Arrays.
                                asList(query.substring(0, query.length() - 1).trim().
                                        split("[(,]")).get(0))).headVariables,
                inputValues);
    }

    /**
     * @param t left side of the join operation
     * @param u right side of the join operation
     */
    public Table(Table t, Table u) {
        this.type = Queries.mb_PartialResults;
        this.data = join(t, u);
        Set<String> inputKeySet = new HashSet<>(t.getData().get(0).keySet());
        inputKeySet.retainAll(u.getData().get(0).keySet());
        this.inputKey = inputKeySet.toArray(new String[0])[0];
        this.inputValues = this.data.stream().
                map(entry -> entry.get(this.inputKey)).distinct().toArray(String[]::new);
        this.output = Operations.merge(t.getOutput(), u.getOutput());
    }

    /**
     *
     * @param query atom query to load in the table
     * @param keys
     * @param inputValues list of input values to be considered for the primary key
     * @throws Exception
     */
    public Table(String query, List<String> keys, String... inputValues) throws Exception {

        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1)
                .trim().split("[(,]"));
        String webServiceName = decomposition.get(0);

        if (webServiceName.equals("getAlbumsArtistId")) {
            this.type = Queries.mb_getAlbumsArtistId;
        } else if (webServiceName.equals("getArtistInfoByName")) {
            this.type = Queries.mb_getArtistInfoByName;
        } else if (webServiceName.equals("getSongByAlbumId")) {
            this.type = Queries.mb_getSongByAlbumId;
        }
        //this.inputValues = new ArrayList<>();
        //decomposition.stream().filter(cell -> cell.contains("\"")).forEach(this.inputValues::add);
        this.inputValues = decomposition.get(1).contains("?") ? inputValues : new String[]{decomposition.get(1)};
        this.inputKey = keys.get(0);
        setOutput(query, keys);
        setData(webServiceName, this.getInputValues());
    }

    private void setData(String webServiceName, String... inputs) throws Exception {
        WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
        ArrayList<Map<String, String>> listOfTupleResult = new ArrayList<>();
        for (String input : inputs) {
            String fileWithCallResult = Objects.requireNonNull(ws).getCallResult(input);
            String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);
            for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                LinkedHashMap<String, String> toAdd = new LinkedHashMap<String, String>() {{
                    put(ws.headVariables.get(0).substring(1).trim(),
                            (tuple[0].contains("NODEF") || tuple[0].isEmpty()) ?
                                    input.trim()
                                    : tuple[0].trim()
                    );
                }};
                for (int i = 1; i < ws.headVariables.size(); i++) {
                    toAdd.put(ws.headVariables.get(i).substring(1).trim(), tuple[i].trim());
                }
                listOfTupleResult.add(toAdd);
            }
        }
        this.data = listOfTupleResult.stream()
                .filter(row -> Operations.where(row, output)).collect(Collectors.toList());

    }


    @Override
    public String toString() {
        return "Table{" +
                "type=" + type +
                ", inputValues='" + Arrays.toString(inputValues) + '\'' +
                ", output=" + output +
                '}';
    }

    @Getter
    public Queries getType() {
        return type;
    }

    public String[] getInputValues() {
        return inputValues;
    }

    public Map<String, String> getOutput() {
        return output;
    }

    private void setOutput(String query, List<String> keys) {
        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1).trim().split("[(,]"));
        this.output = new LinkedHashMap<>();
        keys.remove(0);
        for (int i = 0; i < decomposition.size() - 2; i++) {
            String value = decomposition.get(i + 2);
            //this.getOutput().put(keys.get(i), value.contains("?") ? "" : value);
            if (!value.contains("?")) {
                this.output.put(keys.get(i).substring(1), value.trim());
            }
        }
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public String getInputKey() {
        return inputKey;
    }
}
