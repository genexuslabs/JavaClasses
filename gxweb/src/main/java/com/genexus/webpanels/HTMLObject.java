package com.genexus.webpanels;

import com.genexus.GXBaseCollection;
import com.genexus.GXutil;
import com.genexus.SdtMessages_Message;
import com.genexus.internet.IGxJSONAble;
import com.genexus.internet.IGxJSONSerializable;

import json.org.json.IJsonFormattable;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class HTMLObject implements IGxJSONAble, IGxJSONSerializable
{
	protected int backStyle;
	protected boolean fontSizeSet = true;
	protected boolean fontColorSet;
	protected boolean backColorSet;
	protected int fontBold;
	protected int fontItalic;
	protected int fontUnderline;
	protected int fontStrikethru;
	protected int fontSize = 8;
	protected String fontName = "Arial";
	protected int isPassword;
	protected String name;
	protected int visible = 1;
	protected int backGround;
	protected int foreGround;
	protected String tag;
	protected String link = "";
        protected String tooltip = "";
	protected String webTags;
	protected int top;
	protected int left;
	protected long width;
	protected long height;
	protected int enabled = 1;
	protected StringBuffer style = null;
	protected static int[] szToPt = { 0, 8, 10, 12, 14, 18, 24, 36 };
	protected String themeClass = "";
	protected String columnClass = "";
	protected String columnHeaderClass = "";
	protected JSONObject jsonObj = new JSONObject();
	
	public HTMLObject(GXWebPanel panel)
	{
	}

	public HTMLObject()
	{
	}

	public String getThemeClass()
	{
		return themeClass;
	}

	public void setThemeClass(String themeClass)
	{
		this.themeClass = themeClass;
	}

	public String getColumnClass()
	{
		return columnClass;
	}

	public void setColumnClass(String themeClass)
	{
		columnClass = themeClass;
	}

	public String getColumnHeaderClass()
	{
		return columnHeaderClass;
	}

	public void setColumnHeaderClass(String themeClass)
	{
		columnHeaderClass = themeClass;
	}

	public static int getSz( int i)
	{
		int size = 8;

/*		for(int k = 7; k > 0; k--)
		{
			if ( i >= szToPt[k] )
			{	
				size = k;
				break;
			}
		}
*/
		
		for(int k = 0; k < 8; k++)
		{
			if (i <= szToPt[k])
			{
				size = k;
				break;
			}
		}

		return size;
	}

	public static int roundPt( int i)
	{
/*		int size = 8;

		for(int k = 0; k < 8; k++)
		{
			if (i <= szToPt[k])
			{
				size = szToPt[k];
				break;
			}
		}

		return size;*/
		return i;
	}

	public void setFocus()
	{
	}

        public void setTooltip(String tooltip)
        {
            this.tooltip = tooltip;
        }

        public String getTooltip()
        {
            return this.tooltip;
        }

	public void setWebtags(String webTags)
	{
		this.webTags = webTags;
	}

	public String getWebtags()
	{
		return webTags;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getLinkTarget()
	{
		return linkTarget;
	}

	protected String linkTarget;
	public void setLinkTarget(String linkTarget)
	{
		this.linkTarget = linkTarget;
	}


	public String getTag()
	{
		return this.tag;
	}

	public void setTop(int top)
	{
		this.top = top;
	}

	public int getTop()
	{
		return this.top;
	}

	public void setHeight(long height)
	{
		this.height = height;
	}

	public long getHeight()
	{
		return this.height;
	}

	public void setLeft(int left)
	{
		this.left = left;
	}

	public int getLeft()
	{
		return this.left;
	}

	public void setWidth(long width)
	{
		this.width = width;
	}

	public long getWidth()
	{
		return this.width;
	}

	public void setEnabled(int enabled)
	{
		this.enabled = enabled;
	}

	public int getEnabled()
	{
		return this.enabled;
	}

	public String getLink()
	{
		return link;
	}
	
	public void setLink(String link)
	{
		this.link = link;
	}

	public int getIBackground()
	{
		return backGround;
	}
	
	public int getIForeground()
	{
		return foreGround;
	}

	public void setBackstyle(int style)
	{
		this.backStyle = style;
	}

	public byte getBackstyle()
	{
		return (byte) backStyle;
	}


	public void setIForeground(int foreGround)
	{
		this.foreGround = foreGround;
		fontColorSet = true;
	}
	
	public void setIBackground(int backGround)
	{
		this.backGround = backGround;
		backColorSet = true;
	}

	public byte getFontBold()
	{
		return (byte) fontBold;
	}
	
	public void setFontBold(int fontBold)
	{
		this.fontBold = fontBold;
	}

	public byte getFontItalic()
	{
		return (byte) fontItalic;
	}
	
	public void setFontItalic(int fontItalic)
	{
		this.fontItalic = fontItalic;
	}

	public byte getFontUnderline()
	{
		return (byte) fontUnderline;
	}
	
	public void setFontUnderline(int fontUnderline)
	{
		this.fontUnderline = fontUnderline;
	}

	public void setFontStrikethru(int fontStrikethru)
	{
		this.fontStrikethru = fontStrikethru;
	}

	public byte getFontStrikethru()
	{
		return (byte) fontStrikethru;
	}

	public String getFontName()
	{
		return fontName;
	}
	
	public void setFontName(String fontName)
	{
		this.fontName = fontName ;
	}

	public byte getFontSize()
	{
		return (byte) fontSize;
	}
	
	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
		fontSizeSet = true;
	}

	public byte getIsPassword()
	{
		return (byte) isPassword;
	}
	
	public void setIsPassword(int isPassword)
	{
		this.isPassword = isPassword;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String controlName)
	{
		this.name = controlName.toUpperCase();
	}

	public void setVisible(int visible)
	{
		this.visible = visible ;
	}
	
	public byte getVisible()
	{
		return (byte) visible;
	}

	private int titleBackStyle;
	public int getTitleBackStyle()
	{
		return titleBackStyle;
	}
	public void setTitleBackStyle(int value)
	{
		this.titleBackStyle= value;
	}

	private int titleForeColor;
	public int getTitleForeColor()
	{
		return titleForeColor;
	}
	public void setTitleForeColor(int value)
	{
		this.titleForeColor = value;
	}

	private int titleBackColor;
	public int getTitleBackColor()
	{
		return titleBackColor;
	}

	public void setTitleBackColor(int value)
	{
		this.titleBackColor = value;

	}

	private int titleFontBold;
	public int getTitleFontBold()
	{
		return titleFontBold;
	}

	public void setTitleFontBold(int value)
	{
		this.titleFontBold = value;

	}

	private int titleFontItalic;
	public int getTitleFontItalic()
	{
		return titleFontItalic;
	}

	public void setTitleFontItalic(int value)
	{
		this.titleFontItalic = value;

	}

	private int titleFontUnderline;
	public int getTitleFontUnderline()
	{
		return titleFontUnderline;
	}

	public void setTitleFontUnderline(int value)
	{
		this.titleFontUnderline = value;
	}

	private int titleFontStrikethru;
	public int getTitleFontStrikethru()
	{
		return titleFontStrikethru;
	}

	public void setTitleFontStrikethru(int value)
	{
		this.titleFontStrikethru = value;
	}

	private int titleFontSize;
	public int getTitleFontSize()
	{
		return titleFontSize;
	}

	public void setTitleFontSize(int value)
	{
		this.titleFontSize = value;
	}

	private String titleFontName;
	public String getTitleFontName()
	{
		return titleFontName;
	}

	public void setTitleFontName(String value)
	{
		this.titleFontName = value;
	}

	private String title;
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String value)
	{
		this.title = value;
	}

	int titleformat;
	public int getTitleFormat()
	{
		return titleformat;
	}

	public void setTitleFormat(int titleformat)
	{
		this.titleformat = titleformat;
	}


	String jsOnClick = "";
	public String getJsonclick()
	{
		return jsOnClick;
	}

	public void setJsonclick(String onclick)
	{
		this.jsOnClick = onclick;
	}  

	String JSEvent = "";
	public String getJsevent()
	{
		return JSEvent ;
	}

	public void setJsevent(String onclick)
	{
		this.JSEvent = onclick;
	}  



	String internalName = "";
	public String getInternalname()
	{
		return internalName;
	}

	public void setInternalname(String internalName)
	{
		this.internalName = internalName;
	}

	public void tojson()
	{
	}

	public String ToJavascriptSource()
	{
		return GetJSONObject().toString();
	}

	public void AddObjectProperty(String name, Object prop)
	{
		try
		{
			jsonObj.put(name, prop);
		}
		catch (JSONException e) { }
	}

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }

	public Object GetJSONObject()
	{
		tojson();
		return jsonObj;
	}

	public void FromJSONObject(IJsonFormattable obj)
	{
	}

        public String toJSonString()
	{
		return ToJavascriptSource();
	}
	public boolean fromJSonString(String s)
	{
		return fromJSonString(s, null);
	}
	public boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages)
	{
		try
		{
			jsonObj = new JSONObject(s);
			FromJSONObject(jsonObj);
			return true;
		}
		catch (JSONException ex)
		{
			GXutil.ErrorToMessages("fromjson error", ex.getMessage(), messages);
			return false;
		}
	}
}
