package com.jackpang.config;

import com.jackpang.IdGenerator;
import com.jackpang.ProtocolConfig;
import com.jackpang.compress.Compressor;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.discovery.RegistryConfig;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.SerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * description: XmlResolver
 * date: 11/23/23 8:19 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class XmlResolver {
    /**
     * load configuration from xml  not using dom4j
     * @param configuration configuration
     */
    public void loadFromXml(Configuration configuration) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("jrpc.xml");
            Document doc = builder.parse(inputStream);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            configuration.setPort(resolvePort(xPath, doc));
            configuration.setAppName(resolveAppName(xPath, doc));
            configuration.setIdGenerator(resolveIdGenerator(xPath, doc));
            configuration.setRegistryConfig(resolveRegistryConfig(xPath, doc));

            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(xPath, doc);
            CompressorFactory.addCompressor(compressorObjectWrapper);

            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(xPath, doc);
            SerializerFactory.addSerializer(serializerObjectWrapper);

            configuration.setCompressType(resolveCompressType(xPath, doc));
            configuration.setSerializeType(resolveSerializeType(xPath, doc));


            configuration.setLoadBalancer(resolveLoadBalancer(xPath, doc));

        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.info("No related xml file or load configuration from xml error", e);
        }
    }

    private ObjectWrapper<Serializer> resolveSerializer(XPath xPath, Document doc) {
        Serializer serializer = parseObject(xPath, doc, "/configuration/serializer", null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(xPath, doc, "/configuration/serializer", "code")));
        String name = parseString(xPath, doc, "/configuration/serializer", "name");
        return new ObjectWrapper<>(code, name, serializer);
    }

    private ObjectWrapper<Compressor> resolveCompressor(XPath xPath, Document doc) {
        Compressor compressor = parseObject(xPath, doc, "/configuration/compressor", null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(xPath, doc, "/configuration/compressor", "code")));
        String name = parseString(xPath, doc, "/configuration/compressor", "name");
        return new ObjectWrapper<>(code, name, compressor);
    }

    private String resolveSerializeType(XPath xPath, Document doc) {
        String expression = "/configuration/serializeType";
        return parseString(xPath, doc, expression, "type");
    }

    private String resolveCompressType(XPath xPath, Document doc) {
        String expression = "/configuration/compressType";
        return parseString(xPath, doc, expression, "type");
    }

    private LoadBalancer resolveLoadBalancer(XPath xPath, Document doc) {
        return parseObject(xPath, doc, "/configuration/loadBalancer", null);
    }

    private RegistryConfig resolveRegistryConfig(XPath xPath, Document doc) {
        String url = parseString(xPath, doc, "/configuration/registry", "url");
        return new RegistryConfig(url);
    }

    private IdGenerator resolveIdGenerator(XPath xPath, Document doc) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(xPath, doc, expression, "class");
        String dataCenterId = parseString(xPath, doc, expression, "dataCenterId");
        String machineId = parseString(xPath, doc, expression, "MachineId");
        Class<?> clazz;
        Object instance;
        try {
            clazz = Class.forName(aClass);
            instance = clazz.getConstructor(new Class[]{Long.class, Long.class}).newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return (IdGenerator) instance;

    }

    private String resolveAppName(XPath xPath, Document doc) {
        String expression = "/configuration/appName";
        return parseString(xPath, doc, expression);

    }

    private int resolvePort(XPath xPath, Document doc) {
        String expression = "/configuration/port";
        return Integer.parseInt(parseString(xPath, doc, expression));
    }

    /**
     * parse a node return instance
     * @param xPath xPath parser
     * @param doc xml document
     * @param expression xPath expression
     * @param paramType constructor parameter type
     * @param params constructor parameter
     * @param <T> return type
     * @return instance
     */
    private <T> T parseObject(XPath xPath, Document doc, String expression, Class[] paramType, Object...params)  {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instance;
            if (paramType==null){
                instance = aClass.getConstructor().newInstance();
            }else {
                instance= aClass.getConstructor(paramType).newInstance(params);
            }
            return (T) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | XPathExpressionException e) {
            log.error("An error occurred when parsing the expression", e);
        }
        return null;
    }



    /**
     * parse a node return string <port num="8089"/>
     * @param xPath xPath parser
     * @param doc xml document
     * @param expression xPath expression
     * @param attributeName attribute name
     * @return value of node
     */
    private String parseString(XPath xPath, Document doc, String expression, String attributeName)  {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("An error occurred when parsing the expression", e);
        }
        return null;
    }

    /**
     * parse a node return string <port>8089</port>
     * @param xPath xPath parser
     * @param doc xml document
     * @param expression xPath expression
     * @return value of node
     */
    private String parseString(XPath xPath, Document doc, String expression)  {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("An error occurred when parsing the expression", e);
        }
        return null;
    }

    // configure

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

}
