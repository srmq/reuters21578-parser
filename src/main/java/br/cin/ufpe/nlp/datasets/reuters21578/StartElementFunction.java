package br.cin.ufpe.nlp.datasets.reuters21578;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface StartElementFunction {
	public void execute(String uri, String localName, String qName, Attributes attributes) throws SAXException;
}
