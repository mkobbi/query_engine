package pandas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import static pandas.Operations.select;

public class Column extends ArrayList<String> {
    private final String header;
    private static Integer nth = 0;

    public Column(String header) {
        super();
        this.header = header;
    }

    public Column() {
        super();
        this.header = "col" + nth.toString();
        nth++;
    }

    public Column(View t, String joinKey) {
        this(joinKey);
        this.clear();
        this.addAll(select(t, joinKey).stream().map(elt -> elt.get(joinKey)).collect(Collectors.toList()));
    }

    //A deplacer vers table
    public Column toColumn(View t, String joinKey) {
        this.clear();
        this.addAll(select(t, joinKey).stream().map(elt -> elt.get(joinKey)).collect(Collectors.toList()));
        return null;
    }

    public static String[] columnValues(View t, String joinKey) {
        Column c = new Column(t, joinKey);
        return new HashSet<>(c).toArray(new String[0]);
    }
}
