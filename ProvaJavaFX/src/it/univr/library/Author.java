package it.univr.library;

public class Author implements Comparable<Author>
{
    private Integer id;
    private String name;
    private String surname;

    public Author(Integer id, String name, String surname)
    {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public String toString()
    {
        return name + " " + surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname(){return surname;}

    public String getNameSurname(){return name + " " + surname;}

    public Integer getId(){return id;}

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Author author = (Author) o;
        return getNameSurname().toUpperCase().equals(author.getNameSurname().toUpperCase()) && id.equals(author.getId());
    }

    @Override
    public int hashCode()
    {
        return name.hashCode()^surname.hashCode();
    }

    @Override
    public int compareTo(Author author)
    {
        if(this.equals(author))
            return 0;
        else
        {
            if(name.compareTo(author.name) == 0)
            {
                if(surname.compareTo(author.surname) == 0)
                    return this.id - author.id;

                return surname.compareTo(author.surname);
            }

            return name.compareTo(author.name);
        }
    }


}