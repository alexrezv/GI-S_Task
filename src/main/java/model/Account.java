package model;

import java.util.Objects;

public class Account {
    private long id;
    private String firstName;
    private String lastName;

    public Account(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Account(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return id + " " + firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            if (obj instanceof Account) {
                return Objects.equals(this.firstName, ((Account) obj).firstName) &&
                        Objects.equals(this.lastName, ((Account) obj).lastName);
            } else {
                return false;
            }
        }
    }
}
