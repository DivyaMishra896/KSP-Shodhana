package com.ksp.shodhana.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing an event in an investigation timeline.
 * Maps to the "TimelineEvent" table in Catalyst Data Store.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {

    private Long rowId;
    private Long investigationRowId;

    @JsonAlias({"eventType", "type"})
    private String eventType;

    @JsonAlias({"eventDate", "timestamp", "date"})
    private String eventDate;

    private String title;
    private String description;

    @JsonAlias({"createdBy", "officerName", "officer"})
    private String createdBy;
}
