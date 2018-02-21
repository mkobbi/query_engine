package constants;

public class Settings {

    private static final String rootProject = "evaluation/";
    public static final String dirWithDef = rootProject + "ws-definitions/";


    public static String getDirForCallResults(String ws) {
        return rootProject + ws + "/call_results/";
    }

    public static String getDirForTransformationResults(String ws) {
        return rootProject + ws + "/transf_results/";
    }


}
