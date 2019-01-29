package fr.epsi.book.dal;

import fr.epsi.book.domain.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO implements IDAO<Contact, Long> {
	
	private static final String INSERT_QUERY = "INSERT INTO contact (name, email, phone, type_var, type_num) values (?,?,?,?,?)";
	private static final String FIND_BY_ID_QUERY = "SELECT * FROM contact WHERE id=?";
	private static final String FIND_ALL_QUERY = "SELECT * FROM contact";
	private static final String UPDATE_QUERY = "UPDATE contact SET name=? , email=?, type_var=?, type_num=? WHERE id=?";
	private static final String REMOVE_QUERY = "DELETE FROM contact WHERE id=?";
	private static final String REMOVE_ASSOC_QUERY = "DELETE FROM book_contact WHERE id_contact=?";
	@Override
	public void create( Contact c ) throws SQLException {
		
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( INSERT_QUERY, Statement.RETURN_GENERATED_KEYS );
		st.setString( 1, c.getName() );
		st.setString( 2, c.getEmail() );
		st.setString( 3, c.getPhone() );
		st.setString( 4, c.getType().getValue() );
		st.setInt( 5, c.getType().ordinal() );
		st.executeUpdate();
		ResultSet rs = st.getGeneratedKeys();

		if ( rs.next() ) {
			c.setId( rs.getString( 1 ) );
		}

	}
	
	@Override
	public Contact findById( Long id ) throws SQLException {

		try {
			Connection connection = PersistenceManager.getConnection();
			PreparedStatement st = connection.prepareStatement(FIND_BY_ID_QUERY);
			st.setString(1, id.toString());
			ResultSet rs = st.executeQuery();

			Contact contact = null;
			if (rs.next()) {
				contact = getContactFromResultSet(rs);
			}

			return contact;
		}catch (SQLException se){
			se.getNextException();
			return null;
		}
	}
	
	@Override
	public List<Contact> findAll() throws SQLException{
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( FIND_ALL_QUERY);
		ResultSet rs = st.executeQuery();

		List<Contact> contacts = new ArrayList<>();

		while (rs.next()) {
			Contact contact = getContactFromResultSet(rs);
			contacts.add(contact);

		}

		return contacts;
	}
	
	@Override
	public void update( Contact contact ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(UPDATE_QUERY);

		st.setString(1, contact.getName());
		st.setString(2, contact.getEmail());
		st.setString(3, contact.getType().getValue());
		st.setInt(4, contact.getType().ordinal());
		st.setString(5,contact.getId());

		st.executeQuery();

	}

	@Override
	public void remove( Contact contact ) throws SQLException{

		Connection connection = PersistenceManager.getConnection();
		//suppression des tables d'association
		PreparedStatement stAssoc = connection.prepareStatement(REMOVE_ASSOC_QUERY);
		stAssoc.setString(1, contact.getId());
		stAssoc.executeQuery();

		PreparedStatement st = connection.prepareStatement(REMOVE_QUERY);
		st.setString(1,contact.getId());

		st.executeQuery();
	}

	public static Contact getContactFromResultSet (ResultSet rs) throws SQLException {
		Contact contact = new Contact();
		contact.setId(rs.getString("id"));
		contact.setName(rs.getString("name"));
		contact.setEmail(rs.getString("email"));
		contact.setPhone(rs.getString("phone"));
		return contact;
	}
}


