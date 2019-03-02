// $Log: GXMailMessage.java,v $
// Revision 1.1  2001/12/28 20:45:20  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/12/28 20:45:20  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.util.Date;
import java.util.Hashtable;

import com.genexus.CommonUtil;

public class GXMailMessage 
{
	// El from solo tiene sentido cuando se recibe. Al enviar el que importa el sender
	private MailRecipient from;

	private MailRecipientCollection to ;
	private MailRecipientCollection cc ;
	private MailRecipientCollection bcc;
	private MailRecipientCollection replyto;
	private Date dateReceived;
	private Date dateSent;

	private String subject;
	private String text;
	private String htmlText;
	private StringCollection attachments;
	private Hashtable headers;
	
	public GXMailMessage()
	{
		clear();
	}

	public void clear()
	{
		from = new MailRecipient();
		to = new MailRecipientCollection();
		cc = new MailRecipientCollection();
		bcc = new MailRecipientCollection();
		replyto = new MailRecipientCollection();

		dateReceived = CommonUtil.nullDate();
		dateSent = CommonUtil.nullDate();

		subject = "";
		text = "";
		htmlText = "";
		attachments = new StringCollection();
		headers = new Hashtable();
	}

	void setFrom(MailRecipient from)
	{
		this.from = from;
	}

	public MailRecipient getFrom()
	{
		return from;
	}

    public MailRecipientCollection getTo()
	{
		return to;
	}

    void setTo(MailRecipientCollection to)
	{
		this.to = to;
	}

    public MailRecipientCollection getCc()
	{
		return cc;
	}

    void setCc(MailRecipientCollection cc)
	{
		this.cc = cc;
	}


    void setBcc(MailRecipientCollection bcc)
	{
		this.bcc = bcc;
	}

    public MailRecipientCollection getBcc()
	{
		return bcc;
	}

	public MailRecipientCollection getReplyto()
	{
		return replyto;
	}

	void setReplyto(MailRecipientCollection replyto)
	{
		this.replyto = replyto;
	}

	public Date getDateReceived()
	{
		return dateReceived;
	}

    public void setDateReceived(Date dateReceived)
	{
		this.dateReceived = dateReceived;
	}

    public Date getDateSent()
	{
		return dateSent;
	}

    public void setDateSent(Date dateSent)
	{
		this.dateSent = dateSent;
	}


    public void setSubject( String subject)
	{
		this.subject = JapaneseMimeDecoder.decode(subject);
	}

    public String getSubject()
	{
		return subject;
	}

    public void setText( String text)
	{
		this.text = text;
	}

    public String getText()
	{
		return text;
	}

    public StringCollection getAttachments()
	{
		return attachments;
	}

    void setAttachments(StringCollection attachments)
	{
		this.attachments = attachments;
	}

    public void setHtmltext(String htmlText)
	{
		this.htmlText = htmlText;		
	}

    public String getHtmltext()
	{
		return htmlText;		
	}
    
    public void addHeader(String name, String value)
    {
    	headers.remove(name);    	
    	headers.put(name, value);    	    		
    }
    
    public String getHeader(String name)
    {    	
    	return (String) ((headers.containsKey(name.toUpperCase()))?headers.get(name.toUpperCase()):"");
    }

	public void setHeaders(Hashtable keys) {
		headers = keys;		
	}

	public Hashtable getHeaders() {
		return headers;
	}
}

