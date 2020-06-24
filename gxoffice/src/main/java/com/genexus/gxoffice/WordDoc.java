package com.genexus.gxoffice;

import java.io.File;

public class WordDoc {
	private IWordDocument document = null;

	private boolean closed = false;

	// Properties que se deben setear antes de abrir el documento

	public static boolean defaultUseMSOffice = true;

	private boolean useMSOffice = defaultUseMSOffice;
	private boolean chkNewMSOffice = false;

	public void setDefaultUseMSOffice(short useMSOffice) {
		defaultUseMSOffice = useMSOffice != 0 ? true : false;
		setUseMSOffice(useMSOffice);
	}

	public static short getDefaultUseMSOffice() {
		return (short) (defaultUseMSOffice ? 1 : 0);
	}

	public void setUseMSOffice(short useMSOffice) {
		chkNewMSOffice = true;
		this.useMSOffice = useMSOffice != 0 ? true : false;
	}

	public short getUseMSOffice() {
		return (short) (useMSOffice ? 1 : 0);
	}

	private void initDocument() {
		if (document == null) {
			if (useMSOffice) {
				document = new com.genexus.gxoffice.WordDocument();
			} else {
				try {
					Class c = Class.forName("com.sun.star.text.XTextDocument");
					document = new com.genexus.gxoffice.ooffice.WordDocument();
				} catch (Throwable e) {
					document = new com.genexus.gxoffice.WordDocument();
				}
			}
		}
	}

	public short Open(String wName) {
		if (com.genexus.ModelContext.getModelContext() != null) {
			com.genexus.internet.HttpContext webContext = (com.genexus.internet.HttpContext) com.genexus.ModelContext
					.getModelContext().getHttpContext();
			if ((webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb)) {
				if (!new File(wName).isAbsolute()) {
					wName = ((com.genexus.webpanels.HttpContextWeb) webContext).getRealPath(wName);
				}
			}
		}

		if ((document != null) && closed && chkNewMSOffice) {
			closed = false;
			document.cleanup();
			document = null;
		}
		initDocument();
		return document.Open(wName);
	}

	public short Hide() {
		if (document != null) {
			return document.Hide();
		} else {
			return -1;
		}
	}

	public short Show() {
		if (document != null) {
			return document.Show();
		} else {
			return -1;
		}
	}

	public short Close() {
		if (document != null) {
			short ret = document.Close();
			if (ret == 0) {
				closed = true;
			}
			return ret;
		}
		return 0;
	}

	public short Unbind() {
		if (document != null) {
			return document.Unbind();
		} else {
			return -1;
		}
	}

	public short PrintOut(short Preview) {
		if (document != null) {
			return document.PrintOut(Preview);
		} else {
			return -1;
		}
	}

	public short PrintOut() {
		if (document != null) {
			return document.PrintOut();
		} else {
			return -1;
		}
	}

	public short PrintOut(short Preview, short Background) {
		if (document != null) {
			return document.PrintOut(Preview, Background);
		} else {
			return -1;
		}
	}

	public void setText(String Text) {
		if (document != null) {
			document.setText(Text);
		}
	}

	public String getText() {
		if (document != null) {
			return document.getText();
		} else {
			return "";
		}
	}

	public short Append(String Text) {
		if (document != null) {
			return document.Append(Text);
		} else {
			return -1;
		}
	}

	public short SpellCheck() {
		if (document != null) {
			return document.SpellCheck();
		} else {
			return -1;
		}
	}

	public short Save() {
		if (document != null) {
			return document.Save();
		} else {
			return -1;
		}
	}

	public short Replace(String oldVal, String newVal) {
		if (document != null) {
			return document.Replace(oldVal, newVal);
		} else {
			return -1;
		}
	}

	public short Replace(String oldVal, String newVal, short MatchCase) {
		if (document != null) {
			return document.Replace(oldVal, newVal, MatchCase);
		} else {
			return -1;
		}
	}

	public short Replace(String oldVal, String newVal, short MatchCase, short MatchWholeWord) {
		if (document != null) {
			return document.Replace(oldVal, newVal, MatchCase, MatchWholeWord);
		} else {
			return -1;
		}
	}

	public short SaveAs(String Name) {
		if (document != null) {
			return document.SaveAs(Name);
		} else {
			return -1;
		}
	}

	public short SaveAs(String Name, String Type) {
		if (document != null) {
			return document.SaveAs(Name, Type);
		} else {
			return -1;
		}
	}

	public short SaveAs(String Name, String Type, short DOSText) {
		if (document != null) {
			return document.SaveAs(Name, Type, DOSText);
		} else {
			return -1;
		}
	}

	public short SaveAs(String Name, String Type, short DOSText, short LineBreaks) {
		if (document != null) {
			return document.SaveAs(Name, Type, DOSText, LineBreaks);
		} else {
			return -1;
		}
	}

	public short RunMacro(String Name) {
		if (document != null) {
			return document.RunMacro(Name);
		} else {
			return -1;
		}
	}

	public short RunMacro(String Name, Object[] Parms) {
		if (document != null) {
			return document.RunMacro(Name, Parms);
		} else {
			return -1;
		}
	}

	public short getErrCode() {
		if (document != null) {
			return document.getErrCode();
		} else {
			return -2;
		}
	}

	public String getErrDescription() {
		if (document != null) {
			return document.getErrDescription();
		} else {
			return "";
		}
	}

	private short errDisplay = 0;

	public void setErrDisplay(short _jcomparam_0) {
		errDisplay = _jcomparam_0;
		if (document != null) {
			document.setErrDisplay(errDisplay);
		}
	}

	public short getErrDisplay() {
		if (document != null) {
			return document.getErrDisplay();
		} else {
			return errDisplay;
		}
	}

	private String template = "";

	public void setTemplate(String _jcomparam_0) {

		File file = new File(_jcomparam_0);
		template = file.isAbsolute() ? _jcomparam_0 : file.getAbsolutePath();
		if (document == null) {
			initDocument();
		}
		document.setTemplate(template);
	}

	public String getTemplate() {
		if (document != null) {
			return document.getTemplate();
		} else {
			return template;
		}
	}

	private short readOnly = 0;

	public void setReadOnly(short _jcomparam_0) {
		readOnly = _jcomparam_0;
		if (document != null) {
			document.setReadOnly(readOnly);
		}
	}

	public short getReadOnly() {
		if (document != null) {
			return document.getReadOnly();
		} else {
			return readOnly;
		}
	}

	public void cleanup() {
		if (document != null) {
			document.cleanup();
		}
	}
}
