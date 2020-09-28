
package com.genexus.util;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.DynamicExecute;
import com.genexus.GxUnknownObjectCollection;

import java.math.*;

/** Esta clase se encarga de evaluar una expresion
 *
 *  El formato de la expresion permite constantes numericas, PI, operadores +, -, *, / y las siguientes
 *  funciones matematicas (pow, sqrt, cos, sin, tan, acos, asin, atan, floor, round, exp, ln, abs, int, frac, max, min)
 *  Las funciones max, min, y pow reciben 2 argumentos
 *  La funcion rnd() no recibe argumentos y retorna un n�mero aleat�rio entre 0 y 1
 *  Las demas funciones reciben 1 argumento
 */
public class ExpressionEvaluator
{
	ModelContext context;
	int handle;
	boolean throwExceptions = false;
	//Hashtable parms = new Hashtable();
	GXProperties parms = new GXProperties();
	short UNKNOWN_ERROR = 1;
	short PARAMETER_ERROR = 2;
	short EXPRESSION_ERROR = 3;
	short EVALUATION_ERROR = 4;
	short EXTERNAL_FUNCTION_ERROR = 5;
	boolean iifContext = false;

	public ExpressionEvaluator(ModelContext context, int handle, String varParms)
	{
		this.handle = handle;
		this.context = context;
		setParms(varParms);		
	}
	public ExpressionEvaluator(ModelContext context, int handle)
	{
		this.handle = handle;
		this.context = context;
		setParms("");
	}

	// Propiedades/Metodos del objeto Expression
	public GXProperties getVariables()
	{
		return parms;
	}
	String expr;
	public String getExpression()
	{
		return expr; 
	}
	public void setExpression( String value)
	{
		expr = value; 
	}
	short errCode;
	public short getErrCode()
	{ 
		return errCode; 
	}
	String errDescription;
	public String getErrDescription()
	{
		return errDescription;
	}
	public BigDecimal evaluate()
	{
		try
		{
			return eval(expr).getDecimal();
		}
		catch (IllegalArgumentException e)
		{
			return throwException(UNKNOWN_ERROR, e.getMessage()).getDecimal();
		}
	}
	@SuppressWarnings("unchecked")
	public GxUnknownObjectCollection getUsedVariables()
	{
		FastTokenizer ft = new FastTokenizer(getTokenizerExpression(expr));
		GxUnknownObjectCollection ls = new GxUnknownObjectCollection();

		String tk;
		tk = ft.nextToken();
		while (!ft.eof())
		{
			if (tk.indexOf("(") == -1 && ! Character.isDigit(tk.charAt(0)))
				ls.add(tk);
			tk = ft.nextToken();
		}
		return ls;
	}

	// Fin Propiedades/Metodos del objeto expression

	/** Setea las variables y sus valores.
	 * El formato es el siguiente: VarName1=Valor1;VarName2=Valor2;....;VarNameN=ValorN
	 */
	public void setParms(String varParms)
	{
		parms.clear();
		Tokenizer tokenizer = new Tokenizer(varParms, ";", false);
		while(tokenizer.hasMoreTokens())
		{
			String parm = tokenizer.nextToken().trim();
			if(parm.equals(""))continue;
			int index = parm.indexOf('=');
			if(index == -1 || Character.isDigit(parm.charAt(0)))
			{
				throwException(PARAMETER_ERROR, "Parm " + parm + " does not comply with format: 'ParmName=ParmValue'");
			}else
			{
				try
				{
					String parmName = parm.substring(0, index).trim().toLowerCase();
					String parmValue = parm.substring(index+1).trim();
					if(parmValue.length() > 0 && !Character.isDigit(parmValue.charAt(0)))
					{
						if (parms.containsKey( parmValue) )
						{
							parms.put(parmName, parms.get(parmValue));
						}else
						{
							throwException(PARAMETER_ERROR, "Unknown parameter '" + parmValue + "'"); 
						}
						continue;
					}
					parms.put(parmName, parmValue); 
				}catch(Throwable e)
				{
					throwException(PARAMETER_ERROR, "Parm " + parm + " cannot be evaluated: " + e.getMessage());
				}
			}
		}
	}

	public void setThrowExceptions(boolean throwExceptions)
	{
		this.throwExceptions = throwExceptions;
	}

	public static BigDecimal eval(ModelContext context, int handle, String expression, String parms)
	{
		return new ExpressionEvaluator(context, handle, parms).eval(expression).getDecimal();
	}

