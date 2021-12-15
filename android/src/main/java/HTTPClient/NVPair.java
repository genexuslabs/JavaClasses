package HTTPClient;


/**
 * This class holds a Name/Value pair of strings. It's used for headers,
 * form-data, attribute-lists, etc. This class is immutable.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 */
public final class NVPair
{
    /** the name */
    private String name;

    /** the value */
    private String value;


    // Constructors

    /**
     * Creates a new name/value pair and initializes it to the
     * specified name and value.
     *
     * @param name  the name
     * @param value the value
     */
    public NVPair(String name, String value)
    {
	this.name  = name;
	this.value = value;
    }

    /**
     * Creates a copy of a given name/value pair.
     *
     * @param p the name/value pair to copy
     */
    public NVPair(NVPair p)
    {
	this(p.name, p.value);
    }


    // Methods

    /**
     * Get the name.
     *
     * @return the name
     */
    public final String getName()
    {
	return name;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public final String getValue()
    {
	return value;
    }


    /**
     * Produces a string containing the name and value of this instance.
     *
     * @return a string containing the class name and the name and value
     */
    public String toString()
    {
	return getClass().getName() + "[name=" + name + ",value=" + value + "]";
    }
}
