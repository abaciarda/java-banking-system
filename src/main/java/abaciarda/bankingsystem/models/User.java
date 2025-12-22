package abaciarda.bankingsystem.models;

public class User {
    private final int id;
    private final String name;
    private final String surname;
    private final String ssn;

    public User(int id, String name, String surname, String ssn) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.ssn = ssn;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getSsn() {
        return ssn;
    }
}
