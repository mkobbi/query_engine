import download.WebService;
import pandas.Table;
import parsers.WebServiceDescription;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static pandas.Operations.select;


public class Main {


    public static final void main(String[] argv) throws Exception {

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        List<String> query = new LinkedList<String>(Arrays.asList(input
                .substring(0, input.length() - 1)
                .trim().split("(<-)|#")));

        List<String> atoms = query.subList(1, query.size());

        Table t = new Table(atoms.get(0));
        atoms.remove(atoms.get(0));
        for (String atom : atoms) {

            List<String> decomposition = Arrays.asList(atom.substring(0, atom.length() - 1)
                    .trim().split("[(,]"));
            String webServiceName = decomposition.get(0);

            WebService ws = WebServiceDescription.loadDescription("mb_" + webServiceName);
            String joinKey = ws.headVariables.get(0).substring(1);

            String[] joinValues =
                    select(t, joinKey).stream().map(elt -> elt.get(joinKey))
                            .collect(Collectors.toList()).toArray(new String[0]);

            t = new Table(t, new Table(atom, joinValues));
        }
        String result = query.get(0).replaceAll("\\s+", "");
        List<String> composite = Arrays.asList(result.substring(0, result.length() - 1)
                .trim().split("[(,]"));
        String[] args = composite.subList(1, composite.size()).stream()
                .map(key -> key.substring(1)).collect(Collectors.toList())
                .toArray(new String[0]);

        select(t, args).forEach(System.out::println);

    }

}