	public static BigDecimal eval(ModelContext context, int handle, String expression, byte [] err, String [] errMsg, String parms)
	{
		err[0] = 1; 
		errMsg[0] = "";
		try
		{
			if(expression.trim().equals(""))
			{
				return new BigDecimal(0);
			}
			ExpressionEvaluator eval = new ExpressionEvaluator(context, handle, parms);
			return eval.eval(expression).getDecimal();
		}catch(NumberFormatException e)
		{
			err[0] = 0;
			errMsg[0] = "Invalid number: " + e.getMessage();
			return new BigDecimal(0);
		}catch(Throwable e)
		{
			err[0] = 0;
			errMsg[0] = e.getMessage();
			return new BigDecimal(0);
		}
	}

	EvalValue eval(String expression)
	{
		errCode = 0;
		errDescription = "";
		if (expression == "")
			return throwException(EXPRESSION_ERROR, "Empty expression");

		if (!matchParentesis(expression))
		{
			return throwException(EXPRESSION_ERROR, "The expression '" + expression + "' has unbalanced parenthesis");
		}
		String delim = "'!+-/*><=" + GE + LE + AND + OR + NE;
		boolean useParentheses = false;
		if (iifContext && (expression.contains(""+AND) || expression.contains(""+OR)))
		{
			delim = "" + AND + OR;
			useParentheses = true;
		}
		Tokenizer tokenizer = new Tokenizer(getTokenizerExpression(expression), delim, true, useParentheses);
		return evaluate(expression, tokenizer);
	}

	private EvalValue throwException(short errCod, String error)
	{
		if(throwExceptions)
		{
			throw new IllegalArgumentException(error);
		}else
		{
			errCode = errCod;
			errDescription = error;
			//System.err.println(error);
			return new EvalValue(new BigDecimal(0));
		}	
	}

	private static final char GE = 0x01;
	private static final char LE = 0x02;
	private static final char AND = (char)(0x03);
	private static final char OR = (char)(0x04);
	private static final char NE = (char)(0x05);
	private String getTokenizerExpression(String expression)
	{
		int index;
		while((index = expression.indexOf("==")) != -1)
			expression = expression.substring(0, index) + expression.substring(index+1);	   
		while((index = expression.indexOf(">=")) != -1)
			expression = expression.substring(0, index) + GE + expression.substring(index+2);
		while((index = expression.indexOf("<=")) != -1)
			expression = expression.substring(0, index) + LE + expression.substring(index+2);
		while ((index = expression.indexOf("&&")) != -1)
			expression = expression.substring(0, index) + AND + expression.substring(index + 2);
		while ((index = expression.indexOf("||")) != -1)
			expression = expression.substring(0, index) + OR + expression.substring(index + 2);
		while ((index = expression.indexOf("!=")) != -1)
			expression = expression.substring(0, index) + NE + expression.substring(index + 2);
		while ((index = expression.indexOf("<>")) != -1)
			expression = expression.substring(0, index) + NE + expression.substring(index + 2);
		while ((index = indexOfKeyword(expression, "and", index)) != -1)
			expression = expression.substring(0, index) + AND + expression.substring(index + 3);
		while ((index = indexOfKeyword(expression, "or", index)) != -1)
			expression = expression.substring(0, index) + OR + expression.substring(index + 2);
		return expression;
	}

	private int indexOfKeyword(String expression, String keyw, int nextIndex)
	{
		String exp = expression.toLowerCase();
		int index = exp.indexOf(keyw.toLowerCase(), nextIndex + 1);
		while (index >= 0)
		{
			if (validKeyword(expression, index, keyw))
				return index;
			index = exp.indexOf(keyw.toLowerCase(), index + 1);
		}
		return index;
	}
	private Boolean validKeyword( String expression, int index, String keyw)
	{
		if (index-1 >= 0)
			if (Character.isLetterOrDigit(expression.charAt(index - 1)) || "()!".indexOf(expression.charAt(index-1)) != -1)
				return false;
		if (index + keyw.length() < expression.length())
			if (Character.isLetterOrDigit(expression.charAt(index + keyw.length())) || "()!".indexOf(expression.charAt(index + keyw.length())) != -1)
				return false;
		return true;
	}

	private EvalValue evaluate(String fullExpression, Tokenizer tokenizer)
	{
		return evaluate(fullExpression, tokenizer, false);
	}
	
