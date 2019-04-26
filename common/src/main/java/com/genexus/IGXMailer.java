package com.genexus;

public interface IGXMailer {
    int gxmlon(String fromName);
    int gxmsnd(String toName, String mySubject, String myText, int UI);
    int gxmsndb(String fromName, String to, String subject, String message, int window);
    int gxmsend(String to, String cc, String bcc, String subject, String message, String attachments, int dummy, int window, String fromName);
    int gxmloff();
    void gxmdspmsg (int value, int[] ret);

    void gxmdspmsg (int value);
    int gxmerror(int[] error);
    void gxmcount(int[] count, int[] ret);

    int gxmcount(int[] count);
    void gxmattachdir (String dir, int[] ret);

    int gxmattachdir (String dir);
    void gxmaddressformat(int format, int[] ret);
    int gxmaddressformat(int format);
    void gxmeditwindow (int val, int ret[]);

    int gxmeditwindow (int val);
    void gxmchangefolder(String folder, int newMessages, int markAsRead, int[] ret);

    int gxmchangefolder(String folder, int newMessages, int markAsRead);
    int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text);

    int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach);

    int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent);

    void gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent, java.util.Date[] received, int[] ret);

    int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent, java.util.Date[] received);
    void gxmlogout(int[] out);
    int gxmlogout();

    void gxmloginmapi(String profile, int newMessages, int[] ret);

    int gxmloginmapi(String profile, int newMessages);
    void gxmloginmapi(String profile, int newMessages, int markAsRead, int ret[]);

    int gxmloginmapi(String profile, int newMessages, int markAsRead);

    void gxmloginpop3(String host, String user, String password, int newMessages, int delete, int timeout, int[] ret);

    int gxmloginpop3(String host, String user, String password, int newMessages, int delete, int timeout);

    void gxmloginsmtp(String host, String name, String address, String user, String password, int timeout, int[] ret);

    int gxmloginsmtp(String host, String name, String address, String user, String password);

    int gxmloginsmtp(String host, String name, String address);


    int gxmloginsmtp(String host, String name, String address, String user, String password, int timeout);

    void gxmmode(String mode, int[] out);

    int gxmmode(String mode);




}
