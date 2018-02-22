import download.WebService;
import org.junit.Test;
import pandas.Column;
import pandas.Row;
import pandas.View;
import parsers.WebServiceDescription;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static pandas.Operations.select;

public class MainTest {
    private static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\033[34m";
    private final String query = "getArtistInfoByName(Snoop Dogg, ?artistId, ?beginDate, ?endDate)";
    private final String query2 = "getAlbumsArtistId(?artistId, " +
            "Doggystyle, ?albumId, ?releaseData, ?country)";

    @Test
    public void main() throws Exception {
        View t = new View(query);
        //Arrays.asList(select("artistId", t)).stream().forEach(System.out::println);
        List<Row> sigma = select(t, "artistId");
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get("artistId")).forEach(l::add);
        String[] input = l.toArray(new String[0]);
        View u = new View(query2, input);
        //join(t, u).stream().forEach(System.out::println);
        //u.getData().stream().forEach(System.out::println);
        View jointure = new View(t, u);
        select(t, "artistName", "albumTitle").forEach(System.out::println);
        System.out.println(ANSI_GREEN + jointure.getInputKey() + ANSI_RESET);
    }

    @Test
    public void filterOuput() throws Exception {
        View t = new View(query);
        List<Row> sigma = select(t, "artistId");
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get("artistId")).forEach(l::add);
        String[] input = l.toArray(new String[0]);
        View u = new View(query2, input);
        System.out.println(ANSI_GREEN + u.getOutput() + ANSI_RESET);
        /*
        u.getData().stream()
                .filter(row -> Operations.where(row, u.getOutput()))
                .forEach(System.out::println);
         */
        u.forEach(System.out::println);

    }

    @Test
    public void split() throws Exception {
        final String input = "P(?artistName, ?albumTitle) <- getArtistInfoByName(Kendrick Lamar, ?artistId , ?beginDate, ?endDate)# getAlbumsArtistId(?artistId, Section.80 , ?albumId, ?releaseData, ?country)#getSongByAlbumId(?albumId,?artistName, ?songTitle)";
        List<String> query = new LinkedList<>(Arrays.asList(input
                .substring(0, input.length() - 1)
                .trim().split("(<-)|#")));

        List<String> atoms = query.subList(1, query.size());

        View t = new View(atoms.get(0));
        atoms.remove(atoms.get(0));
        for (String atom : atoms) {

            List<String> decomposition = Arrays.asList(atom.substring(0, atom.length() - 1)
                    .trim().split("[(,]"));
            String webServiceName = decomposition.get(0);

            WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
            String joinKey = Objects.requireNonNull(ws).headVariables.get(0).substring(1);

            System.out.println(ANSI_GREEN + joinKey + ANSI_RESET);
            //List<String> joinValues =
            select(t, joinKey).stream()
                    //.map(elt -> elt.get(joinKey))
                    //.collect(Collectors.toList());
                    .forEach(System.out::println);

            String[] joinValues =
                    //Set<String> joinValues =
                    //        select(t, joinKey).stream().map(elt -> elt.get(joinKey))
                    //                .toArray(String[]::new);
                    //.collect(Collectors.toSet()).toArray(new String[0]);
                    Column.columnValues(t, joinKey);

            System.out.println("Join Values");
            Arrays.asList(joinValues).forEach(System.out::println);
            //System.out.println(joinValues);

            View u = new View(atom, joinValues);
            System.out.print(ANSI_BLUE);
            u.stream()
                    //.map(elt -> elt.get(joinKey))
                    //.collect(Collectors.toList());
                    .forEach(System.out::println);
            System.out.print(ANSI_RESET);
            t = new View(t, u);
            select(t, joinKey).stream()
                    //.map(elt -> elt.get(joinKey))
                    //.collect(Collectors.toList());
                    .forEach(System.out::println);
        }
        System.out.println(query.get(0).substring(0, query.get(0).length() - 1));
        String result = query.get(0).replaceAll("\\s+", "");
        System.out.println(ANSI_GREEN + result + ANSI_RESET);
        List<String> composite = Arrays.asList(result.substring(0, result.length() - 1)
                .trim().split("[(,]"));
        composite.subList(1, composite.size()).stream()
                .map(key -> key.substring(1)).forEach(System.out::println);

    }

    @Test
    public void toCSV() throws Exception {
        View t = new View(query);
        final List<Row> list = t;
        List<String> headers = list.stream()
                .flatMap(map -> map.keySet().stream()).distinct().collect(Collectors.toList());
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(headers.get(i));
            sb.append(i == headers.size() - 1 ? "\n" : ",");
        }
        for (Map<String, String> map : list) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(map.get(headers.get(i)));
                sb.append(i == headers.size() - 1 ? "\n" : ",");
            }
        }
        System.out.println(sb.toString());
    }

    @Test
    public void timestamp() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        System.out.println(sdf.format(new Timestamp(System.currentTimeMillis())));
    }
}

