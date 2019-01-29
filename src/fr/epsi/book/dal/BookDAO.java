package fr.epsi.book.dal;

import fr.epsi.book.domain.Book;
import fr.epsi.book.domain.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookDAO implements IDAO<Book, Long> {

	private static final String INSERT_QUERY = "INSERT INTO book (code) values (?)";
	private static final String INSERT_ASSOC_QUERY = "INSERT INTO book_contact(id_book, id_contact) VALUES(?,?)";
	private static final String FIND_BY_ID_QUERY = "SELECT * FROM book WHERE id=?";
	private static final String FIND_CONTACT_ID_QUERY = "SELECT * FROM contact c JOIN book_contact bc ON bc.id_contact=c.id WHERE bc.id_book=?";
	private static final String FIND_ALL_QUERY = "SELECT * FROM book";
	private static final String UPDATE_QUERY = "UPDATE book SET code=? WHERE id=?";
	private static final String UPDATE_ASSOC_QUERY = "UPDATE book_contact SET id_contact=? WHERE id_book=?";
	private static final String REMOVE_ASSOC_QUERY = "DELETE FROM book_contact WHERE id_book=?";
	private static final String REMOVE_QUERY = "DELETE FROM book WHERE id=?";
	
	@Override
	public void create( Book book ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( INSERT_QUERY, Statement.RETURN_GENERATED_KEYS );
		st.setString( 1, book.getCode());
		st.executeUpdate();

		ResultSet rs = st.getGeneratedKeys();

		if ( rs.next() ) {
			book.setId( rs.getString( 1 ) );
		}

		PreparedStatement stAssoc = connection.prepareStatement(INSERT_ASSOC_QUERY);
		Map<String, Contact> contacts = book.getContacts();
		for (Map.Entry<String, Contact> entry : contacts.entrySet()){
			stAssoc.setString(1, book.getId());
			stAssoc.setString(2, entry.getValue().getId());
			stAssoc.executeQuery();
		}

	}
	
	@Override
	public Book findById( Long id ) throws SQLException{
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( FIND_BY_ID_QUERY);
		st.setString(1, id.toString());
		ResultSet rs = st.executeQuery();
		PreparedStatement stContact = connection.prepareStatement( FIND_CONTACT_ID_QUERY);
		ResultSet rsContact = stContact.executeQuery();
		Book book = null;
		if (rs.next()) {
			book = new Book();
			book.setCode(rs.getString("code"));
			while(rsContact.next()){
				Contact contact = ContactDAO.getContactFromResultSet(rs);
				book.addContact(contact);
			}
		}

		return book ;
	}

	@Override
	public List<Book> findAll() throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( FIND_ALL_QUERY );
		ResultSet rs = st.executeQuery();
		PreparedStatement stContact = connection.prepareStatement( FIND_CONTACT_ID_QUERY );
		ResultSet rsContact = stContact.executeQuery();
		List<Book> books = new ArrayList<>();
		while(rs.next()) {
			Book book = new Book();
			book.setCode(rs.getString("code"));

			while(rsContact.next()){
				Contact contact = ContactDAO.getContactFromResultSet(rs);
				book.addContact(contact);
			}
			books.add(book);
		}

		return books ;
	}
	
	@Override
	public void update( Book book ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(UPDATE_QUERY);
		st.setString(1, book.getCode());
		st.executeQuery();
		PreparedStatement stAssoc = connection.prepareStatement(UPDATE_ASSOC_QUERY);

		Map<String, Contact> contacts = book.getContacts();
		for (Map.Entry<String, Contact> entry : contacts.entrySet()){
			stAssoc.setString(1, book.getId());
			stAssoc.setString(2, entry.getValue().getId());
			stAssoc.executeQuery();
		}

	}
	
	@Override
	public void remove( Book book ) throws SQLException{
		Connection connection = PersistenceManager.getConnection();

		PreparedStatement stContact = connection.prepareStatement(REMOVE_ASSOC_QUERY);
		stContact.setString(1,book.getId());
		stContact.executeQuery();

		PreparedStatement st = connection.prepareStatement(REMOVE_QUERY);
		st.setString(1,book.getId());
		st.executeQuery();
	}

}
