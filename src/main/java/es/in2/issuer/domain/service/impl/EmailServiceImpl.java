package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;

import static es.in2.issuer.domain.util.Constants.FROM_EMAIL;
import static es.in2.issuer.domain.util.Constants.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public Mono<Void> sendPin(String to, String subject, String pin) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("pin", pin);
            String htmlContent = templateEngine.process("pin-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendTransactionCodeForCredentialOffer(String to, String subject, String link, String knowledgebaseUrl, String user, String organization) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            try {
                ClassPathResource imgResource = new ClassPathResource("static/img/qr-wallet.png");
                String imageResourceName = imgResource.getFilename();

                log.info("Attempting to load image: {}", imageResourceName);
                byte[] imageBytes = Files.readAllBytes(imgResource.getFile().toPath());
                log.info("Successfully loaded image: {}", imageResourceName);

                String imageContentType = Files.probeContentType(imgResource.getFile().toPath());
                if (imageContentType == null) {
                    imageContentType = MimeTypeUtils.IMAGE_PNG_VALUE;
                    log.warn("Could not determine content type for image, using default: {}", imageContentType);
                }

                Context context = new Context();

                context.setVariable("link", link);
                context.setVariable("user", user);
                context.setVariable("organization", organization);
                context.setVariable("knowledgebaseUrl", knowledgebaseUrl);
                context.setVariable("imageResourceName", imageResourceName);

                final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
                if (imageResourceName != null) {
                    log.info("Adding inline image to email with name: {}", imageResourceName);
                    helper.addInline(imageResourceName, imageSource, imageContentType);
                }

                String htmlContent = templateEngine.process("activate-credential-email", context);
                helper.setText(htmlContent, true);

                javaMailSender.send(mimeMessage);
            } catch (IOException e) {
                log.error("Error loading image or processing email: {}", e.getMessage(), e);
                throw new RuntimeException("Error processing email", e);
            }

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendPendingCredentialNotification(String to, String subject) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            String htmlContent = templateEngine.process("credential-pending-notification", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendCredentialSignedNotification(String to, String subject, String firstName) {
        firstName = firstName.replace("\"", "");
        final String finalName = firstName;

        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("name", finalName);
            String htmlContent = templateEngine.process("credential-signed-notification", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
