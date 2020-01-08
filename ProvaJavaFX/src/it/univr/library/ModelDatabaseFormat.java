package it.univr.library;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModelDatabaseFormat implements Model
{
    private DatabaseConnection db = new DatabaseConnection();

    public ArrayList<Format> getFormats()
    {
        ArrayList<Format> formats;

        db.DBOpenConnection();
        db.executeSQLQuery( "SELECT name " +
                "FROM formats ");

        formats = resultSetToformats(db.getResultSet());
        db.DBCloseConnection();

        return formats;
    }

    private ArrayList<Format> resultSetToformats(ResultSet rs)
    {
        ArrayList<Format> formats = new ArrayList<>();
        Format format;

        try
        {
            while (rs.next())
            {
                format = new Format(db.getSQLString(rs,"name"));
                formats.add(format);
            }

            return formats;
        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return null;
    }

    public void addNewFormat(Format newFormat)
    {
        db.DBOpenConnection();
        db.executeSQLUpdate( "INSERT INTO formats(name) " +
                "VALUES(?)", List.of(newFormat.getName()));
    }
}