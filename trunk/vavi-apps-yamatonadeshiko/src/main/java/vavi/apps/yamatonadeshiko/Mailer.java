/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Mailer. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050830 nsano initial version <br>
 */
public class Mailer {

    /** */
    private static Log log = LogFactory.getLog(Mailer.class);

    /** バッチモードの Transport */
    private Transport transport;
    /** バッチモードの session */
    private Session session;
    /** FROM: ヘッダ値 */
    private String from;

    /** タイトル、本文のエンコーディング */
    private static String encoding;
    /** メール送信用ホスト */
    private static String smtpHost;

    /** バッチモードを開始します。 */
    public void open(String from) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);

        this.from = from;

        this.session = Session.getDefaultInstance(props, null);
        session.getProperties().remove("mail.smtp.from");
        session.getProperties().put("mail.smtp.from", from);

        this.transport = session.getTransport("smtp");
        transport.connect();
    }

    /** メールを送ります。 */
    public void write(String to, String subject, String text)
        throws MessagingException {

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.setRecipients(Message.RecipientType.TO, to);
        mimeMessage.setSubject(subject, encoding);
        mimeMessage.setText(text, encoding);

        transport.sendMessage(mimeMessage, new Address[] { new InternetAddress(to) });
    }

    /** バッチモードを終了します。 */
    public void close() throws MessagingException {
        transport.close();
    }

    /** メール設定が書かれているプロパティファイル */
    private static final ResourceBundle rb = ResourceBundle.getBundle("vavi.apps.yamatonadeshiko.yamatonadeshiko", Locale.getDefault());

    /** */
    static {
        try {
            smtpHost = rb.getString("mailer.smtpHost");
            encoding = rb.getString("mailer.encoding");
        } catch (Exception e) {
            log.error(e);
        }
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        Mailer mailer = new Mailer();
        mailer.open("sano-n@klab.org");
        mailer.write("snaohide@docomo.ne.jp", "title", "message");
        mailer.close();
        System.err.println("done");
    }
}

/* */
