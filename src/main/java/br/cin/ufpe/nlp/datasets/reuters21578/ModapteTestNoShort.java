package br.cin.ufpe.nlp.datasets.reuters21578;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ModapteTestNoShort extends DefaultHandler {
	private Map<String, StartElementFunction> startElementFunctions;
	private Map<String, EndElementFunction> endElementFunctions;
	
	private File outputDir;
	
	private File outputFile;
	
	private BufferedWriter bufw;
	
	private boolean hasProducedText;
	
	private boolean insideBody;
	
	private boolean insideTopics;
	
	private boolean insideDElement;
	
	private String newId;
	
	private List<String> topicList;
	
	private StringBuffer dTopicElement;
	
	public ModapteTestNoShort(File outDir) {
		this.outputDir = outDir;
		this.startElementFunctions = new HashMap<String, StartElementFunction>();
		this.startElementFunctions.put("REUTERS", new StartReutersElement());
		this.startElementFunctions.put("BODY", new StartBodyElement());
		this.startElementFunctions.put("TOPICS", new StartTopicsElement());
		this.startElementFunctions.put("D", new StartDElement());
		
		this.endElementFunctions = new HashMap<String, EndElementFunction>();
		this.endElementFunctions.put("REUTERS", new EndReutersElement());
		this.endElementFunctions.put("BODY", new EndBodyElement());
		this.endElementFunctions.put("TOPICS", new EndTopicsElement());
		this.endElementFunctions.put("D", new EndDElement());
	}
	
	private class EndReutersElement implements EndElementFunction {

		public void execute(String uri, String localName, String qName) throws SAXException {
			nullifyOutput();
		}
		
	}
	
	private class StartTopicsElement implements StartElementFunction {

		public void execute(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			insideTopics = true;
			topicList = new LinkedList<String>();
		}
		
	}
	
	private class StartDElement implements StartElementFunction {

		public void execute(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (insideTopics) {
				dTopicElement = new StringBuffer();
			} else {
				dTopicElement = null;
			}
			insideDElement = true;
		}
		
	}
	
	private class EndDElement implements EndElementFunction {

		public void execute(String uri, String localName, String qName) throws SAXException {
			if (insideTopics) {
				assert(dTopicElement != null);
				String dName = dTopicElement.toString().trim();
				if (dName.length() > 0) {
					topicList.add(dName);
				}
				dTopicElement = null;
			}
			insideDElement = false;
		}
		
	}
	
	private class EndTopicsElement implements EndElementFunction {

		public void execute(String uri, String localName, String qName) throws SAXException {
			insideTopics = false;
			if (topicList.size() == 1) {
				if (newId != null) {
					File myOutputDir = new File(outputDir, topicList.get(0).toLowerCase());
					if(!myOutputDir.exists())
						myOutputDir.mkdir();
					outputFile = new File(myOutputDir, newId);
					try {
						bufw = new BufferedWriter(new FileWriter(outputFile));
					} catch (IOException e) {
						bufw = null;
						throw new SAXException("IOException when trying to create BufferedWriter", e);
					}
					
				}
			}
			topicList = null;
		}
		
	}
	
	private class StartBodyElement implements StartElementFunction {

		public void execute(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			insideBody = true;
		}
		
	}
	
	private class EndBodyElement implements EndElementFunction {

		public void execute(String uri, String localName, String qName) throws SAXException {
			insideBody = false;
		}
		
	}
	
	private class StartReutersElement implements StartElementFunction {
		public void execute(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			nullifyOutput();
			hasProducedText = false;
			Map<String, String> attrValues = new HashMap<String, String>(attributes.getLength());
			for(int i = 0; i < attributes.getLength(); i++) {
				final String attrLocalName = attributes.getLocalName(i);
				final String attrValue = attributes.getValue(i);
				attrValues.put(attrLocalName.toUpperCase(), attrValue);
			}
			
			if(isModapte(attrValues) && isTest(attrValues)) {
				newId = getNewId(attrValues);
			}
		}

	}
	
	private void nullifyOutput() throws SAXException {
		this.newId = null;
		this.topicList = null;
		if (bufw != null) {
			assert(outputFile != null);
			try {
				bufw.close();
				if (!hasProducedText) {
					outputFile.delete();
				}
			} catch (IOException e) {
				throw new SAXException("IOException while trying to close BufferedWriter", e);
			}
			bufw = null;
			outputFile = null;
		}
	}

	
	private static String getNewId(Map<String, String> attrValues) {
		final String ret = attrValues.get("NEWID");
		return ret;
	}
	
	private static boolean isTest(Map<String, String> attrValues) {
		boolean ret = false;
		String lewisSplitType = attrValues.get("LEWISSPLIT");
		if (lewisSplitType != null && lewisSplitType.equalsIgnoreCase("TEST")) {
			ret = true;
		}
		
		return ret;
	}
	
	private static boolean isModapte(Map<String, String> attrValues) {
		boolean ret = false;
		String lewisSplitType = attrValues.get("LEWISSPLIT");
		if (lewisSplitType != null) {
			if (lewisSplitType.equalsIgnoreCase("TRAIN") || lewisSplitType.equalsIgnoreCase("TEST")) {
				String hasTopics = attrValues.get("TOPICS");
				if (hasTopics != null && hasTopics.equalsIgnoreCase("YES")) {
					ret = true;
				}
			}
		}
		return ret;
	}
	
	private Optional<StartElementFunction> startElementFunctionFor(String element) {
		final Optional<StartElementFunction> ret = Optional.ofNullable(this.startElementFunctions.get(element.toUpperCase()));
		return ret;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Optional<StartElementFunction> func = startElementFunctionFor(localName);
		if (func.isPresent())
			func.get().execute(uri, localName, qName, attributes);
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (insideDElement && insideTopics) {
			dTopicElement.append(ch, start, length);
		} else if (insideBody && bufw != null) {
			for (int i = start; i < start + length; i++) {
				try {
					hasProducedText = true;
					bufw.append(ch[i]);
				} catch (IOException e) {
					throw new SAXException("Exception while trying to append characters from body", e);
				}
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Optional<EndElementFunction> func = endElementFunctionFor(localName);
		if (func.isPresent()) {
			func.get().execute(uri, localName, qName);
		}
	}
	
	private Optional<EndElementFunction> endElementFunctionFor(String localName) {
		final Optional<EndElementFunction> ret = Optional.ofNullable(this.endElementFunctions.get(localName.toUpperCase()));
		return ret;
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		//first argument should be directory with xml files
		//second argument should be output directory
		if (args.length < 2) {
			throw new IllegalArgumentException("first argument should be directory with xml files; second argument should be output directory");
		}
		File inputDir = new File(args[0]);
		if (inputDir == null || !inputDir.isDirectory()) {
			throw new IllegalArgumentException("First argument should be input directory");
		}
		if (!inputDir.canRead()) {
			throw new IllegalArgumentException("Input directory is not readable");
		}
		File outputDir = new File(args[1]);
		if (outputDir == null || !outputDir.isDirectory()) {
			throw new IllegalArgumentException("Second argument should be output directory");
		}
		if (!outputDir.canWrite()) {
			throw new IllegalArgumentException("Output directory is not writable");
		}
			
		File[] inputFiles = inputDir.listFiles();
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setNamespaceAware(true);
		saxFactory.setValidating(false);
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setErrorHandler(new SimpleErrorHandler(System.err));
		xmlReader.setContentHandler(new ModapteTestNoShort(outputDir));
		for (File file : inputFiles) {
			if (file.toString().toLowerCase().endsWith(".xml")) {
				BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(file));
				try {
					final InputSource input = new InputSource(bufStream);
					input.setEncoding("UTF-8");
					xmlReader.parse(input);
				} finally {
					bufStream.close();
				}
			}
		}
	}
}
