package com.wrongweather.moipzy.global;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleAuthorizationConfig {

//    private static final String APPLICATION_NAME = "moipzy";
//    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//    @Bean
//    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
//
//        InputStreamReader clientSecretReader = new InputStreamReader(getClass().getResourceAsStream(CREDENTIALS_FILE_PATH));
//
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);
//
//        return new GoogleAuthorizationCodeFlow.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                JSON_FACTORY,
//                clientSecrets,
//                SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//    }
//
//    public Credential getCredentials(GoogleAuthorizationCodeFlow flow) throws IOException {
//        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8888).build())
//                .authorize("user");
//    }
//    @Bean
//    public Credential googleCredential(GoogleAuthorizationCodeFlow flow, String accessToken) throws IOException, GeneralSecurityException {
//
//        return new Credential.Builder(flow.getMethod())
//                .setTransport(flow.getTransport())
//                .setJsonFactory(flow.getJsonFactory())
//                .setClientAuthentication(flow.getClientAuthentication())
//                .build()
//                .setAccessToken(accessToken);
//    }

//    @Bean
//    public Calendar googleCalendar(Credential credential) throws IOException, GeneralSecurityException {
//        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }


}
