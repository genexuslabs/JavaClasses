/*
 * Created on 27/07/2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.genexus.search;

/**
 * @author Willy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearchException extends Exception
{
        public SearchException(int errCode){
            super(getMessage(errCode));
            m_errCode = errCode;
        }
        private int m_errCode;

        public int getErrCode()
        {
            return m_errCode;
        }
        public void setErrCode(int errCode)
        {
               m_errCode = errCode;
        }

        static String getMessage(int errCode)
        {
                switch (errCode)
                {
                        case NO_ERROR:
                                return "No Error";

                        case COULDNOTCONNECT:
                                return "Could not connect to index files";

                        case IOEXCEPTION:
                                return "Could not open index files";

                        case PARSEERROR:
                                return "Error parsing query string";

        case INDEXERROR:
            return "Invalid collection index";

        default:
                                return "Unknow";
                }
        }

        public static final int NO_ERROR = 0; // NO Error
        public static final int COULDNOTCONNECT = 1; //Could not connect to index
        public static final int PARSEERROR = 2;
        public static final int IOEXCEPTION = 3;
        public static final int INDEXERROR = 4;
}

