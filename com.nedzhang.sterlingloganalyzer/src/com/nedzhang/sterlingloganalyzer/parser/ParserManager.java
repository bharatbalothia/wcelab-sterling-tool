package com.nedzhang.sterlingloganalyzer.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ParserManager {

	private static final Object instanceCreationLock = new Object();

	private static ParserManager instance;

	private final HashMap<String, LogLineParser> parserMap;
	
	private final ArrayList<String> parserkeyList;

	private ParserManager() throws XPathExpressionException {


		parserMap = new HashMap<String, LogLineParser>();
		
		parserkeyList = new ArrayList<String>();

		loadParser(parserkeyList, parserMap);
	}

	public static ParserManager getInstance() throws XPathExpressionException {

		if (instance == null) {
			synchronized (instanceCreationLock) {
				if (instance == null) {
					instance = new ParserManager();
				}
			}
		}

		return instance;

	}

	private void loadParser(ArrayList<String> parserkeyList, Map<String, LogLineParser> parserMap)
			throws XPathExpressionException {

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		XPathExpression xpathSterlingLogParser = xpath
				.compile("/SterlingLogParsers/SterlingLogParser");
		XPathExpression xpathValidationExpression = xpath
				.compile("ValidationExpression");
		XPathExpression xpathParsingExpression = xpath
				.compile("ParsingExpression");
		XPathExpression xpathTimeStampFormatExpression = xpath
				.compile("TimeStampFormat");

		NodeList sterlingLogParserNodes = (NodeList) xpathSterlingLogParser
				.evaluate(
						new InputSource(this.getClass().getResourceAsStream(
								"/SterlingLogParserConfig.xml")),
						XPathConstants.NODESET);

		for (int i = 0; i < sterlingLogParserNodes.getLength(); i++) {
			Element parserElement = (Element) sterlingLogParserNodes.item(i);

			final String parserName = parserElement.getAttribute("name");

			final String defaultFlag = parserElement.getAttribute("default");

			final String validationExpression = getNodeInnerTextTrimmed(
					parserElement, xpathValidationExpression);

			final String parsingExpression = getNodeInnerTextTrimmed(
					parserElement, xpathParsingExpression);

			final String timeStampFormat = getNodeInnerTextTrimmed(
					parserElement, xpathTimeStampFormatExpression);

			final LogLineRegexParser parser = new LogLineRegexParser(
					validationExpression, parsingExpression, timeStampFormat);

			parserMap.put(parserName, parser);

			boolean isDefault = i == 0
					|| (defaultFlag != null && defaultFlag.length() > 0 && ("true"
							.equalsIgnoreCase(defaultFlag)
							|| "yes".equalsIgnoreCase(defaultFlag) || "y"
								.equalsIgnoreCase(defaultFlag)));

			parser.setProperty("default", String.valueOf(isDefault));
			
			parserkeyList.add(parserName);
		}
	}

	public String[] getParserNames() {
		String[] names = new String[parserkeyList.size()];
		return parserkeyList.toArray(names);
	}

	public LogLineParser getParser(String name) {
		return parserMap.get(name);
	}

	private static String getNodeInnerTextTrimmed(Node node,
			XPathExpression xpath) throws XPathExpressionException {

		Node targetNode = (Node) xpath.evaluate(node, XPathConstants.NODE);

		if (targetNode != null) {
			String textContent = targetNode.getTextContent();

			if (textContent != null && textContent.length() > 0) {
				textContent = textContent.trim();
			}
			return textContent;
		} else {
			return null;
		}
	}

}
