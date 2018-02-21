package parsers;

import constants.Settings;
import download.WebService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebServiceDescription {

    public static WebService loadDescription(String webServiceName) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("description-log.txt", "UTF-8");
        /* prefixes **/
        HashMap<String, String> prefixes = new HashMap<>();

        /* head variables **/
        HashMap<String, Integer> headVariableToPosition = new HashMap<>();
        ArrayList<String> headVariables = new ArrayList<>();
        int numberInputs = 0;
        List<String> urlFragments = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(new File(Settings.dirWithDef + webServiceName + ".xml"));

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            Document xmlDocument = builder.parse(file);

            XPath xPath = XPathFactory.newInstance().newXPath();

            //parse prefixes
            String prefix = "/ws/prefix";
            NodeList nodeList = (NodeList) xPath.compile(prefix).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String expr_name = "./@name";
                Node nodeName = (Node) xPath.compile(expr_name).evaluate(nodeList.item(i), XPathConstants.NODE);
                String prefix_name = nodeName.getNodeValue();

                String expr_value = "./@value";
                Node nodeValue = (Node) xPath.compile(expr_value).evaluate(nodeList.item(i), XPathConstants.NODE);
                String prefix_value = nodeValue.getNodeValue();
                prefixes.put(prefix_name.trim(), prefix_value.trim());
                writer.println(" prefix name=" + prefix_name.trim() + "  value=" + prefix_value.trim());

            }

            //parse variables in the head: order matters!
            String headVariableExpr = "/ws/headVariables/variable";
            nodeList = (NodeList) xPath.compile(headVariableExpr).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String expr_name = "./@name";
                String name = ((Node) xPath.compile(expr_name).evaluate(nodeList.item(i), XPathConstants.NODE)).getNodeValue();

                String expr_type = "./@type";
                String type = ((Node) xPath.compile(expr_type).evaluate(nodeList.item(i), XPathConstants.NODE)).getNodeValue();
                if (type.trim().startsWith("in")) numberInputs = i;

                headVariables.add(name.trim());
                headVariableToPosition.put(name.trim(), i);

                writer.println("Variable : " + name + " position " + i);
            }


            //parse url fragments
            String exprURLFragments = "/ws/call/part";
            nodeList = (NodeList) xPath.compile(exprURLFragments).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {

                String expr_type = "./@type";
                String type = ((Node) xPath.compile(expr_type).evaluate(nodeList.item(i), XPathConstants.NODE)).getNodeValue();
                if (type.trim().startsWith("inputValues")) urlFragments.add(null);
                else {
                    String expr_value = "./@value";
                    String fixPart = ((Node) xPath.compile(expr_value).evaluate(nodeList.item(i), XPathConstants.NODE)).getNodeValue();
                    urlFragments.add(fixPart.trim());
                }
            }
            writer.print("The parts of the URLs (calls):");
            for (String part : urlFragments) {
                writer.print(" " + part);
            }
            writer.println("");
            writer.close();
            return new WebService(webServiceName, urlFragments, prefixes, headVariables, headVariableToPosition, numberInputs);

        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            writer.close();
            return null;
        }

    }
}