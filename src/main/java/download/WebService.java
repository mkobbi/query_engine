package download;

import constants.Formating;
import constants.Settings;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class WebService {


    private final String name;

    /**
     * the lists of fragments and variables that form the URL, on the position of variables we have the value null
     **/
    private final List<String> urlFragments;

    /**
     * The head
     **/
    public ArrayList<String> headVariables;
    /**
     * the order of the variables matters!
     * The first variables should be the inputValues variables
     * and their order should be exactly the one required by the construction of the URL
     **/
    public HashMap<String, Integer> headVariableToPosition;

    List<Triple> body;

    /**
     * the body currently not handled!!
     **/
    public WebService(String name, List<String> urlFragments, HashMap<String, String> prefixes, ArrayList<String> headVariables, HashMap<String, Integer> headVariableToPosition, int numberInputs) {
        this.name = name;
        this.urlFragments = urlFragments;

        this.headVariables = headVariables;
        this.headVariableToPosition = headVariableToPosition;

        /*
      The body
     */
    }

    /**
     * constructor for convenience purposes -- if we do not want to read a description from a file
     **/
    public WebService(String name, List<String> params) {
        this.name = name;
        this.urlFragments = params;
    }


    /**
     * @param inputs
     * @return the file where the call result is stored; returns null if some error occurs
     */
    public String getCallResult(String... inputs) {
        String fileWithCallResult = Settings.getDirForCallResults(this.name) + Formating.getFileNameForInputs(inputs);
        //search first the result in the cache
        File f = new File(fileWithCallResult);
        if (f.exists()) return fileWithCallResult;

        //otherwise call the web service
        String URL = getURLForCallWithInputs(inputs);
        return downloadCallResults(URL, fileWithCallResult);

    }

    /**
     * @param fileWithCallResult
     * @return the path of the file where the result is stored
     * @throws Exception
     */
    public String getTransformationResult(String fileWithCallResult) throws Exception {
        Source callResult = new StreamSource(new File(fileWithCallResult));

        Source xsl = new StreamSource(new File(Settings.dirWithDef + this.name + ".xsl"));

        String fileName = fileWithCallResult.substring(fileWithCallResult.lastIndexOf("/") + 1);
        String fileWithTransformationResult = Settings.getDirForTransformationResults(this.name) + fileName;
        System.out.println("File with the transformation result: " + fileWithTransformationResult);
        Result trasformResult = new StreamResult(new File(fileWithTransformationResult));

        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
        transformer.transform(callResult, trasformResult);

        return fileWithTransformationResult;
    }


    /**
     * @param inputs
     * @return the URL of the call for the given inputs
     */
    private String getURLForCallWithInputs(String... inputs) {
        int i = 0;
        StringBuilder call = new StringBuilder();
        for (String p : urlFragments) {
            if (p == null) {
                if (i >= inputs.length) return null; //something wrong; insufficient number of inputValues values
                call.append(Formating.transformStringForURL(inputs[i]));
            } else call.append(p);
        }
        return call.toString();
    }

    /**
     * @param URL
     * @param fileForTheResults
     * @return downloads the file and stores it in the file. If the call result is JSON, then transforms it to XML
     */
    private String downloadCallResults(String URL, String fileForTheResults) {
        String newLine = System.getProperty("line.separator");
        BufferedReader in = null;
        Writer writer = null;
        try {
            URL url = new URL(URL);
            URLConnection conn = url.openConnection();
            // fake a request coming from a browser in order to avoid error 403 (from discogs for instance)
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;" +
                    "     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
            //conn.setRequestProperty("Accept-Charset", "UTF-8");
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // I remove empty lines until the v1 character
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) break;
            }

            // create a string writer if JSON detected 
            boolean isJSONData = false;
            if (line != null) {
                if (line.startsWith("{")) {
                    System.out.println("JSON detected");
                    writer = new StringWriter();
                    isJSONData = true;
                } else {
                    writer = new FileWriter(fileForTheResults);
                }
                writer.write(line + newLine);
            }
            System.out.println(line);


            // write the rest of the inputValues file
            while ((line = in.readLine()) != null) {
                Objects.requireNonNull(writer).write(line + newLine);
                System.out.println(line);
            }
            Objects.requireNonNull(writer).flush();

            // if it's json data do the transformation 
            if (isJSONData) JSONToXML.transformToXML(writer.toString(), fileForTheResults);
        } catch (IOException e) {
            System.out.println("Error in the download " + URL);
            return null;
        } catch (Exception e) {
            System.out.println("Error transformation " + URL);
            return null;
        } finally {
            try {
                if (writer != null) writer.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
                //return null;
            }
        }
        return fileForTheResults;
    }

}
