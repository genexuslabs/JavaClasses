package com.genexus.gxoffice;

public interface IWordDocument
{
	public short Open(String wName);

	public short Hide();

	public short Show();

	public short Close();

	public short Unbind();

  	public short PrintOut(short Preview);

  	public short PrintOut();

  	public short PrintOut(short Preview, short Background);

  	public void setText(String Text);
	
  	public String getText();

  	public short Append(String Text);

  	public short SpellCheck();

  	public short Save();

  	public short Replace(String oldVal, String newVal);

  	public short Replace(String oldVal, String newVal, short MatchCase);

  	public short Replace(String oldVal, String newVal, short MatchCase, short MatchWholeWord);
	
  	public short SaveAs(String Name);
  
  	public short SaveAs(String Name, String Type);

  	public short SaveAs(String Name, String Type, short DOSText);

  	public short SaveAs(String Name, String Type, short DOSText, short LineBreaks);
	
	public short RunMacro(String Name);
  	
  	public short RunMacro(String Name, Object[] Parms);

  	public short getErrCode();

    public String getErrDescription();

  	public void setErrDisplay(short _jcomparam_0);
        
	public short getErrDisplay();

  	public void setTemplate(String _jcomparam_0);
        
	public String getTemplate();

  	public void setReadOnly(short _jcomparam_0);
        
	public short getReadOnly();

  	public void cleanup();
}
