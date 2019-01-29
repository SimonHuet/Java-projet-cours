package fr.epsi.book.dal;

import fr.epsi.book.domain.Book;
import fr.epsi.book.domain.Contact;

public class DAOFactory {
	
	private DAOFactory() {}
	
	public static IDAO<Contact, Long> getContactDAO() {
		return new ContactDAO();
	}
	
	public static IDAO<Book, Long> getBookDAO() {
		return new BookDAO();
	}
}
