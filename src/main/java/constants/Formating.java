package constants;

public class Formating {


    public static String transformStringForURL(String input) {
        return input.trim().replaceAll("\\s+", "+");
    }

    public static String getFileNameForInputs(String... inputs) {
        StringBuilder buff = new StringBuilder();
        boolean first = true;
        for (String input : inputs) {
            if (!first) buff.append("_");
            else first = false;
            buff.append(transformStringForURL(input));
        }
        buff.append(".xml");
        return buff.toString();
    }

}
