import download.WebService;
import org.junit.Test;
import pandas.Table;
import parsers.WebServiceDescription;

import java.util.*;

import static pandas.Operations.select;

public class MainTest {
    private static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private final String query = "getArtistInfoByName(Kendrick Lamar, ?artistId, ?beginDate, ?endDate)";
    private final String query2 = "getAlbumsArtistId(?artistId, " +
            "Section.80, ?albumId, ?releaseData, ?country)";

    @Test
    public void main() throws Exception {
        Table t = new Table(query);
        //Arrays.asList(select("artistId", t)).stream().forEach(System.out::println);
        List<Map<String, String>> sigma = select(t, "artistId");
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get("artistId")).forEach(l::add);
        String[] input = l.toArray(new String[0]);
        Table u = new Table(query2, input);
        //join(t, u).stream().forEach(System.out::println);
        //u.getData().stream().forEach(System.out::println);
        Table jointure = new Table(t, u);
        select(jointure, "artistName", "albumTitle").forEach(System.out::println);
        System.out.println(ANSI_GREEN + jointure.getInputKey() + ANSI_RESET);
    }

    @Test
    public void filterOuput() throws Exception {
        Table t = new Table(query);
        List<Map<String, String>> sigma = select(t, "artistId");
        List<String> l = new ArrayList<>();
        sigma.stream().map(tuple -> tuple.get("artistId")).forEach(l::add);
        String[] input = l.toArray(new String[0]);
        Table u = new Table(query2, input);
        System.out.println(ANSI_GREEN + u.getOutput() + ANSI_RESET);
        /*
        u.getData().stream()
                .filter(row -> Operations.where(row, u.getOutput()))
                .forEach(System.out::println);
         */
        u.getData().forEach(System.out::println);

    }

    @Test
    public void split() throws Exception {
        final String sc = ("P(?artistName, ?albumTitle) <- getArtistInfoByName(Snoop Dogg, ?artistId , " +
                "?beginDate, ?endDate)# getAlbumsArtistId(?artistId, " +
                "?albumTitle , ?albumId, ?releaseData, ?country)");
        List<String> query = new LinkedList<String>(Arrays.asList(sc.split("(<-)|#")));

        List<String> atoms = query.subList(1, query.size());

        System.out.println(ANSI_GREEN + atoms.get(0) + ANSI_RESET);

        Table t = new Table(atoms.get(0));
        atoms.remove(atoms.get(0));
        for (String atom : atoms) {

            List<String> decomposition = Arrays.asList(atom.substring(0, atom.length() - 1)
                    .trim().split("[(,]"));
            String webServiceName = decomposition.get(0);
            WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);

            String joinKey = ws.headVariables.get(0).substring(1);

            System.out.println(ANSI_GREEN + joinKey + ANSI_RESET);

            //List<String> joinValues =
            select(t, joinKey).stream()
                    //.map(elt -> elt.get(joinKey))
                    //.collect(Collectors.toList());
                    .forEach(System.out::println);

            //joinValues.;

            //t = new Table(t, new Table(atom, joinValues.toArray(new String[0])));
        }
        System.out.println(query.get(0).substring(0, query.get(0).length() - 1));
        String result = query.get(0).replaceAll("\\s+", "");
        System.out.println(ANSI_GREEN + result + ANSI_RESET);
        List<String> composite = Arrays.asList(result.substring(0, result.length() - 1)
                .trim().split("[(,]"));
        composite.subList(1, composite.size()).stream()
                .map(key -> key.substring(1)).forEach(System.out::println);

    }
}