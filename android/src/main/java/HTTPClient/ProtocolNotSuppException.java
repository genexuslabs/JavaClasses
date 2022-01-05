

package HTTPClient;

import java.io.IOException;

/**
 * Signals that the protocol is not supported.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 */
public class ProtocolNotSuppException extends IOException
{

    /**
     * Constructs an ProtocolNotSuppException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public ProtocolNotSuppException()
    {
	super();
    }


    /**
     * Constructs an ProtocolNotSuppException class with the specified
     * detail message.  A detail message is a String that describes this
     * particular exception.
     * @param s the String containing a detail message
     */
    public ProtocolNotSuppException(String s)
    {
	super(s);
    }

}
