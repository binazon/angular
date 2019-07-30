package com.orange.srs.refreport.technical.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

public abstract class JAXBXMLMarshaller<T> implements XMLMarshaller<T> {

	private static final Logger LOGGER = Logger.getLogger(JAXBXMLMarshaller.class);

	private static final String XML_ENCODING = "ISO-8859-1";
	private static final boolean XML_FORMATTING = true;

	private Marshaller xmlMarshaller;
	private Unmarshaller xmlUnmarshaller;

	private String schemaUrl = null;

	/**
	 * Create an JAXBXMLMarshaller
	 */
	public JAXBXMLMarshaller() {

		try {
			JAXBContext context = JAXBContext.newInstance(getGenericClass());

			// The Marshaller
			xmlMarshaller = context.createMarshaller();
			xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, XML_FORMATTING);
			xmlMarshaller.setProperty(Marshaller.JAXB_ENCODING, XML_ENCODING);

			// The Unmarshaller
			xmlUnmarshaller = context.createUnmarshaller();
			if (schemaUrl != null && !"".equals(schemaUrl)) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(new URL(schemaUrl));
				xmlUnmarshaller.setSchema(schema);
			}

		} catch (Exception e) {
			LOGGER.error("Can't intanciate the JAXBXMLMarshaller", e);
		}
	}

	/**
	 * Get the generic Class of the JAXBXMLMarshaller
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getGenericClass() {

		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
	}

	public String marshallToString(T object) throws JAXBException {

		StringWriter writer = new StringWriter();
		xmlMarshaller.marshal(object, writer);
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public T unmarshallFromString(String xml) throws JAXBException {

		StringReader reader = new StringReader(xml);
		return (T) xmlUnmarshaller.unmarshal(reader);
	}

	public File marshallToFile(T object, File filename) throws JAXBException {

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(filename);
			xmlMarshaller.marshal(object, fileOutputStream);
			return filename;
		} catch (Exception e) {
			LOGGER.error("marshallToFile error while marshalling file " + filename + " " + e.getMessage(), e);
			// Failed to write File
			return null;
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				LOGGER.debug("unmarshallFromFile error while unmarshalling file " + filename + " " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public T unmarshallFromFile(File filename) throws JAXBException {

		FileInputStream fileInputStream = null;
		T result = null;
		try {
			fileInputStream = new FileInputStream(filename);
			result = (T) xmlUnmarshaller.unmarshal(fileInputStream);
		} catch (Exception e) {
			// Failed to read File
			LOGGER.error("unmarshallFromFile error while unmarshalling file " + filename + " " + e.getMessage(), e);
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				LOGGER.debug("unmarshallFromFile error while unmarshalling file " + filename + " " + e.getMessage(), e);
			}
		}

		return result;
	}
}
