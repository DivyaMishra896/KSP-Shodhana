package com.ksp.shodhana.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksp.shodhana.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory local data store that loads seed data from JSON resources on startup.
 * Acts as a robust fallback/mock database for local development and offline demos.
 */
@Component
public class LocalDataStore {

    private static final Logger log = LoggerFactory.getLogger(LocalDataStore.class);
    private final ObjectMapper objectMapper;

    private List<Crime> crimes = new ArrayList<>();
    private List<Criminal> criminals = new ArrayList<>();
    private List<CrimeCriminalLink> links = new ArrayList<>();
    private List<CriminalNetwork> network = new ArrayList<>();
    private List<TimelineEvent> timelineEvents = new ArrayList<>();
    private List<Investigation> investigations = new ArrayList<>();
    private List<FinancialTransaction> financialTransactions = new ArrayList<>();

    public LocalDataStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing LocalDataStore with seed data from classpath...");
        try {
            this.crimes = loadData("seed-data/crimes.json", new TypeReference<List<Crime>>() {});
            // Assign sequential row IDs and area types to crimes
            for (int i = 0; i < crimes.size(); i++) {
                Crime c = crimes.get(i);
                c.setRowId((long) (i + 1));
                if (c.getAreaType() == null) {
                    String d = c.getDistrict() != null ? c.getDistrict().toLowerCase() : "";
                    if (d.contains("bengaluru") || d.contains("mysuru")) {
                        c.setAreaType("urban");
                    } else if (d.contains("hubballi") || d.contains("tumakuru")) {
                        c.setAreaType("semi-urban");
                    } else {
                        c.setAreaType("rural");
                    }
                }
            }
            log.info("Loaded {} crimes", crimes.size());

            this.criminals = loadData("seed-data/criminals.json", new TypeReference<List<Criminal>>() {});
            for (int i = 0; i < criminals.size(); i++) {
                Criminal cr = criminals.get(i);
                if (cr.getAreaType() == null) {
                    String d = cr.getDistrict() != null ? cr.getDistrict().toLowerCase() : "";
                    if (d.contains("bengaluru") || d.contains("mysuru")) {
                        cr.setAreaType("urban");
                    } else if (d.contains("hubballi") || d.contains("tumakuru")) {
                        cr.setAreaType("semi-urban");
                    } else {
                        cr.setAreaType("rural");
                    }
                }
            }
            log.info("Loaded {} criminals", criminals.size());

            this.links = loadData("seed-data/crime_criminal_links.json", new TypeReference<List<CrimeCriminalLink>>() {});
            for (int i = 0; i < links.size(); i++) {
                links.get(i).setRowId((long) (i + 1));
            }
            log.info("Loaded {} crime-criminal links", links.size());

            this.network = loadData("seed-data/criminal_network.json", new TypeReference<List<CriminalNetwork>>() {});
            for (int i = 0; i < network.size(); i++) {
                network.get(i).setRowId((long) (i + 1));
            }
            log.info("Loaded {} criminal network edges", network.size());

            this.timelineEvents = loadData("seed-data/timeline_events.json", new TypeReference<List<TimelineEvent>>() {});
            for (int i = 0; i < timelineEvents.size(); i++) {
                timelineEvents.get(i).setRowId((long) (i + 1));
            }
            log.info("Loaded {} timeline events", timelineEvents.size());

            investigations.add(Investigation.builder()
                    .rowId(1L)
                    .crimeRowId(1L)
                    .title("MG Road Metro Chain Snatching Investigation")
                    .status("Active")
                    .leadOfficer("Inspector Ramesh K")
                    .startedDate("2026-06-15")
                    .notes("CCTV footage recovered, track suspect vehicle registration.")
                    .build());

            investigations.add(Investigation.builder()
                    .rowId(2L)
                    .crimeRowId(3L)
                    .title("Mysuru Jewelry Store Armed Robbery Investigation")
                    .status("Active")
                    .leadOfficer("Inspector Venkatesh N")
                    .startedDate("2026-06-20")
                    .notes("Fingerprint match found for Anil D'Souza. Track getaway van.")
                    .build());

            // Initialize synthetic financial transactions (Feature 3: Financial Crime Analysis)
            initFinancialTransactions();

            log.info("Initialized {} investigations and {} financial transactions", investigations.size(), financialTransactions.size());

        } catch (Exception e) {
            log.error("Failed to load seed data in LocalDataStore: {}", e.getMessage(), e);
        }
    }

    private <T> List<T> loadData(String path, TypeReference<List<T>> typeReference) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.warn("Seed file not found at path: {}", path);
                return new ArrayList<>();
            }
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, typeReference);
            }
        } catch (Exception e) {
            log.error("Error reading JSON from path: {}", path, e);
            return new ArrayList<>();
        }
    }

    private void initFinancialTransactions() {
        // 18 Synthetic financial transactions linking to criminals (rowId 1-16), 4 rule-flagged
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(1L).transactionId("TXN-2026-9001").linkedCriminalId(1L).amount(2500000.0)
                .fromAccount("ACC-4491-9921").toAccount("SHELL-OFFSHORE-881").bankName("State Bank of India")
                .timestamp("2026-06-12 14:22:00").isFlagged(true)
                .flagReason("FLAGGED: Single wire transfer > ₹1,000,000 to unverified offshore shell account")
                .build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(2L).transactionId("TXN-2026-9002").linkedCriminalId(1L).amount(45000.0)
                .fromAccount("ACC-4491-9921").toAccount("ACC-1102-3391").bankName("Canara Bank")
                .timestamp("2026-06-14 09:15:00").isFlagged(false).flagReason(null).build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(3L).transactionId("TXN-2026-9003").linkedCriminalId(2L).amount(1800000.0)
                .fromAccount("ACC-9921-1102").toAccount("ACC-7741-2291").bankName("HDFC Bank")
                .timestamp("2026-06-18 16:45:00").isFlagged(true)
                .flagReason("FLAGGED: Multiple transfers exceeding 10x reported income bracket within 48 hours")
                .build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(4L).transactionId("TXN-2026-9004").linkedCriminalId(2L).amount(25000.0)
                .fromAccount("ACC-9921-1102").toAccount("ACC-5512-4411").bankName("ICICI Bank")
                .timestamp("2026-06-19 11:30:00").isFlagged(false).flagReason(null).build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(5L).transactionId("TXN-2026-9005").linkedCriminalId(3L).amount(4200000.0)
                .fromAccount("ACC-3310-8812").toAccount("CRYPTO-MIXER-901").bankName("Axis Bank")
                .timestamp("2026-06-21 21:05:00").isFlagged(true)
                .flagReason("FLAGGED: Rapid structuring transfer to known crypto tumbler address")
                .build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(6L).transactionId("TXN-2026-9006").linkedCriminalId(3L).amount(12000.0)
                .fromAccount("ACC-3310-8812").toAccount("ACC-8812-4401").bankName("State Bank of India")
                .timestamp("2026-06-22 10:10:00").isFlagged(false).flagReason(null).build());
        financialTransactions.add(FinancialTransaction.builder()
                .rowId(7L).transactionId("TXN-2026-9007").linkedCriminalId(4L).amount(3500000.0)
                .fromAccount("ACC-7711-2290").toAccount("HAWALA-DESK-402").bankName("Karnataka Bank")
                .timestamp("2026-06-24 18:30:00").isFlagged(true)
                .flagReason("FLAGGED: Cross-border informal hawala remittance pattern detected")
                .build());
        for (long id = 5L; id <= 16L; id++) {
            financialTransactions.add(FinancialTransaction.builder()
                    .rowId(id + 3)
                    .transactionId("TXN-2026-90" + String.format("%02d", id + 3))
                    .linkedCriminalId(id)
                    .amount(15000.0 + (id * 2500))
                    .fromAccount("ACC-100" + id + "-449")
                    .toAccount("ACC-200" + id + "-881")
                    .bankName("State Bank of India")
                    .timestamp("2026-06-25 12:00:00")
                    .isFlagged(false)
                    .flagReason(null)
                    .build());
        }
    }

    // ===== Getters =====
    public List<Crime> getCrimes() { return crimes; }
    public List<Criminal> getCriminals() { return criminals; }
    public List<CrimeCriminalLink> getLinks() { return links; }
    public List<CriminalNetwork> getNetwork() { return network; }
    public List<TimelineEvent> getTimelineEvents() { return timelineEvents; }
    public List<Investigation> getInvestigations() { return investigations; }
    public List<FinancialTransaction> getFinancialTransactions() { return financialTransactions; }
}
