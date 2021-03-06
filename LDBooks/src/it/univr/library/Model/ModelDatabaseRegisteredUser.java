package it.univr.library.Model;

import it.univr.library.Utils.DatabaseConnection;
import it.univr.library.Data.RegisteredClient;
import it.univr.library.Data.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ModelDatabaseRegisteredUser implements ModelRegisteredUser
{
    private DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public RegisteredClient getRegisteredClient(User testUser)
    {
        RegisteredClient user;
        db.DBOpenConnection();
        db.executeSQLQuery( "SELECT name, surname, phoneNumber, email, password " +
                            "FROM registeredUsers " +
                            "WHERE email LIKE ? AND password LIKE ?",
                            List.of(testUser.getEmail(), testUser.getPassword()));

        user = resultSetToUser(db.getResultSet());


        return user;
    }

    private RegisteredClient resultSetToUser(ResultSet rs)
    {
        RegisteredClient user = null;

        try
        {
            while (rs.next())
            {
                user = new RegisteredClient();
                user.setName(db.getSQLString(rs, "name"));
                user.setSurname(db.getSQLString(rs, "surname"));
                user.setEmail(db.getSQLString(rs, "email"));
                user.setPassword(db.getSQLString(rs, "password"));
                user.setPhoneNumber(db.getSQLString(rs, "phoneNumber"));
            }

            return user;
        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return null;
    }
}