	private EvalValue evaluate(String fullExpression, Tokenizer tokenizer, boolean stopOnLowPrecedence)
	{
		EvalValue retVal = eval(tokenizer);
		EvalValue termino = new EvalValue(new BigDecimal(0));
		while (tokenizer.hasMoreTokens())
		{
			if (stopOnLowPrecedence)
			{
				String[] nextOp = new String[1];
				if (tokenizer.peek(nextOp) && nextOp[0].length() >= 1 && (nextOp[0].charAt(0) == '+' || nextOp[0].charAt(0) == '-'))
					break;
			}

			String soperador = tokenizer.nextToken(); 
			char operador = soperador.charAt(0);
			// Obtengo el proximo termino
			switch (operador)
			{
			case '+':
				termino = evaluate(fullExpression, tokenizer, true);
				retVal = EvalValue.add(retVal, termino);
				break;
			case '-':
				termino = evaluate(fullExpression, tokenizer, true);
				retVal = EvalValue.subtract(retVal, termino);
				break;
			case '*':
				termino = eval(tokenizer);
				retVal = EvalValue.multiply( retVal, termino);
				break;
			case '/':
				termino = eval(tokenizer);
				if(termino.isFalse() && errCode == 0)
					throwException(EVALUATION_ERROR, "Division by zero");
				if (errCode == 0)
					retVal = EvalValue.divide( retVal, termino);
				break;
			case '>': return EvalValue.greater(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			case '<': return EvalValue.less(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			case '=': return EvalValue.equal(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			case GE: return EvalValue.greaterOrEqual(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			case LE: return EvalValue.lessOrEqual(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			case AND: return (retVal.isTrue()) && (evaluate(fullExpression, tokenizer).isTrue()) ? EvalValue.trueValue() : EvalValue.falseValue();
			case OR: return (retVal.isTrue()) || (evaluate(fullExpression, tokenizer).isTrue()) ? EvalValue.trueValue() : EvalValue.falseValue();
			case NE: return EvalValue.notEqual(retVal, evaluate(fullExpression, tokenizer)) ? EvalValue.trueValue() : EvalValue.falseValue();
			default:
				throwException(EVALUATION_ERROR, "Unknown operator '" + soperador + "' found in expression '" + fullExpression + "'");
				break;
			}
		}
		return retVal;
	}

	private EvalValue eval(Tokenizer tokenizer)
	{
		String token = getNextToken(tokenizer);

		if (token.equalsIgnoreCase("!"))
		{ // Si se trata de una expresion del tipo '!expr'
			return eval(tokenizer).isFalse() ? EvalValue.trueValue() : EvalValue.falseValue();
		}
		if (token.equalsIgnoreCase("-"))
		{ // Si se trata de una expresion del tipo '-expr'
			return EvalValue.multiply( new EvalValue(new BigDecimal(-1)), eval(tokenizer));
		}
		if (token.equalsIgnoreCase("+"))
		{ // Si se trata de una expresion del tipo '+expr'
			return eval(tokenizer);
		}

		// Hasta aqui tengo el termino a procesar
		// Entonces, veo si lo que tengo es una expresion entre parentesis,
		// un numero, o una funcion
		if (token.startsWith("(") && token.endsWith(")"))
		{
			// Si es una expresion entre parentesis
			return eval(token.substring(1, token.length() - 1).trim());
		}
		if (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.')
		{ // Si se trata de una constante (numero)
			try
			{
				return new EvalValue( new BigDecimal(token));
			}
			catch(Exception e)
			{
				return throwException(EVALUATION_ERROR, "Invalid variable reference: " + token);
			}
		}
		if (token.startsWith("'"))
		{
			// Si se trata de una constante (char)
			String s = "";
			String tk = tokenizer.nextToken();
			while (! tk.equals("'"))
			{
				s += tk;
				tk = tokenizer.nextToken();
			}
			return new EvalValue(s);
		}
		if (token.equalsIgnoreCase("PI"))
		{
			return new EvalValue(new BigDecimal(Math.PI));
		}

		if(parms.containsKey( token))
		{ // Si era una variable, retorno su valor
			return eval(parms.get(token));
			//return (new EvalValue(new Double(parms.get(token)).doubleValue()));
		}

		// Si no es nada de esto, debe ser una funcion

		int indexLeftParen = token.indexOf('(');
		int indexRightParen = token.lastIndexOf(')');
		if(indexLeftParen == -1 || indexRightParen == -1)
		{ // Si no es una funcion, es una variable sin referenciar
			return throwException(EVALUATION_ERROR, "Invalid variable reference: " + token);
		}

		String funcName = token.substring(0, indexLeftParen);
		double result = evalFuncCall(funcName, token.substring(indexLeftParen + 1, indexRightParen));
		if (Double.isInfinite(result) || Double.isNaN(result))
		{
			return new EvalValue(new BigDecimal(0));
		}
		else
		{
			return new EvalValue(new BigDecimal(result));
		}
	}

	/** Evalua la llamada a una funcion
	 *  @param funcName Nombre de la funcion
	 *  @param expr Expresion con los parametros
	 *  @return resultado de la evaluacion
	 */
	private double evalFuncCall(String funcName, String expr)
	{
		if (funcName.equalsIgnoreCase("rnd"))
		{ // Si se trata de la funcion random
			return Math.random();
		}

		// Primero veo si es una funcion de 1 sola variable
		if (funcName.equalsIgnoreCase("abs"))
		{
			return Math.abs(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("int"))
		{
			return (double)(eval(expr).getDecimal().longValue());
		}
		if (funcName.equalsIgnoreCase("frac"))
		{
			double value = eval(expr).getDecimal().doubleValue() ;
			return value - (double) ( (long) value);
		}
		if (funcName.equalsIgnoreCase("sin"))
		{
			return Math.sin(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("asin"))
		{
			return Math.asin(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("cos"))
		{
			return Math.cos(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("acos"))
		{
			double x = eval(expr).getDecimal().doubleValue() ;
			if (x > 1 || x < -1)
				throwException(EVALUATION_ERROR, "Invalid range");
			return Math.acos(x);
		}
		if (funcName.equalsIgnoreCase("tan"))
		{
			return Math.tan(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("atan"))
		{
			return Math.atan(eval(expr).getDecimal().doubleValue());
		}
		if (funcName.equalsIgnoreCase("floor"))
		{
			return Math.floor(eval(expr).getDecimal().doubleValue() );
		}
		if (funcName.equalsIgnoreCase("round"))
		{
			return Math.round(eval(expr).getDecimal().doubleValue() );
		}
		if (funcName.equalsIgnoreCase("trunc"))
		{
			return Math.floor(eval(expr).getDecimal().doubleValue() );
		}	  
		if (funcName.equalsIgnoreCase("ln") ||  // tanto ln como log calculan el logaritmo neperiano
				funcName.equalsIgnoreCase("log"))
		{
			double val = eval(expr).getDecimal().doubleValue() ;
			if (val <= 0)
			{
				return throwException(EVALUATION_ERROR, "Illegal argument (" + val + ") to function log(" + expr + ")").getDecimal().doubleValue() ;
			}
			return Math.log(val);
		}
		if (funcName.equalsIgnoreCase("exp"))
		{
			return Math.exp(eval(expr).getDecimal().doubleValue() );
		}
		if (funcName.equalsIgnoreCase("sqrt"))
		{
			return Math.sqrt(eval(expr).getDecimal().doubleValue() );
		}

		// Ahora veo si es una de las funciones internas de 2 variables
		if(funcName.equalsIgnoreCase("pow") ||
				funcName.equalsIgnoreCase("max") ||
				funcName.equalsIgnoreCase("min"))
		{
			Tokenizer paramTokenizer = new Tokenizer(expr, ",", true);
			String sarg1, sarg2;
			try
			{
				sarg1 = getNextToken(paramTokenizer);
				paramTokenizer.nextToken();
				sarg2 = getNextToken(paramTokenizer);
			}
			catch (NoSuchElementException e)
			{
				return throwException(EVALUATION_ERROR, "The function " + funcName + " needs 2 arguments").getDecimal().doubleValue() ;
			}

			double arg1 = eval(sarg1).getDecimal().doubleValue() ;
			double arg2 = eval(sarg2).getDecimal().doubleValue() ;

			if (funcName.equalsIgnoreCase("pow"))
			{
				return Math.pow(arg1, arg2);
			}
			if (funcName.equalsIgnoreCase("max"))
			{
				return Math.max(arg1, arg2);
			}
			if (funcName.equalsIgnoreCase("min"))
			{
				return Math.min(arg1, arg2);
			}
		}

		if(funcName.equalsIgnoreCase("iif"))
		{ // Si se trata de un iif
			Tokenizer paramTokenizer = new Tokenizer(expr, ",", true);
			String sarg1, sarg2, sarg3;
			try
			{
				sarg1 = getNextToken(paramTokenizer);
				paramTokenizer.nextToken();
				sarg2 = getNextToken(paramTokenizer);
				paramTokenizer.nextToken();
				sarg3 = getNextToken(paramTokenizer);
			}
			catch (NoSuchElementException e)
			{
				return throwException(EVALUATION_ERROR, "The function " + funcName + " needs 3 arguments").getDecimal().doubleValue() ;
			}
			iifContext = true;
			Boolean iif_result = eval(sarg1).isTrue();
			if (errCode != 0)
				return 0;

			double result;
			if(iif_result)
				result = eval(sarg2).getDecimal().doubleValue() ;
			else 
				result = eval(sarg3).getDecimal().doubleValue() ;
			iifContext = false;
			return result;
		}

		return evalExternalFunctionCall(funcName, expr);		 
	}

	@SuppressWarnings("unchecked")
	private double evalExternalFunctionCall(String funcName, String expr)
	{
		Tokenizer paramTokenizer = new Tokenizer(expr.trim(), ",", true);
		Vector functionParms = new Vector();
		while(paramTokenizer.hasMoreTokens())
		{
			String arg = getNextToken(paramTokenizer).trim();
			if((arg.startsWith("\"") || arg.startsWith("'")) &&
					(arg.endsWith("\"") || arg.endsWith("'")))
			{ // Si es un string
				functionParms.addElement(arg.substring(1, arg.length()-1));
			}
			else
			{ // Sino evaluo el parametro
				// Sino evaluo el parametro
				EvalValue eValue = eval(arg);
				// Se pasa el tipo de parametro que corresponde
				if (eValue.getString() == null)
					functionParms.addElement(new Double(eValue.getDecimal().doubleValue() ));
				else
					functionParms.addElement(new String(eValue.getString()));
			}

			if(paramTokenizer.hasMoreTokens())
			{ // Consumo el ','
				paramTokenizer.nextToken();
			}
		}
		functionParms.addElement(new Double(0)); // Ahora agrego el parm donde quedara el retorno
		Object [] callParms = new Object[functionParms.size()];
		functionParms.copyInto(callParms);

		try
		{			  
			funcName = funcName.toLowerCase();

			DynamicExecute.dynamicExecute(context, handle, ExpressionEvaluator.class, funcName, callParms);
		}catch(Exception e)
		{
			return throwException(EXTERNAL_FUNCTION_ERROR, e.toString()).getDecimal().doubleValue() ;
		}

		return ((Double)callParms[callParms.length - 1]).doubleValue();	   
	}

	private String getNextToken(Tokenizer tokenizer)
	{
		String token = "";
		do
		{
			token += tokenizer.nextToken();
		}
		while (!matchParentesis(token) || (token.trim().equals("") && tokenizer.hasMoreTokens()));
		if (tokenizer.useParentheses() && !token.startsWith("(") && !token.startsWith("IIF"))
		{
			return "(" + token.trim() + ")";
		}
		return token.trim();
	}

	private boolean matchParentesis(String token)
	{
		int cantLeft = 0, cantRight = 0;
		char [] cars = new char[token.length()];
		token.getChars(0, token.length(), cars, 0);
		for (int i = 0; i < cars.length; i++)
		{
			char c = cars[i];
			if (c == '(')
					{
				cantLeft++;
					}
			if (c == ')')
			{
				cantRight++;
			}
		}
		return cantLeft == cantRight;
	}

}

class FastTokenizer
{
	int pos;
	char[] s;
	String delimiters = "!+-/*><=()"+GE+LE+AND+OR+NE;
	String includeDelimiters = "(";
	boolean eof;
	private static final char GE = 0x01;
	private static final char LE = 0x02;
	private static final char AND = 0x03;
	private static final char OR = 0x04;
	private static final char NE = 0x05;

	public FastTokenizer(String str)
	{
		s = new char[str.length()];
		str.getChars(0, str.length(), s, 0);
	}
	public boolean eof()
	{
		return eof;
	}
	public String nextToken()
	{
		String tk = "";
		if (pos >= s.length)
			eof = true;
		while (pos < s.length)
		{
			char c = s[pos++];
			// Si encuentra un delimitador termina con el token
			if (delimiters.indexOf(c) != -1)
			{
				// Incluye el delimitador en el token si esta configurado
				if (includeDelimiters.indexOf(c) != -1)
					tk += c;

				// Retorna solo si encuentra un token
				if (!StringUtils.isEmpty(tk))
					return tk;
			}
			else
				tk += c;
		}
		return tk;
	}
}
class EvalValue
{
	String stringValue;
	BigDecimal decimalValue;
	public EvalValue(BigDecimal d)
	{
		decimalValue = d;
	}
	public EvalValue(String s)
	{
		stringValue = s;
	}
	public BigDecimal getDecimal()
	{
		return decimalValue;
	}
	public String getString()
	{
		return stringValue;
	}
	public static EvalValue add(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			return new EvalValue(a.getDecimal().add(b.getDecimal()));
		}
		else
			return new EvalValue(a.getString() + b.getString());
	}
	public static EvalValue subtract(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			return new EvalValue(a.getDecimal().subtract(b.getDecimal()));
		}
		else
			throw new IllegalArgumentException("Invalid operation: string - string");
	}
	public static EvalValue multiply(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			return new EvalValue(a.getDecimal().multiply(b.getDecimal()));
		}
		else
			throw new IllegalArgumentException("Invalid operation: string * string");
	}
	public static EvalValue divide(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
			return new EvalValue(a.getDecimal().divide(b.getDecimal(), MathContext.DECIMAL64));
		else
			throw new IllegalArgumentException("Invalid operation: string / string");
	}
	public static boolean greater(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
			return a.getDecimal().doubleValue()  > b.getDecimal().doubleValue() ;
			else
				throw new IllegalArgumentException("Invalid operation: string > string");
	}
	public static boolean less(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
			return a.getDecimal().doubleValue()  < b.getDecimal().doubleValue() ;
		else
			throw new IllegalArgumentException("Invalid operation: string < string");
	}
	public static boolean equal(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			if (b.getDecimal()==null)
				return false;
			else			
				return a.getDecimal().doubleValue()  == b.getDecimal().doubleValue() ;
		}
		else
			return a.getString().equals( b.getString());
	}
	public static boolean notEqual(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			if (b.getDecimal()==null)
				return true;
			else
				return a.getDecimal().doubleValue()  != b.getDecimal().doubleValue() ;
		}
		else
			return ! a.getString().equals(b.getString());
	}
	public static boolean greaterOrEqual(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
			return a.getDecimal().doubleValue()  >= b.getDecimal().doubleValue() ;
			else
				throw new IllegalArgumentException("Invalid operation: string >= string");
	}
	public static boolean lessOrEqual(EvalValue a, EvalValue b)
	{
		if (a.stringValue == null)
		{
			if (b.getDecimal()==null)
				return false;
			else			
				return a.getDecimal().doubleValue()  <= b.getDecimal().doubleValue() ;
		}
		else
			throw new IllegalArgumentException("Invalid operation: string <= string");
	}
	public boolean isTrue()
	{
		if (stringValue == null)
			return getDecimal().doubleValue()  != 0;
		else
			throw new IllegalArgumentException("Invalid operation: isTrue("+getString()+")");
	}
	public boolean isFalse()
	{
		if (stringValue == null)
			return getDecimal().doubleValue()  == 0;
		else
			throw new IllegalArgumentException("Invalid operation: isFalse("+getString()+")");
	}
	public static EvalValue falseValue()
	{
		return new EvalValue( new BigDecimal(0));
	}
	public static EvalValue trueValue()
	{
		return new EvalValue(new BigDecimal(1));
	}
}

class Tokenizer
{
	private StringTokenizer m_Tokenizer;
	private String m_PeekedToken;
	private boolean useParentheses;


	public Tokenizer(String str, String delim, boolean returnDelims, boolean useParentheses)
	{
		this(str, delim, returnDelims);
		this.useParentheses = useParentheses;
	}

	public Tokenizer(String str, String delim, boolean returnDelims)
	{
		m_Tokenizer = new StringTokenizer(str, delim, returnDelims);
		m_PeekedToken = null;
	}

	boolean hasMoreTokens()
	{
		if (m_PeekedToken != null)
			return true;

		return m_Tokenizer.hasMoreTokens();
	}

	public boolean peek(String[] nextToken)
	{
		if (m_PeekedToken != null)
		{
			nextToken[0] = m_PeekedToken.trim();
			return true;
		}
		
		if (!m_Tokenizer.hasMoreTokens())
			return false;
		
		// Advance the underlying tokenizer, but keep the returned token locally and return
		// it on next call to peek() or nextToken().
		m_PeekedToken = m_Tokenizer.nextToken();
		nextToken[0] = m_PeekedToken.trim();
		return true;
	}
	
	public String nextToken()
	{
		if (m_PeekedToken != null)
		{
			String next = m_PeekedToken;
			m_PeekedToken = null;
			return next;
		}
		
		return m_Tokenizer.nextToken();
	}

	public boolean useParentheses()
	{
		return useParentheses;
	}
}

