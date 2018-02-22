import download.WebService;
import pandas.Column;
import pandas.View;
import parsers.WebServiceDescription;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static pandas.Operations.select;

/**
 * Main class of the project
 */
public class Main {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private static void toCSV(View data) throws IOException {
        String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        FileWriter fw = new FileWriter("query-" + timestamp + ".csv", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        List<String> headers = data.stream()
                .flatMap(map -> map.keySet().stream()).distinct().collect(Collectors.toList());
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(headers.get(i));
            sb.append(i == headers.size() - 1 ? "\n" : ";");
        }
        for (Map<String, String> map : data) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(map.get(headers.get(i)));
                sb.append(i == headers.size() - 1 ? "\n" : ";");
            }
        }
        out.println(sb.toString());
        out.close();
        bw.close();
        fw.close();
    }

    public static void main(String[] argv) {

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        List<String> query = new LinkedList<>(Arrays.asList(input
                .substring(0, input.length() - 1)
                .trim().split("(<-)|#")));

        String result = query.get(0).replaceAll("\\s+", "");

        List<String> composite = Arrays.asList(result.substring(0, result.length() - 1)
                .trim().split("[(,]"));
        String[] args = composite.subList(1, composite.size()).stream()
                .map(key -> key.substring(1)).toArray(String[]::new);

        List<String> atoms = query.subList(1, query.size());

        View t = null;
        try {
            t = new View(atoms.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        atoms.remove(atoms.get(0));
        for (String atom : atoms) {

            List<String> decomposition = Arrays.asList(atom.substring(0, atom.length() - 1)
                    .trim().split("[(,]"));
            String webServiceName = decomposition.get(0);

            WebService ws = null;
            try {
                ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String joinKey = Objects.requireNonNull(ws).headVariables.get(0).substring(1);

            String[] joinValues = Column.columnValues(t, joinKey);

            try {
                t = new View(t, new View(atom, joinValues));
            } catch (IndexOutOfBoundsException e) {
                t = new View(args);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //select(t, args).forEach(System.out::println);
        try {
            toCSV(select(Objects.requireNonNull(t), args));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
