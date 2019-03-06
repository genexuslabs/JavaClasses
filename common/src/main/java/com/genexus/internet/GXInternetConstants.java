// $Log: GXInternetConstants.java,v $
// Revision 1.4  2006/07/07 20:24:59  alevin
// - Agrego constante RECEIVED.
//
// Revision 1.3  2006/07/05 19:15:26  alevin
// - Agrego MAIL_LastNotSupported.
//
// Revision 1.2  2002/09/02 19:48:29  aaguiar
// - Se cambio el casing de content-type
//
// Revision 1.1.1.1  2000/11/22 16:28:44  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2000/11/22 16:28:44  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

public interface GXInternetConstants
{
	static boolean DEBUG = com.genexus.DebugFlag.DEBUG;

	static String CRLFString = System.getProperty("line.separator");

	static String BASE64 = "base64";
	static String QUOTED_PRINTABLE = "quoted-printable";

	static String DATE = "Date";
        static String RECEIVED = "Received";
	static String FROM = "From";
	static String TO = "To";
	static String CC = "Cc";
	static String BCC = "Bcc";
	static String SUBJECT = "Subject";
	static String PRIORITY = "X-Priority";
	static String ORGANIZATION = "Organization";
 	static String REPLY_TO = "Reply-To";
	
	static String CONTENT_TRANSFER_ENCODING 	= "Content-Transfer-Encoding";
	static String CONTENT_DISPOSITION			= "Content-Disposition";
	static String CONTENT_TYPE 			 		= "Content-type";

	static String BOUNDARY				    	= "boundary";
	static String TEXT 							= "text";
	static String FILENAME 						= "filename";
	static String NAME 							= "name";
	static String ATTACHMENT 					= "attachment";

	static String TYPE_MULTIPART 				= "multipart";
	static String SUBTYPE_ALTERNATIVE 			= "alternative";

	static int CR = 13;
	static int LF = 10;
	static byte[] CRLFByteArray = new byte[] { 13, 10};

	static int MAIL_Ok =  0;      				// No hubo error. (O/I/M)
	static int MAIL_AlreadyLogged =  1;      	// Ya hay una sesi�n abierta. (I/M)
	static int MAIL_NotLogged =  2;     		// No hay una sesi�n abierta. (I/M)
	static int MAIL_CantLogin =  3;      		// No se pudo iniciar una sesi�n. (I/M)
	static int MAIL_CantOpenOutlook =  4;     	// No se pudo abrir el Outlook. (O)
	static int MAIL_CantOpenFolder =  5;      	// No se pudo abrir la carpeta. (O/M)
	static int MAIL_InvalidSenderName =  6;   	// Nombre de emisor no v�lido. (I)
	static int MAIL_InvalidSenderAddress =  7;	// Direcci�n de emisor no v�lida. (I)
	static int MAIL_InvalidUser=  8;      		// Nombre de usuario no v�lido. (I)
	static int MAIL_InvalidPassword =  9;     	// Contrase�a no v�lida. (I)
	static int MAIL_MessageNotSent = 10;      	// No se pudo enviar mensaje. (O/I/M)
	static int MAIL_NoMessages = 11;      		// No hay mensajes para recibir. (O/I/M)
	static int MAIL_CantDeleteMessage = 12;   	// No se pudo eliminar mensaje. (I)
	static int MAIL_NoRecipient = 13;     		// No especific� destinatario principal del mensaje. (O/I/M)
	static int MAIL_InvalidRecipient = 14;    	// Destinatario no v�lido. (O/I/M)
	static int MAIL_InvalidAttachment = 15;   	// Attachment no v�lido. (O/I/M)
	static int MAIL_CantSaveAttachment = 16;  	// No se pudo guardar attachment. (O/I/M)
	static int MAIL_InvalidValue = 17;      	// Valor no v�lido. (O/I/M)
	static int MAIL_ConnectionLost = 19;      	// Se perdi� la conexi�n (I)
	static int MAIL_TimeoutExceeded = 20;     	// Se ha excedido el tiempo m�ximo de espera (timeout) (I)
	static int MAIL_ErrorReceivingMessage = 22;	// Error al recibir mensaje. (O/M)
	static int MAIL_NoAuthentication = 23;      // El servidor no reconoce ninguno de lo m�todos de autenticaci�n soportados (I)
	static int MAIL_AuthenticationError = 24;   // Error en la autenticaci�n
	static int MAIL_PasswordRefused = 25;      	// Usuario o clave rechazados.
        static int MAIL_LastNotSupported = 29;      	// El servidor no soporta el comando LAST
	
	static int MAIL_ServerReplyInvalid = 50;
	static int MAIL_ServerRepliedErr = 51;  

	static int MAIL_InvalidMode = 100;      	// Modo no v�lido.
}