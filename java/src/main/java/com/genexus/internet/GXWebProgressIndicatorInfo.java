package com.genexus.internet;

import com.genexus.ModelContext;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.xml.XMLReader;
import com.genexus.xml.XMLWriter;


    public final class GXWebProgressIndicatorInfo extends GXXMLSerializable
    {        
    	private String action; /*  0: Show, 1:ShowWithTitle, 2: ShowWithTitleAndDesc, 3:Hide */
    	private String cssClass;
        private String title;
        private String description;
        private int maxValue;
        private int value;
        private byte type;
        
    	public GXWebProgressIndicatorInfo(int arg0, ModelContext arg1,
				String arg2) {
			super(arg0, arg1, arg2);
			// TODO Auto-generated constructor stub
		}
    	
        public void tojson( boolean includeState )
        {
			AddObjectProperty("Action", action);
            AddObjectProperty("Class", cssClass);
            AddObjectProperty("Title", title);
            AddObjectProperty("Description", description);
            AddObjectProperty("MaxValue", maxValue);
            AddObjectProperty("Value", value);
            AddObjectProperty("Type", type);
        }
        
        public void tojson( )
        {
           tojson( true) ;
        }

		public String getJsonMap(String value) {return null;}
		
		public void initialize() {
			// TODO Auto-generated method stub	
		}

		
		public short readxml(XMLReader arg0, String arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		
		public void writexml(XMLWriter arg0, String arg1, String arg2) {
			// TODO Auto-generated method stub
			
		}

		
		public void writexml(XMLWriter arg0, String arg1, String arg2,
				boolean arg3) {
			// TODO Auto-generated method stub
			
		}

		public String getCssClass() {
			return cssClass;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public int getMaxValue() {
			return maxValue;
		}

		public void setMaxValue(int maxValue) {
			this.maxValue = maxValue;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public byte getType() {
			return type;
		}

		public void setType(byte type) {
			this.type = type;
		}
    }

