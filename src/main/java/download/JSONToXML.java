package download;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;


public class JSONToXML {
    private static String readFileContent(String filePath) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        br.close();
        return content.toString();
    }

    private static void storeContentInFile(String content, String filePath) throws Exception {
        FileWriter fOut = new FileWriter(filePath);
        fOut.write(content);
        fOut.close();
    }

    public static void transformToXML(String jsonData, String destinationPath) throws Exception {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = JSONSerializer.toJSON(jsonData);
        String xml = serializer.write(json);
        /* System.out.println(xml); **/
        storeContentInFile(xml, destinationPath);
    }

    public static void main(String[] args) throws Exception {
        String source = "/Users/adi/Dropbox/ClementThesis/Nico-Data/deezer/getAlbumByName/Barbra+Streisand";
        String jsonData = readFileContent(source);
        String destination = "test.xml";
        transformToXML(jsonData, destination);

    }
}


