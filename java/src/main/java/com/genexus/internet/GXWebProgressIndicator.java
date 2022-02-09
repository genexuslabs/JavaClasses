package com.genexus.internet;

import com.genexus.ModelContext;

public class GXWebProgressIndicator
    {        
		private static String ID = "GX_PROGRESS_BAR";
        private GXWebNotification notification;        
        private GXWebProgressIndicatorInfo info;       
		private ModelContext context;
		
        public GXWebProgressIndicator(ModelContext gxContext)
        {
			context = gxContext;
            notification = new GXWebNotification(gxContext);            
            info = new GXWebProgressIndicatorInfo(0,gxContext,"");            
        }
      
        public void show()
        {
            setAction("0");
            updateProgress();
        }

        private void updateProgress()
        {
			GXWebNotificationInfo notif = new GXWebNotificationInfo(0,context,"");
			notif.setId(GXWebProgressIndicator.ID);
			notif.setGroupName(GXWebProgressIndicator.ID);
			notif.setMessage(info);
			notification.notify(notif);
        }
        
        public void showWithTitle(String title)
        {
            setTitle(title);
            setAction("1");
            updateProgress();
        }

        public void showWithTitleAndDescription(String title, String desc)
        {
            setTitle(title);
            setDescription(desc);
            setAction("2");
            updateProgress();            
        }

        public void hide()
        {			
            setAction("3");
            updateProgress();
        }

		private String getAction() {
			return info.getAction();
		}

		private void setAction(String action) {
			info.setAction(action);
		}
		
        private String getProgressMessage()
        {
            return info.toJSonString();
        }
        
        public String getCssClass() {
			return info.getCssClass();
		}

		public void setCssClass(String cssClass) {
			info.setCssClass(cssClass);
			updateProgress(); 
		}

		public String getTitle() {
			return info.getTitle();
		}

		public void setTitle(String title) {
			info.setTitle(title);
			updateProgress(); 
		}
			

		public String getDescription() {
			return info.getDescription();
		}

		public void setDescription(String description) {
			info.setDescription(description);
			updateProgress(); 
		}

		public int getMaxValue() {
			return info.getMaxValue();
		}

		public void setMaxValue(int maxValue) {
			info.setMaxValue(maxValue);
		}

		public int getValue() {
			return info.getValue();
		}

		public void setValue(int value) {
			info.setValue(value);
			updateProgress(); 
		}

		public byte getType() {
			return info.getType();
		}

		public void setType(byte type) {
			info.setType(type);
		}
        

    }

