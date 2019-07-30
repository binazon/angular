package com.orange.srs.refreport.technical.xml;

import java.io.File;

import javax.xml.bind.JAXBException;

public interface XMLMarshaller<T> {

	/**
	 * Marshall the object to Xml String
	 * 
	 * @param object
	 * @return
	 * @throws JAXBException
	 */
	public String marshallToString(T object) throws JAXBException;

	/**
	 * Unmarshall the Xml String to object
	 * 
	 * @param xml
	 * @return
	 * @throws JAXBException
	 */
	public T unmarshallFromString(String xml) throws JAXBException;

	/**
	 * Marshall the object to Xml file
	 * 
	 * @param object
	 * @param filename
	 * @return
	 * @throws JAXBException
	 */
	public File marshallToFile(T object, File filename) throws JAXBException;

	/**
	 * Unmarshall the Xml File to object
	 * 
	 * @param xml
	 * @return
	 * @throws JAXBException
	 */
	public T unmarshallFromFile(File xmlFile) throws JAXBException;
}