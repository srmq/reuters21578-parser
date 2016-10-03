package br.cin.ufpe.nlp.datasets.reuters21578;

import org.xml.sax.SAXException;

public interface EndElementFunction {
	public void execute(String uri, String localName, String qName) throws SAXException;
}
