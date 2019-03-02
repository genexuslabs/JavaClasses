package com.genexus;
/**
 * Created by IntelliJ IDEA.
 * User: cmurialdo
 * Date: 11/08/2004
 * Time: 02:43:25 PM
 * To change this template use Options | File Templates.
 */
public class GXFormatNumber {
    static public String InvariantNumber( String n)
    {
        String dSep = ",";//NumberFormat.NumberDecimalSeparator;
        char decimalSeparator = '.';
        if (dSep.length() > 0)
        {
            decimalSeparator = dSep.charAt(0);
            n = n.replace(decimalSeparator, '.');
        }
        return n;
    }

	public static String FormatNumber( String s, String p, char decimalSeparator, char groupSeparator)
	{
		int sStart, pStart, sDec, pDec, i, j, k;
		boolean leftZ = false;
		boolean rightZ = false;

		s = s.trim();
		//s = InvariantNumber( s);
		p = p.trim();
		sDec = 0;
		if ((sStart = s.indexOf('.')) == -1)		// No hay decimales en el numero
			sStart = s.length();
		else
			sDec = s.length() - sStart;				// Cant. decimales (incl. el punto)

		pDec = 0;
		if ((pStart = p.indexOf('.')) == -1)		// No hay decimales en la picture
			pStart = p.length();
		else
			pDec = p.length() - pStart;				// Cant. decimales (incl. el punto)


		StringBuffer result = new StringBuffer();
		int max = Math.max(p.length(), s.length());
		for (int count=0; count < max; count++)
		{
			result.append(' ');
		}
		/// Proceso parte izq. del punto dec.
		j = sStart-1;
		k = pStart-1;
		for (i = k; i >= 0; i--)
		{
			switch( p.charAt(i))
			{
				case '9':
					if (j < 0)
						result.setCharAt(k--,'0');
					else if (s.charAt(j) == ' ')
						result.setCharAt(k--, '0');
					else
						result.setCharAt(k--,s.charAt(j));
					j--;
					break;
				case 'Z':
					if (j < 0)
						result.setCharAt(k--,' ');
					else if (leftZ || leftZero( s, j))
					{
						result.setCharAt(k--,' ');
						leftZ = true;
					}
					else
						result.setCharAt(k--, s.charAt(j));
					j--;
					break;
				case ',':
					if (j < 0)
					{
						if (i > 0)
							if( p.charAt(i-1) == '9')
								if ( groupSeparator != '\0')
									result.setCharAt(k--, groupSeparator);
					}
					else if ((j == 0 && s.charAt(j) != '-') || j > 0)
						if ( groupSeparator != '\0')
							result.setCharAt(k--, groupSeparator);
					break;
				default:
					if ( ! leftZ)
						result.setCharAt(k--, p.charAt(i));
					break;
			}
		}
		/// Proceso parte der. del punto dec.
		if (pDec > 0 )
		{
			j = sStart;
			for (i = pStart; i < p.length(); i++)
			{
				switch( p.charAt(i))
				{
					case '9':
						if (j < s.length())
							result.setCharAt(i,s.charAt(j));
						else
							result.setCharAt(i, '0');
						j++;
						break;
					case 'Z':
						if (rightZ || rightZero( s, j))
						{
							result.setCharAt(i, ' ');
							rightZ = true;
						}
						else if (j < s.length())
							result.setCharAt(i, s.charAt(j));
						j++;
						break;
					case '.':
						if (decimalSeparator != '\0')
							result.setCharAt(i, decimalSeparator);
						j++;
						break;
					default:
						result.setCharAt(i, p.charAt(i));
						break;
				}
			}
		}
		String sResult = result.toString().trim();
		if( sResult.endsWith(String.valueOf(decimalSeparator)))
			// elimino punto decimal al final del String
			sResult = sResult.substring(0, sResult.length()-1);
		return sResult;
	}
	static boolean leftZero(String s, int len)
    {
        char[] numbers = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'};
        if (indexOfAny( s, numbers, 0, len+1) == -1)
            return true;
        return false;
    }
    static int indexOfAny(String s, char[] arr, int start, int count){

        for (int i=0; i<arr.length; i++){
            if (s.indexOf(arr[i]) >= 0 )
                return 1;
        }
        return -1;
    }
    static boolean rightZero(String s, int pos)
    {
        char[] numbers = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        if ( s.length() > pos && indexOfAny(s, numbers, pos, s.length()-pos) == -1)
            return true;
        return false;
    }

}
