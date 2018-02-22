package pandas;

import download.WebService;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.xml.sax.SAXException;
import parsers.ParseResultsForWS;
import parsers.WebServiceDescription;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;

import static pandas.Operations.join;

public class View extends ArrayList<Row> {
    private Queries type;
    private final String inputKey;
    private final String[] inputValues;
    private Row output;

    /*    public View(String query) throws Exception {
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
    public View(String query, String... inputValues) throws Exception {
        this(query, Objects.requireNonNull(WebServiceDescription.
                        loadDescription("mb_" + Arrays.
                                asList(query.substring(0, query.length() - 1).trim().
                                        split("[(,]")).get(0))).headVariables,
                inputValues);
    }

    public View(String[] headers) {
        this(new ArrayList<Row>() {{
            add(new Row() {{
                for (String arg : headers) {
                    put(arg, "");
                }
            }});
        }});

    }

    View(ArrayList<Row> data) {
        super(data);
        this.type = Queries.mb_PartialResults;
        this.inputKey = data.get(0).keySet().toArray(new String[0])[0];
        this.inputValues = this.stream().
                map(entry -> entry.get(this.inputKey)).distinct().toArray(String[]::new);
        this.output = new Row();
    }

    /**
     * @param t left side of the join operation
     * @param u right side of the join operation
     */
    public View(View t, View u) {
        super(join(t, u));
        this.type = Queries.mb_PartialResults;
        Set<String> inputKeySet = new HashSet<>(t.get(0).keySet());
        inputKeySet.retainAll(u.get(0).keySet());
        this.inputKey = inputKeySet.toArray(new String[0])[0];
        this.inputValues = this.stream().
                map(entry -> entry.get(this.inputKey)).distinct().toArray(String[]::new);
        this.output = Operations.merge(t.getOutput(), u.getOutput());
    }

    /**
     *
     * @param query atom query to load in the table
     * @param headers list of column headers
     * @param inputValues list of input values to be considered for the primary key. It should not be empty
     * @throws Exception whenever there is an issue with the web service
     */
    View(String query, List<String> headers, String... inputValues) {

        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1)
                .trim().split("[(,]"));
        String webServiceName = decomposition.get(0);

        if ("getAlbumsArtistId".equals(webServiceName)) {
            this.type = Queries.mb_getAlbumsArtistId;

        } else if ("getArtistInfoByName".equals(webServiceName)) {
            this.type = Queries.mb_getArtistInfoByName;

        } else if ("getSongByAlbumId".equals(webServiceName)) {
            this.type = Queries.mb_getSongByAlbumId;

        }
        //this.inputValues = new ArrayList<>();
        //decomposition.stream().filter(cell -> cell.contains("\"")).forEach(this.inputValues::add);
        this.inputValues = decomposition.get(1).contains("?") ? inputValues : new String[]{decomposition.get(1)};
        this.inputKey = headers.get(0);
        setOutput(query, headers);
        setData(webServiceName, this.getInputValues());
    }

    private void setData(String webServiceName, String... inputs) {
        int count = 0;
        final int maxTries = 3;
        while (true) {
            try {
                WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
                List<Row> listOfTupleResult = new ArrayList<>();
                for (String input : inputs) {
                    String fileWithCallResult = Objects.requireNonNull(ws).getCallResult(input);
                    String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);
                    for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                        Row toAdd = new Row() {{
                            put(ws.headVariables.get(0).substring(1).trim(),
                                    (tuple[0].contains("NODEF") || tuple[0].isEmpty()) ?
                                            input.trim()
                                            : tuple[0].trim()
                            );
                        }};
                        for (int i = 1; i < ws.headVariables.size(); i++) {
                            toAdd.put(ws.headVariables.get(i).substring(1).trim(), (tuple[i] != null) ? tuple[i].trim() : "");
                        }
                        listOfTupleResult.add(toAdd);
                    }
                }
                ArrayList<Row> list = new ArrayList<>();
                for (Row row : listOfTupleResult) {
                    if (Operations.where(row, output)) {
                        list.add(row);
                    }
                }
                this.clear();
                this.addAll(list);
                break;
            } catch (NullPointerException e) {
                System.out.println("Retrying download");
                if (++count == maxTries) throw e;
            } catch (IOException | TransformerException | SAXException | XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String toString() {
        return "View{" +
                "type=" + type +
                ", inputValues='" + Arrays.toString(inputValues) + '\'' +
                ", output=" + output.toString() +
                '}';
    }

    @Getter
    public Queries getType() {
        return type;
    }

    public String[] getInputValues() {
        return inputValues;
    }

    public Row getOutput() {
        return output;
    }

    private void setOutput(String query, List<String> keys) {
        List<String> decomposition = Arrays.asList(query.substring(0, query.length() - 1).trim().split("[(,]"));
        this.output = new Row();
        keys.remove(0);
        for (int i = 0; i < decomposition.size() - 2; i++) {
            String value = decomposition.get(i + 2);
            //this.getOutput().put(keys.get(i), value.contains("?") ? "" : value);
            if (!value.contains("?")) {
                this.output.put(keys.get(i).substring(1), value.trim());
            }
        }
    }

    /*public List<Row> getData() {
        return data;
    }*/

    public String getInputKey() {
        return inputKey;
    }
}
