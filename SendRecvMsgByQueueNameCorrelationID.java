package final_test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SendRecvMsgByQueueNameCorrelationID {

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse("OLTPAPI.xml");
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//configuration/appSettings/add[@value]");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		Map<String, String> map = new HashMap<String, String>();
		
		for (int i = 0; i < nl.getLength(); i++){
		    Node currentItem = nl.item(i);
		    String key = currentItem.getAttributes().getNamedItem("key").getNodeValue().substring(7);
		    String value = currentItem.getAttributes().getNamedItem("value").getNodeValue();
		    
		    map.put(key,value);
		}
		System.out.println(map);
		String serverUrl = map.get("EMSURL");
		String UserName = map.get("UserName");
		String Password = map.get("Password");
		Long TimeOut = Long.parseLong(map.get("TimeOut"));
		String SendQueue = map.get("SendQueue");
		String RecvQueue = map.get("RecvQueue");

		// 叫readXmlFileUsingDomParser去解析xml檔
//		readXmlFileUsingDomParser2 parse = new readXmlFileUsingDomParser2();
//		parse.readXmlFileUsingDomParser();//會將解析出來的結果存入outputFile.txt

		// 讀取outputFile.txt
//		String input = new Scanner(new File("outputFile.txt")).useDelimiter("\\Z").next();

		String input = new Scanner(new File("test.xml")).useDelimiter("\\Z").next();

		// 呼叫JMSHandler中的sendQueueMessage將outputFile.txt裡面的內容傳給test1
		SendHandler sendHandler = new SendHandler();
		sendHandler.sendQueueMessage(serverUrl, UserName, Password, SendQueue, RecvQueue, TimeOut, input);
	}
}
