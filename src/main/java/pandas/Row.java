package pandas;

import java.util.LinkedHashMap;

public class Row extends LinkedHashMap<String, String> {
    public Row(Row m1) {
        super(m1);
    }

    public Row() {
        super();
    }
}
