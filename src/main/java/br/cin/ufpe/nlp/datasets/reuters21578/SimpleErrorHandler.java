package br.cin.ufpe.nlp.datasets.reuters21578;

import java.io.PrintStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
	private PrintStream out;

	public SimpleErrorHandler(PrintStream out) {
		this.out = out;
	}
	
	private String getExceptionInfo(SAXParseException ex) {
		String systemId = ex.getSystemId();
		if (systemId == null)
			systemId = "null";
		String msg = "URI: " + systemId + ", Line: " + ex.getLineNumber()
			+ ", Column: " + ex.getColumnNumber() + ", Message: " + ex.getMessage();
		return msg;
	}
	
	public void warning(SAXParseException exception) throws SAXException {
		out.println("WARNING: " + getExceptionInfo(exception));
	}

	public void error(SAXParseException exception) throws SAXException {
		out.println("ERROR: " + getExceptionInfo(exception));
		throw exception;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		out.println("FATAL: " + getExceptionInfo(exception));
		throw exception;
	}
	
}