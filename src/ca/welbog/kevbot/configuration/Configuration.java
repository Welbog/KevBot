package ca.welbog.kevbot.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.welbog.kevbot.service.Service;
import ca.welbog.kevbot.responder.Responder;
import ca.welbog.kevbot.responder.ResponderMetadata;
import ca.welbog.kevbot.responder.ResponderMetadata.Type;

public class Configuration {
  
  private Map<String,Service> services = new HashMap<String,Service>();
  private List<ResponderMetadata> responders = new ArrayList<ResponderMetadata>();

  public Configuration(String configFile) {
    // First, build services
    // Then, build responders
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(configFile);
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      
      
      XPathExpression expr = xpath.compile("configuration/service");
      Object o = expr.evaluate(doc,XPathConstants.NODESET);
      NodeList set = (NodeList) o;
      for (int i = 0; i < set.getLength(); i++) {
        Node n = set.item(i);
        String c = n.getAttributes().getNamedItem("class").getTextContent();
        String name = n.getAttributes().getNamedItem("name").getTextContent();
        Service s = (Service)Class.forName(c).newInstance();
        services.put(name, s);
      }
      
      
      expr = xpath.compile("configuration/responder");
      o = expr.evaluate(doc,XPathConstants.NODESET);
      set = (NodeList) o;
      for (int i = 0; i < set.getLength(); i++) {
        Node n = set.item(i);
        String c = n.getAttributes().getNamedItem("class").getTextContent();
        Responder s = (Responder)Class.forName(c).newInstance();
        boolean a = n.getAttributes().getNamedItem("admin") != null;
        if (s.getRequiredServiceNames() != null) {
          for (String serviceName : s.getRequiredServiceNames()) {
            s.addService(serviceName,services.get(serviceName));
          }
        }
        String t = n.getAttributes().getNamedItem("type").getTextContent();
        ResponderMetadata.Type type = Type.CORE;
        if (t.equals("recursive")) {
          type = Type.RECURSIVE;
        }
        ResponderMetadata metadata = new ResponderMetadata(s,a,type);
        responders.add(metadata);
      }
      
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Service getService(String name) {
    return services.get(name);
  }
  
  public List<ResponderMetadata> getResponderMetadata() {
    return responders;
  }
}
