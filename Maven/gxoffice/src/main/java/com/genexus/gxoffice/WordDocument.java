package com.genexus.gxoffice;

public class WordDocument implements IWordDocument
{
  static
  {
        System.loadLibrary("gxoffice2");
  }

  public short Index = -1;

  public native short Open(String wName);

  public native short Hide();

  public native short Show();

  public native short Close();

  public native short Unbind();

  	public short PrintOut(short Preview)
  	{
		return PrintOut(Preview, (short) 0);
  	}

  	public short PrintOut()
  	{
		return PrintOut((short) 0, (short) 0);
  	}

  	public native short PrintOut(short Preview, short Background);

  	public native void setText(String Text);
  	public native String getText();

  	public native short Append(String Text);

  	public native short SpellCheck();

  	public native short Save();

  	public short Replace(String oldVal, String newVal)
	{
		return Replace(oldVal, newVal, (short) 0);
	}

  	public short Replace(String oldVal, String newVal, short MatchCase)
	{
		return Replace(oldVal, newVal, MatchCase, (short) 0);
	}

  	public native short Replace(String oldVal, String newVal, short MatchCase, short MatchWholeWord);
	
  	public short SaveAs(String Name)
  	{
		return SaveAs(Name, "DOC");
  	}
  
  	public short SaveAs(String Name, String Type)
  	{
		return SaveAs(Name, Type, (short) 0);
  	}

  	public short SaveAs(String Name, String Type, short DOSText)
  	{
		return SaveAs(Name, Type, DOSText, (short) 0);
  	}

  	public native short SaveAs(String Name, String Type, short DOSText, short LineBreaks);

  	public native short getErrCode();

        public native String getErrDescription();

  	public native void setErrDisplay(short _jcomparam_0);
        public native short getErrDisplay();

  	public native void setTemplate(String _jcomparam_0);
        public native String getTemplate();

  	public native void setReadOnly(short _jcomparam_0);
        public native short getReadOnly();
		
	public short RunMacro(String Name)
	{
		return -1;
	}
  	
  	public short RunMacro(String Name, Object[] Parms)
	{
		return -1;
	}

  	public native void cleanup();
}
