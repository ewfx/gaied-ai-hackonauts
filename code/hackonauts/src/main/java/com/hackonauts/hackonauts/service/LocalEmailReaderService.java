package com.hackonauts.hackonauts.service;

import com.hackonauts.hackonauts.dto.Email;
import jakarta.mail.internet.MimeMessage;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mail.RFC822Parser;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jakarta.mail.*;



@Service
public class LocalEmailReaderService {

    public List<Email> readEmailsFromLocalFolder(String folderPath) throws Exception {
        File folder = new File(folderPath);
        File[] emailFiles = folder.listFiles((dir, name) -> name.endsWith(".eml"));

        if (emailFiles == null || emailFiles.length == 0) {
            throw new Exception("No email files found in the specified folder.");
        }

        List<Email> emails = new ArrayList<>();
        Tika tika = new Tika();
        RFC822Parser parser = new RFC822Parser();
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        for (File emailFile : emailFiles) {
            try (FileInputStream inputStream = new FileInputStream(emailFile)) {
                MimeMessage message = new MimeMessage(session, inputStream);


                // Create and add an email object
                Email email = new Email();
                email.setSubject(message.getSubject());
                email.setSender(message.getFrom()[0].toString());
                Object content = message.getContent();
                if (content instanceof String) {
                    System.out.println("Content: " + content);
                    email.setContent(message.getContent().toString());
                } else if (content instanceof Multipart) {
                    List<String>    files = new ArrayList<String>();;
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            System.out.println("Attachment: " + part.getFileName());
                            files.add(part.getFileName());
                        } else {
                            System.out.println("Body: " + part.getContent());
                            email.setContent(part.getContent().toString());
                        }
                    }
                    email.setAttachments(files);
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (bodyPart.isMimeType("text/plain")) {
                            System.out.println("Plain Text: " + bodyPart.getContent());
                        } else if (bodyPart.isMimeType("text/html")) {
                            System.out.println("HTML: " + bodyPart.getContent());
                        } else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            System.out.println("Attachment: " + bodyPart.getFileName());
                        } else {
                            System.out.println("Other Content: " + bodyPart.getContent());
                        }
                    }
                } else {
                    System.out.println("Unknown content type: " + content.getClass().getName());
                }



                emails.add(email);
            }
        }

        return emails;
    }

}
