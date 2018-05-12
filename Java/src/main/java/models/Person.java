package main.java.models;

import main.java.client.InvalidFieldException;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "people", uniqueConstraints = {@UniqueConstraint(columnNames = "fiscal_code")})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Person extends BaseModel {

    protected Long id;
    protected String fiscalCode;
    protected String firstName;
    protected String lastName;
    protected Date birthDate;
    protected String address;
    protected String telephone;

    protected Collection<Ingredient> allergies = new ArrayList<>();
    protected Collection<Ingredient> intollerances = new ArrayList<>();
    protected Collection<Contact> contacts = new ArrayList<>();


    /**
     * Default constructor
     */
    public Person() {
        this(null, null, null, null, null, null);
    }


    /**
     * Constructor
     *
     * @param   fiscalCode      fiscal code
     * @param   firstName       first name
     * @param   lastName        last name
     * @param   birthDate       birth date
     * @param   address         address
     * @param   telephone       telephone
     */
    public Person(String fiscalCode, String firstName, String lastName, Date birthDate, String address, String telephone) {
        this.fiscalCode = fiscalCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.address = address;
        this.telephone = telephone;
    }


    /** {@inheritDoc} */
    @Transient
    @Override
    public void checkDataValidity() throws InvalidFieldException {
        // Fiscal code: [A-Z] [0-9] 16 chars length
        if (fiscalCode == null || fiscalCode.isEmpty()) throw new InvalidFieldException("Codice fiscale mancante");
        if (fiscalCode.length() != 16) throw new InvalidFieldException("Codice fiscale non valido");

        // First name: [a-z] [A-Z] space
        if (firstName == null || firstName.isEmpty()) throw new InvalidFieldException("Nome mancante");
        if (!firstName.matches("^[a-zA-Z\\040]+$")) throw new InvalidFieldException("Nome non valido");

        // Last name: [a-z] [A-Z] space
        if (lastName == null || lastName.isEmpty()) throw new InvalidFieldException("Cognome mancante");
        if (!lastName.matches("^[a-zA-Z\\040]+$")) throw new InvalidFieldException("Cognome non valido");

        // Address: [a-z] [A-Z] space . , ; \ / °
        if (address != null && !address.matches("^[a-zA-Z\\040.,;°\\\\\\/]+$")) throw new InvalidFieldException("Indirizzo non valido");

        // Telephone: [0-9] space +
        if (telephone != null && !telephone.matches("^[\\d\\040+]+$")) throw new InvalidFieldException("Telefono non valido");
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Person)) return false;

        Person that = (Person)obj;
        return Objects.equals(getFiscalCode(), that.getFiscalCode()) &&
                Objects.equals(getFirstName(), that.getFirstName()) &&
                Objects.equals(getLastName(), that.getLastName()) &&
                Objects.equals(getBirthdate(), that.getBirthdate()) &&
                Objects.equals(getAddress(), that.getAddress()) &&
                Objects.equals(getTelephone(), that.getTelephone());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getFiscalCode());
    }


    @Id
    @GenericGenerator(name = "native_generator", strategy = "native")
    @GeneratedValue(generator = "native_generator")
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "fiscal_code", nullable = false)
    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    @Column(name = "name", nullable = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "surname", nullable = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "birthdate")
    public Date getBirthdate() {
        return birthDate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthDate = birthdate;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "telephone")
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "allergies",
            joinColumns = { @JoinColumn(name = "person_id") },
            inverseJoinColumns = { @JoinColumn(name = "ingredient_id") }
    )
    public Collection<Ingredient> getAllergies() {
        return allergies;
    }

    public void setAllergies(Collection<Ingredient> allergies) {
        this.allergies = allergies;
    }

    @ManyToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "intollerances",
            joinColumns = { @JoinColumn(name = "person_id") },
            inverseJoinColumns = { @JoinColumn(name = "ingredient_id") }
    )
    public Collection<Ingredient> getIntollerances() {
        return intollerances;
    }

    public void setIntollerances(Collection<Ingredient> intollerances) {
        this.intollerances = intollerances;
    }

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "contacts",
            joinColumns = { @JoinColumn(name = "child_id") },
            inverseJoinColumns = { @JoinColumn(name = "contact_id") }
    )
    public Collection<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<Contact> contacts) {
        this.contacts = contacts;
    }

}
