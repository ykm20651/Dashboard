package com.andamiro.Dashboard.Fixture;

import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.Report;
import com.andamiro.Dashboard.Entity.User;

import java.util.UUID;

import static com.andamiro.Dashboard.Util.TestEntityUtil.forceSetId;

public class ReportFixture {

    public static Report createReport(Incident incident, User user, UUID id) {
        Report report = Report.create(incident, user, "/files/reports/test.pdf");
        forceSetId(report, "id", id);
        return report;
    }

    public static Report dummyReport(Incident incident, User user) {
        return Report.create(incident, user, "/files/reports/dummy.pdf");
    }
}
