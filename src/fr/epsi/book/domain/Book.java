package fr.epsi.book.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@XmlRootElement
public class Book implements Serializable {
	
	private String id;
	private String code;
	private Map<String, Contact> contacts;
	
	public Book() {
		id = UUID.randomUUID().toString();
		contacts = new HashMap<>(  );
	}
	
	public String getId() {
		return id;
	}
	
	public void setId( String id ) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode( String code ) {
		this.code = code;
	}
	
	public Map<String, Contact> getContacts() {
		return contacts;
	}
	
	public void setContacts( Map<String, Contact> contacts ) {
		this.contacts = contacts;
	}
	
	public void addContact( Contact contact ) {
		contacts.put( contact.getId(), contact );
	}
	
	public void removeContact( Contact contact ) {
		contacts.remove( contact.getId() );
	}
	
	public void removeContact( String id ) {
		contacts.remove( id );
	}
}
