package com.hackonauts.hackonauts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hackonauts.hackonauts.dto.ServiceRequest;
import com.hackonauts.hackonauts.repository.ServiceRequestRepository;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailProcessingService {

    @Autowired
    OllamaCategorizationService ollamaCategorizationService;

    @Autowired
    ServiceRequestRepository serviceRequestRepository;

    private final RestTemplate restTemplate;

    public EmailProcessingService() {
        this.restTemplate = new RestTemplate();
    }


    @Autowired
    private JavaMailSender mailSender;

    public void fetchEmails() {
        try {
            Properties properties = new Properties();
            properties.put("mail.pop3.host", "127.0.0.1");
            properties.put("mail.pop3.port", "110");
            properties.put("mail.pop3.auth", "true");

            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("pop3");
            store.connect("127.0.0.1", "aihackonauts", "hackpwd");

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            System.out.println("Total Messages in InBox: " + messages.length);
            for (Message message : messages) {
                System.out.println("---------------------------------");
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("---------------------------------");
                StringBuilder contentBuilder = new StringBuilder();
                System.out.println("Content: " + message.getContent().toString());
                Object content = message.getContent();
                processContent(contentBuilder, message);
                System.out.println("---------------------------------");

                ServiceRequest oServiceRequest = processServiceRequest(message.getSubject(), contentBuilder.toString());
                saveServiceRequest(message, oServiceRequest, contentBuilder );
                message.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processContent(StringBuilder contentBuilder, Message message) throws Exception {
        Tika tika = new Tika();
        if (message.isMimeType("text/plain")) {
            // Extract plain text content
            String plainText = (String) message.getContent();
            System.out.println("Body (Plain Text): " + plainText);
            contentBuilder.append(plainText);

        } else if (message.isMimeType("text/html")) {
            // Extract HTML content
            String htmlContent = (String) message.getContent();
            System.out.println("Body (HTML): " + htmlContent);
            contentBuilder.append(tika.parseToString(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8))));

        } else if (message.isMimeType("multipart/*")) {
            // Process email attachments and multipart content
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            processMultipart(mimeMultipart, tika, contentBuilder);
        }

    }

    private static void processMultipart(MimeMultipart mimeMultipart, Tika tika, StringBuilder contentBuilder) throws Exception {
        TesseractOCRConfig ocrConfig = new TesseractOCRConfig(); // Tesseract OCR config
        ocrConfig.setLanguage("eng");
        ParseContext parseContext = new ParseContext();
        parseContext.set(TesseractOCRConfig.class, ocrConfig);

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/plain")) {
                // Plain text content
                System.out.println("Body (Plain Text): " + bodyPart.getContent());
                contentBuilder.append(bodyPart.getContent());

            } else if (bodyPart.isMimeType("text/html")) {
                // HTML content
                System.out.println("Body (HTML): " + bodyPart.getContent());
                contentBuilder.append(new ByteArrayInputStream( bodyPart.getContent().toString().getBytes(StandardCharsets.UTF_8)));

            } else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                // Process attachments
                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                if(bodyPart.isMimeType("image/*")) {
                    System.out.println("Attachment: " + mimeBodyPart.getFileName());
                    InputStream inputStream = mimeBodyPart.getInputStream();

                    // Use Tika and Tesseract OCR to extract text from the image
                    Metadata metadata = new Metadata();
                    BodyContentHandler handler = new BodyContentHandler();
                    TesseractOCRParser ocrParser = new TesseractOCRParser();
                    ocrParser.parse(inputStream, handler, metadata, parseContext);

                    // Print extracted text
                    System.out.println("Extracted Text from Image:");
                    System.out.println(handler.toString());
                    contentBuilder.append(handler.toString());

                } else {
                    InputStream inputStream = mimeBodyPart.getInputStream();

                    // Use Tika to extract text from attachments
                    Metadata metadata = new Metadata();
                    String extractedText = tika.parseToString(inputStream, metadata);
                    System.out.println("Attachment Content: " + extractedText);
                    contentBuilder.append(extractedText);
                    // Print metadata if needed
                    for (String name : metadata.names()) {
                        System.out.println(name + ": " + metadata.get(name));
                    }
                }
            }
        }
    }


    private void saveServiceRequest(Message message, ServiceRequest oServiceRequest, StringBuilder contentBuilder) throws MessagingException, IOException {
        System.out.println("---------------------------------saveServiceRequest");
        com.hackonauts.hackonauts.entity.ServiceRequest serviceRequest = new com.hackonauts.hackonauts.entity.ServiceRequest();
        serviceRequest.setFrom(String.valueOf(message.getFrom()[0]));
        serviceRequest.setSubject(message.getSubject());
        serviceRequest.setContent(contentBuilder.toString().getBytes(StandardCharsets.UTF_8));
        serviceRequest.setRequestType(oServiceRequest.getRequestType());
        serviceRequest.setSubType(oServiceRequest.getSubType());
        serviceRequest.setMessage(oServiceRequest.getDescription());
        serviceRequest.setInserted(new Timestamp(new Date().getTime()));
        serviceRequestRepository.save(serviceRequest);
        System.out.println("---------------------------------Save Complete");
    }

    private ServiceRequest processServiceRequest(String subject, String string) throws JsonProcessingException {
        System.out.println("---------------------------------processServiceRequest");
        return ollamaCategorizationService.analyzeEmailWithOllama(subject+"-"+ string);
    }


}