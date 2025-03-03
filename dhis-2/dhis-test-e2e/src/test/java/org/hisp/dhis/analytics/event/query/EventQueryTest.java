/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.analytics.event.query;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hisp.dhis.analytics.ValidationHelper.validateHeader;
import static org.hisp.dhis.analytics.ValidationHelper.validateRow;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.List;
import java.util.Map;
import org.hisp.dhis.AnalyticsApiTest;
import org.hisp.dhis.test.e2e.actions.analytics.AnalyticsEventActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Groups e2e tests for Events "/query" endpoint.
 *
 * @author maikel arabori
 */
public class EventQueryTest extends AnalyticsApiTest {
  private final AnalyticsEventActions analyticsEventActions = new AnalyticsEventActions();

  @Test
  public void queryWithProgramAndProgramStageWhenTotalPagesIsFalse() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=pe:LAST_12_MONTHS,ou:ImspTQPwCqd")
            .add("stage=dBwrot7S420")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("totalPages=false")
            .add("desc=lastupdated")
            .add("relativePeriodDate=2022-09-27");

    // When
    ApiResponse response = analyticsEventActions.query().get("lxAQ7Zs9VYR", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(17)))
        .body("width", equalTo(17))
        .body("headerWidth", equalTo(17))
        .body("rows", hasSize(equalTo(3)))
        .body("height", equalTo(3))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(50))
        .body("metaData.pager.isLastPage", is(true))
        .body("metaData.pager", not(hasKey("total")))
        .body("metaData.pager", not(hasKey("pageCount")))
        .body("metaData.items.ImspTQPwCqd.name", equalTo("Sierra Leone"))
        .body("metaData.items.dBwrot7S420.name", equalTo("Antenatal care visit"))
        .body("metaData.items.ou.name", equalTo("Organisation unit"))
        .body("metaData.items.lxAQ7Zs9VYR.name", equalTo("Antenatal care visit"))
        .body("metaData.items.LAST_12_MONTHS.name", equalTo("Last 12 months"))
        .body("metaData.dimensions.pe", hasSize(equalTo(0)))
        .body("metaData.dimensions.ou", hasSize(equalTo(1)))
        .body("metaData.dimensions.ou", hasItem("ImspTQPwCqd"));

    // Validate headers
    validateHeader(response, 0, "psi", "Event", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "ps", "Program stage", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 2, "eventdate", "Visit date", "DATETIME", "java.time.LocalDateTime", false, true);
    validateHeader(response, 3, "storedby", "Stored by", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 4, "createdbydisplayname", "Created by", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        5,
        "lastupdatedbydisplayname",
        "Last updated by",
        "TEXT",
        "java.lang.String",
        false,
        true);
    validateHeader(
        response,
        6,
        "lastupdated",
        "Last updated on",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(
        response,
        7,
        "scheduleddate",
        "Scheduled date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(response, 8, "geometry", "Geometry", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 9, "longitude", "Longitude", "NUMBER", "java.lang.Double", false, true);
    validateHeader(response, 10, "latitude", "Latitude", "NUMBER", "java.lang.Double", false, true);
    validateHeader(
        response, 11, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        12,
        "ounamehierarchy",
        "Organisation unit name hierarchy",
        "TEXT",
        "java.lang.String",
        false,
        true);
    validateHeader(
        response, 13, "oucode", "Organisation unit code", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 14, "programstatus", "Program status", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 15, "eventstatus", "Event status", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 16, "ou", "Organisation unit", "TEXT", "java.lang.String", false, true);

    // Validate the first three rows, as samples.
    validateRow(
        response,
        0,
        List.of(
            "ohAH6BXIMad",
            "dBwrot7S420",
            "2022-04-07 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:41.933",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));

    validateRow(
        response,
        1,
        List.of(
            "onXW2DQHRGS",
            "dBwrot7S420",
            "2022-04-01 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:28.015",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));

    validateRow(
        response,
        2,
        List.of(
            "A7vnB73x5Xw",
            "dBwrot7S420",
            "2022-04-01 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:16.957",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));
  }

  @Test
  public void queryWithProgramAndProgramStageWhenTotalPagesIsTrueByDefault() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=pe:LAST_12_MONTHS,ou:ImspTQPwCqd")
            .add("stage=dBwrot7S420")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("lxAQ7Zs9VYR", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(17)))
        .body("width", equalTo(17))
        .body("headerWidth", equalTo(17))
        .body("rows", hasSize(equalTo(3)))
        .body("height", equalTo(3))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(50))
        .body("metaData.pager.total", equalTo(3))
        .body("metaData.pager.pageCount", equalTo(1))
        .body("metaData.pager", not(hasKey("isLastPage")))
        .body("metaData.items.ImspTQPwCqd.name", equalTo("Sierra Leone"))
        .body("metaData.items.dBwrot7S420.name", equalTo("Antenatal care visit"))
        .body("metaData.items.ou.name", equalTo("Organisation unit"))
        .body("metaData.items.lxAQ7Zs9VYR.name", equalTo("Antenatal care visit"))
        .body("metaData.items.LAST_12_MONTHS.name", equalTo("Last 12 months"))
        .body("metaData.dimensions.pe", hasSize(equalTo(0)))
        .body("metaData.dimensions.ou", hasSize(equalTo(1)))
        .body("metaData.dimensions.ou", hasItem("ImspTQPwCqd"));

    // Validate headers
    validateHeader(response, 0, "psi", "Event", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "ps", "Program stage", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 2, "eventdate", "Visit date", "DATETIME", "java.time.LocalDateTime", false, true);
    validateHeader(response, 3, "storedby", "Stored by", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 4, "createdbydisplayname", "Created by", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        5,
        "lastupdatedbydisplayname",
        "Last updated by",
        "TEXT",
        "java.lang.String",
        false,
        true);
    validateHeader(
        response,
        6,
        "lastupdated",
        "Last updated on",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(
        response,
        7,
        "scheduleddate",
        "Scheduled date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(response, 8, "geometry", "Geometry", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 9, "longitude", "Longitude", "NUMBER", "java.lang.Double", false, true);
    validateHeader(response, 10, "latitude", "Latitude", "NUMBER", "java.lang.Double", false, true);
    validateHeader(
        response, 11, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        12,
        "ounamehierarchy",
        "Organisation unit name hierarchy",
        "TEXT",
        "java.lang.String",
        false,
        true);
    validateHeader(
        response, 13, "oucode", "Organisation unit code", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 14, "programstatus", "Program status", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 15, "eventstatus", "Event status", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 16, "ou", "Organisation unit", "TEXT", "java.lang.String", false, true);

    // Validate the first three rows, as samples.
    validateRow(
        response,
        0,
        List.of(
            "ohAH6BXIMad",
            "dBwrot7S420",
            "2022-04-07 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:41.933",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));

    validateRow(
        response,
        1,
        List.of(
            "onXW2DQHRGS",
            "dBwrot7S420",
            "2022-04-01 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:28.015",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));

    validateRow(
        response,
        2,
        List.of(
            "A7vnB73x5Xw",
            "dBwrot7S420",
            "2022-04-01 00:00:00.0",
            "",
            ",  ()",
            ",  ()",
            "2018-04-12 16:05:16.957",
            "",
            "",
            "0.0",
            "0.0",
            "Ngelehun CHC",
            "Sierra Leone / Bo / Badjia / Ngelehun CHC",
            "OU_559",
            "ACTIVE",
            "ACTIVE",
            "DiszpKrYNg8"));
  }

  @Test
  public void queryWithProgramAndProgramStageFilteringByEventDateUsingFixedPeriods() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=ou:ImspTQPwCqd")
            .add("stage=Zj7UnCAulEk")
            .add("headers=eventdate,ouname,lastupdated")
            .add("totalPages=false")
            .add("eventDate=202204,202207")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("eBAyeGv0exc", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3))
        .body("rows", hasSize(equalTo(100)))
        .body("height", equalTo(100))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(100))
        .body("metaData.pager.total", equalTo(null))
        .body("metaData.pager.pageCount", equalTo(null))
        .body("metaData.pager.isLastPage", equalTo(false))
        .body("metaData.items.202204.name", equalTo("April 2022"))
        .body("metaData.items.202207.name", equalTo("July 2022"))
        .body("metaData.items.ImspTQPwCqd.uid", equalTo("ImspTQPwCqd"))
        .body("metaData.items.ImspTQPwCqd.code", equalTo("OU_525"))
        .body("metaData.items.ImspTQPwCqd.name", equalTo("Sierra Leone"))
        .body("metaData.items.ImspTQPwCqd.dimensionItemType", equalTo("ORGANISATION_UNIT"))
        .body("metaData.items.ImspTQPwCqd.valueType", equalTo("TEXT"))
        .body("metaData.items.ImspTQPwCqd.totalAggregationType", equalTo("SUM"))
        .body("metaData.items.eBAyeGv0exc.uid", equalTo("eBAyeGv0exc"))
        .body("metaData.items.eBAyeGv0exc.name", equalTo("Inpatient morbidity and mortality"))
        .body("metaData.items.ou.uid", equalTo("ou"))
        .body("metaData.items.ou.name", equalTo("Organisation unit"))
        .body("metaData.items.ou.dimensionType", equalTo("ORGANISATION_UNIT"))
        .body("metaData.items.Zj7UnCAulEk.uid", equalTo("Zj7UnCAulEk"))
        .body("metaData.items.Zj7UnCAulEk.name", equalTo("Inpatient morbidity and mortality"))
        .body(
            "metaData.items.Zj7UnCAulEk.description",
            equalTo("Anonymous and ICD-10 coded inpatient data"))
        .body("metaData.dimensions.pe", hasSize(equalTo(0)))
        .body("metaData.dimensions.ou", hasSize(equalTo(1)))
        .body("metaData.dimensions.ou", hasItem("ImspTQPwCqd"));

    // Validate headers.
    validateHeader(
        response,
        0,
        "eventdate",
        "Report date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(
        response, 1, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        2,
        "lastupdated",
        "Last updated on",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);

    // Validate the first three rows, as samples.
    validateRow(
        response, 0, List.of("2022-04-03 00:00:00.0", "Kayongoro MCHP", "2018-04-21 14:07:16.471"));

    validateRow(
        response,
        1,
        List.of("2022-04-23 00:00:00.0", "Bendu (Yawei) CHP", "2018-04-21 14:07:16.233"));

    validateRow(
        response,
        2,
        List.of("2022-07-31 00:00:00.0", "Mendekelema CHP", "2018-04-21 14:07:16.214"));
  }

  @Test
  public void queryWithProgramAndProgramStageFilteringByEventDateUsingRelativePeriod() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=ou:ImspTQPwCqd")
            .add("stage=Zj7UnCAulEk")
            .add("headers=eventdate,ouname")
            .add("totalPages=false")
            .add("eventDate=LAST_12_MONTHS")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("eBAyeGv0exc", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("width", equalTo(2))
        .body("headerWidth", equalTo(2))
        .body("rows", hasSize(equalTo(100)))
        .body("height", equalTo(100))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(100))
        .body("metaData.pager.total", equalTo(null))
        .body("metaData.pager.pageCount", equalTo(null))
        .body("metaData.pager.isLastPage", equalTo(false))
        .body("metaData.items.LAST_12_MONTHS.name", equalTo("Last 12 months"));

    // Validate headers.
    validateHeader(
        response,
        0,
        "eventdate",
        "Report date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(
        response, 1, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);

    // Validate the first three rows, as samples.
    validateRow(response, 0, List.of("2022-08-02 00:00:00.0", "Ngelehun CHC"));

    validateRow(response, 1, List.of("2022-08-02 00:00:00.0", "Ngelehun CHC"));

    validateRow(response, 2, List.of("2022-08-01 00:00:00.0", "Ngelehun CHC"));
  }

  @Test
  public void queryWithProgramAndProgramStageFilteringByEventDateUsingStartEndDates() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=ou:ImspTQPwCqd")
            .add("stage=Zj7UnCAulEk")
            .add("headers=eventdate,ouname")
            .add("totalPages=false")
            .add("eventDate=2021-03-02_2022-03-13")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("eBAyeGv0exc", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("width", equalTo(2))
        .body("headerWidth", equalTo(2))
        .body("rows", hasSize(equalTo(100)))
        .body("height", equalTo(100))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(100))
        .body("metaData.pager.total", equalTo(null))
        .body("metaData.pager.pageCount", equalTo(null))
        .body("metaData.pager.isLastPage", equalTo(false))
        .body("metaData.items.2021-03-02_2022-03-13.name", equalTo("2021-03-02 - 2022-03-13"));

    // Validate headers.
    validateHeader(
        response,
        0,
        "eventdate",
        "Report date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);
    validateHeader(
        response, 1, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);

    // Validate the first three rows, as samples.
    validateRow(response, 0, List.of("2021-11-04 00:00:00.0", "Ngelehun CHC"));

    validateRow(response, 1, List.of("2021-10-07 00:00:00.0", "Ngelehun CHC"));

    validateRow(response, 2, List.of("2021-11-05 00:00:00.0", "Ngelehun CHC"));
  }

  @Test
  public void
      queryWithProgramAndProgramStageAndProgramIndicatorFilteringByEventDateUsingRelativePeriod() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=ou:USER_ORGUNIT,p2Zxg0wcPQ3")
            .add("stage=A03MvHHogjR")
            .add("headers=ouname,p2Zxg0wcPQ3,eventdate")
            .add("totalPages=false")
            .add("eventDate=LAST_12_MONTHS")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("IpHINAT79UW", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3))
        .body("rows", hasSize(equalTo(100)))
        .body("height", equalTo(100))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(100))
        .body("metaData.pager.total", equalTo(null))
        .body("metaData.pager.pageCount", equalTo(null))
        .body("metaData.pager.isLastPage", equalTo(false))
        .body("metaData.items.LAST_12_MONTHS.name", equalTo("Last 12 months"))
        .body("metaData.items.ImspTQPwCqd.uid", equalTo("ImspTQPwCqd"))
        .body("metaData.items.ImspTQPwCqd.code", equalTo("OU_525"))
        .body("metaData.items.ImspTQPwCqd.name", equalTo("Sierra Leone"))
        .body("metaData.items.ImspTQPwCqd.dimensionItemType", equalTo("ORGANISATION_UNIT"))
        .body("metaData.items.ImspTQPwCqd.valueType", equalTo("TEXT"))
        .body("metaData.items.ImspTQPwCqd.totalAggregationType", equalTo("SUM"))
        .body("metaData.items.p2Zxg0wcPQ3.uid", equalTo("p2Zxg0wcPQ3"))
        .body("metaData.items.p2Zxg0wcPQ3.code", equalTo("BCG_DOSE"))
        .body("metaData.items.p2Zxg0wcPQ3.name", equalTo("BCG doses"))
        .body("metaData.items.p2Zxg0wcPQ3.dimensionItemType", equalTo("PROGRAM_INDICATOR"))
        .body("metaData.items.p2Zxg0wcPQ3.valueType", equalTo("NUMBER"))
        .body("metaData.items.p2Zxg0wcPQ3.totalAggregationType", equalTo("SUM"))
        .body("metaData.items.IpHINAT79UW.uid", equalTo("IpHINAT79UW"))
        .body("metaData.items.IpHINAT79UW.name", equalTo("Child Programme"))
        .body("metaData.items.ou.uid", equalTo("ou"))
        .body("metaData.items.ou.name", equalTo("Organisation unit"))
        .body("metaData.items.ou.dimensionType", equalTo("ORGANISATION_UNIT"))
        .body("metaData.items.A03MvHHogjR.uid", equalTo("A03MvHHogjR"))
        .body("metaData.items.A03MvHHogjR.name", equalTo("Birth"))
        .body("metaData.items.A03MvHHogjR.description", equalTo("Birth of the baby"))
        .body("metaData.dimensions.pe", hasSize(equalTo(0)))
        .body("metaData.dimensions.ou", hasSize(equalTo(1)))
        .body("metaData.dimensions.ou", hasItem("ImspTQPwCqd"))
        .body("metaData.dimensions.p2Zxg0wcPQ3", hasSize(equalTo(0)));

    // Validate headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 1, "p2Zxg0wcPQ3", "BCG doses", "NUMBER", "java.lang.Double", false, true);
    validateHeader(
        response,
        2,
        "eventdate",
        "Report date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);

    // Validate the first three rows, as samples.
    validateRow(response, 0, List.of("Ngelehun CHC", "0", "2022-02-27 00:00:00.0"));

    validateRow(response, 1, List.of("Ngelehun CHC", "0", "2022-05-27 00:00:00.0"));

    validateRow(response, 2, List.of("Ngelehun CHC", "1", "2022-01-19 00:00:00.0"));
  }

  @Test
  public void
      queryWithProgramAndProgramStageAndProgramIndicatorFilteringByEventDateUsingFixedDates() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=ou:USER_ORGUNIT,p2Zxg0wcPQ3")
            .add("stage=A03MvHHogjR")
            .add("headers=ouname,p2Zxg0wcPQ3,eventdate")
            .add("totalPages=false")
            .add("eventDate=2021-03-02_2023-03-01")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=lastupdated")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("IpHINAT79UW", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3))
        .body("rows", hasSize(equalTo(100)))
        .body("height", equalTo(100))
        .body("metaData.pager.page", equalTo(1))
        .body("metaData.pager.pageSize", equalTo(100))
        .body("metaData.pager.total", equalTo(null))
        .body("metaData.pager.pageCount", equalTo(null))
        .body("metaData.pager.isLastPage", equalTo(false));

    // Validate headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response, 1, "p2Zxg0wcPQ3", "BCG doses", "NUMBER", "java.lang.Double", false, true);
    validateHeader(
        response,
        2,
        "eventdate",
        "Report date",
        "DATETIME",
        "java.time.LocalDateTime",
        false,
        true);

    // Validate the first three rows, as samples.
    validateRow(response, 0, List.of("Ngelehun CHC", "0", "2022-02-27 00:00:00.0"));

    validateRow(response, 1, List.of("Ngelehun CHC", "1", "2022-12-29 00:00:00.0"));

    validateRow(response, 2, List.of("Ngelehun CHC", "0", "2021-08-15 00:00:00.0"));
  }

  @Test
  public void eventQueryWithProgramAndRepeatableProgramStage() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=edqlbukwRfQ.vANAXwtLwcT,ou:ImspTQPwCqd, pe:THIS_YEAR")
            .add("headers=ou,ounamehierarchy,edqlbukwRfQ.vANAXwtLwcT")
            .add("stage=edqlbukwRfQ")
            .add("displayProperty=NAME")
            .add("outputType=EVENT")
            .add("desc=incidentdate,edqlbukwRfQ.vANAXwtLwcT")
            .add("totalPages=false")
            .add("pageSize=2")
            .add("page=1")
            .add("rowContext=true")
            .add("relativePeriodDate=2022-09-22");

    // When
    ApiResponse response = analyticsEventActions.query().get("WSGAb5XwJ3Y", JSON, JSON, params);
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("rows", hasSize(equalTo(2)));

    validateHeader(response, 0, "ou", "Organisation unit", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        1,
        "ounamehierarchy",
        "Organisation unit name hierarchy",
        "TEXT",
        "java.lang.String",
        false,
        true);
    validateHeader(
        response,
        2,
        "edqlbukwRfQ.vANAXwtLwcT",
        "WHOMCH Hemoglobin value",
        "NUMBER",
        "java.lang.Double",
        false,
        true);

    validateRow(
        response,
        0,
        List.of(
            "zQpYVEyAM2t",
            "Sierra Leone / Western Area / Rural Western Area / Hastings Health Centre",
            "10.0"));
    validateRow(
        response,
        1,
        List.of("lyONqUkY1Bq", "Sierra Leone / Tonkolili / Kunike / Matholey MCHP", "17.0"));
  }

  @Test
  void testMetadataInfoForOptionSetForQuery() {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add(
                "dimension=ou:ImspTQPwCqd,pe:LAST_12_MONTHS,C0aLZo75dgJ.B6TnnFMgmCk,C0aLZo75dgJ.Z1rLc1rVHK8,C0aLZo75dgJ.CklPZdOd6H1")
            .add("filter=C0aLZo75dgJ.vTKipVM0GsX,C0aLZo75dgJ.h5FuguPFF2j,C0aLZo75dgJ.aW66s2QSosT")
            .add("stage=C0aLZo75dgJ")
            .add("displayProperty=NAME")
            .add("outputType=ENROLLMENT")
            .add("totalPages=false");

    // When
    ApiResponse response = analyticsEventActions.query().get("qDkgAbB5Jlk", JSON, JSON, params);
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(24)))
        .body("height", equalTo(0))
        .body("width", equalTo(0))
        .body("rows", hasSize(equalTo(0)))
        .body("metaData.items", hasKey("CklPZdOd6H1"))
        .body("metaData.items", not(hasKey("AZK4rjJCss5")))
        .body("metaData.items", not(hasKey("UrUdMteQzlT")));

    validateHeader(
        response,
        22,
        "C0aLZo75dgJ.CklPZdOd6H1",
        "Sex",
        "TEXT",
        "java.lang.String",
        false,
        true,
        "hiQ3QFheQ3O");
  }

  @Test
  public void queryMetadataInfoForOptionSetAndOptionsWhenNoData() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("includeMetadataDetails=true")
            .add("asc=lastupdated")
            .add("headers=ouname,A03MvHHogjR.ebaJjqltK5N")
            .add("lastUpdated=LAST_5_YEARS")
            .add("stage=A03MvHHogjR")
            .add("displayProperty=NAME")
            .add("totalPages=false")
            .add("outputType=EVENT")
            .add("pageSize=0")
            .add("page=1")
            .add("dimension=ou:USER_ORGUNIT,A03MvHHogjR.ebaJjqltK5N:IN:1;2")
            .add("relativePeriodDate=2022-10-01");

    // When
    ApiResponse response = analyticsEventActions.query().get("IpHINAT79UW", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("rows", hasSize(equalTo(0)))
        .body("height", equalTo(0))
        .body("width", equalTo(0))
        .body("headerWidth", equalTo(2));

    // Assert metaData.
    String expectedMetaData =
        "{\"pager\":{\"page\":1,\"pageSize\":0,\"isLastPage\":false},\"items\":{\"kzgQRhOCadd\":{\"uid\":\"kzgQRhOCadd\",\"name\":\"MNCH Polio doses (0-3)\",\"options\":[{\"code\":\"2\",\"uid\":\"Xr0M5yEhtpT\"},{\"code\":\"1\",\"uid\":\"lFFqylGiWLk\"}]},\"ImspTQPwCqd\":{\"uid\":\"ImspTQPwCqd\",\"code\":\"OU_525\",\"name\":\"Sierra Leone\",\"dimensionItemType\":\"ORGANISATION_UNIT\",\"valueType\":\"TEXT\",\"totalAggregationType\":\"SUM\"},\"ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"IpHINAT79UW\":{\"uid\":\"IpHINAT79UW\",\"name\":\"Child Programme\"},\"ou\":{\"uid\":\"ou\",\"name\":\"Organisation unit\",\"dimensionType\":\"ORGANISATION_UNIT\"},\"A03MvHHogjR\":{\"uid\":\"A03MvHHogjR\",\"name\":\"Birth\",\"description\":\"Birth of the baby\"},\"Xr0M5yEhtpT\":{\"uid\":\"Xr0M5yEhtpT\",\"code\":\"2\",\"name\":\"Dose 2\"},\"A03MvHHogjR.ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"LAST_5_YEARS\":{\"name\":\"Last 5 years\"},\"lFFqylGiWLk\":{\"uid\":\"lFFqylGiWLk\",\"code\":\"1\",\"name\":\"Dose 1\"}},\"dimensions\":{\"pe\":[],\"ou\":[\"ImspTQPwCqd\"],\"A03MvHHogjR.ebaJjqltK5N\":[\"lFFqylGiWLk\",\"Xr0M5yEhtpT\"]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        1,
        "A03MvHHogjR.ebaJjqltK5N",
        "MCH OPV dose",
        "TEXT",
        "java.lang.String",
        false,
        true);

    // Assert rows.
  }

  @Test
  public void queryMetadataInfoForOptionSetAndOptionsWhenOneRows() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("includeMetadataDetails=true")
            .add("asc=lastupdated")
            .add("headers=ouname,A03MvHHogjR.ebaJjqltK5N")
            .add("lastUpdated=LAST_5_YEARS")
            .add("stage=A03MvHHogjR")
            .add("displayProperty=NAME")
            .add("totalPages=false")
            .add("outputType=EVENT")
            .add("pageSize=1")
            .add("page=1")
            .add("dimension=ou:USER_ORGUNIT,A03MvHHogjR.ebaJjqltK5N:IN:1;2")
            .add("relativePeriodDate=2022-10-01");

    // When
    ApiResponse response = analyticsEventActions.query().get("IpHINAT79UW", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("rows", hasSize(equalTo(1)))
        .body("height", equalTo(1))
        .body("width", equalTo(2))
        .body("headerWidth", equalTo(2));

    // Assert metaData.
    String expectedMetaData =
        "{\"pager\":{\"page\":1,\"pageSize\":1,\"isLastPage\":false},\"items\":{\"kzgQRhOCadd\":{\"uid\":\"kzgQRhOCadd\",\"name\":\"MNCH Polio doses (0-3)\",\"options\":[{\"code\":\"1\",\"uid\":\"lFFqylGiWLk\"}]},\"ImspTQPwCqd\":{\"uid\":\"ImspTQPwCqd\",\"code\":\"OU_525\",\"name\":\"Sierra Leone\",\"dimensionItemType\":\"ORGANISATION_UNIT\",\"valueType\":\"TEXT\",\"totalAggregationType\":\"SUM\"},\"ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"IpHINAT79UW\":{\"uid\":\"IpHINAT79UW\",\"name\":\"Child Programme\"},\"ou\":{\"uid\":\"ou\",\"name\":\"Organisation unit\",\"dimensionType\":\"ORGANISATION_UNIT\"},\"A03MvHHogjR\":{\"uid\":\"A03MvHHogjR\",\"name\":\"Birth\",\"description\":\"Birth of the baby\"},\"A03MvHHogjR.ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"LAST_5_YEARS\":{\"name\":\"Last 5 years\"},\"lFFqylGiWLk\":{\"uid\":\"lFFqylGiWLk\",\"code\":\"1\",\"name\":\"Dose 1\"}},\"dimensions\":{\"pe\":[],\"ou\":[\"ImspTQPwCqd\"],\"A03MvHHogjR.ebaJjqltK5N\":[\"lFFqylGiWLk\"]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        1,
        "A03MvHHogjR.ebaJjqltK5N",
        "MCH OPV dose",
        "TEXT",
        "java.lang.String",
        false,
        true);

    // Assert rows.
    validateRow(response, 0, List.of("Ngelehun CHC", "1"));
  }

  @Test
  public void queryMetadataInfoForOptionSetAndOptionsWhenTenRows() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("includeMetadataDetails=true")
            .add("asc=lastupdated")
            .add("headers=ouname,A03MvHHogjR.ebaJjqltK5N")
            .add("lastUpdated=LAST_5_YEARS")
            .add("stage=A03MvHHogjR")
            .add("displayProperty=NAME")
            .add("totalPages=false")
            .add("outputType=EVENT")
            .add("pageSize=1")
            .add("page=10")
            .add("dimension=ou:USER_ORGUNIT,A03MvHHogjR.ebaJjqltK5N:IN:1;2")
            .add("relativePeriodDate=2022-10-01");

    // When
    ApiResponse response = analyticsEventActions.query().get("IpHINAT79UW", JSON, JSON, params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("rows", hasSize(equalTo(1)))
        .body("height", equalTo(1))
        .body("width", equalTo(2))
        .body("headerWidth", equalTo(2));

    // Assert metaData.
    String expectedMetaData =
        "{\"pager\":{\"page\":10,\"pageSize\":1,\"isLastPage\":false},\"items\":{\"kzgQRhOCadd\":{\"uid\":\"kzgQRhOCadd\",\"name\":\"MNCH Polio doses (0-3)\",\"options\":[{\"code\":\"1\",\"uid\":\"lFFqylGiWLk\"}]},\"ImspTQPwCqd\":{\"uid\":\"ImspTQPwCqd\",\"code\":\"OU_525\",\"name\":\"Sierra Leone\",\"dimensionItemType\":\"ORGANISATION_UNIT\",\"valueType\":\"TEXT\",\"totalAggregationType\":\"SUM\"},\"ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"IpHINAT79UW\":{\"uid\":\"IpHINAT79UW\",\"name\":\"Child Programme\"},\"ou\":{\"uid\":\"ou\",\"name\":\"Organisation unit\",\"dimensionType\":\"ORGANISATION_UNIT\"},\"A03MvHHogjR\":{\"uid\":\"A03MvHHogjR\",\"name\":\"Birth\",\"description\":\"Birth of the baby\"},\"A03MvHHogjR.ebaJjqltK5N\":{\"uid\":\"ebaJjqltK5N\",\"code\":\"DE_2006104\",\"name\":\"MCH OPV dose\",\"dimensionItemType\":\"DATA_ELEMENT\",\"valueType\":\"TEXT\",\"aggregationType\":\"AVERAGE\",\"totalAggregationType\":\"SUM\"},\"LAST_5_YEARS\":{\"name\":\"Last 5 years\"},\"lFFqylGiWLk\":{\"uid\":\"lFFqylGiWLk\",\"code\":\"1\",\"name\":\"Dose 1\"}},\"dimensions\":{\"pe\":[],\"ou\":[\"ImspTQPwCqd\"],\"A03MvHHogjR.ebaJjqltK5N\":[\"lFFqylGiWLk\"]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        1,
        "A03MvHHogjR.ebaJjqltK5N",
        "MCH OPV dose",
        "TEXT",
        "java.lang.String",
        false,
        true);

    // Assert rows.
    validateRow(response, 0, List.of("Ngelehun CHC", "1"));
  }

  @Test
  public void queryWithOrgUnitDataElement() throws JSONException {
    // Given
    String dimensionItems =
        String.join(
            ";",
            "DiszpKrYNg8",
            "g8upMTyEZGZ",
            "LEVEL-H1KlN4QIauv",
            "OU_GROUP-nlX2VoouN63",
            "USER_ORGUNIT",
            "USER_ORGUNIT_CHILDREN",
            "USER_ORGUNIT_GRANDCHILDREN");

    String dimensionOrgUnitDataElement = "Ge7Eo3FNnbl.rypjN8CV02V:IN:" + dimensionItems;
    String dimensionOrgUnit = "ou:USER_ORGUNIT";

    String dimension = dimensionOrgUnitDataElement + "," + dimensionOrgUnit;

    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("dimension=" + dimension)
            .add("headers=ouname,Ge7Eo3FNnbl.rypjN8CV02V")
            .add("totalPages=false")
            .add("displayProperty=NAME")
            .add("pageSize=100")
            .add("page=1")
            .add("includeMetadataDetails=true")
            .add("outputType=EVENT");

    // When
    ApiResponse response = analyticsEventActions.query().get("MoUd5BTQ3lY", params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("rows", hasSize(equalTo(0)))
        .body("height", equalTo(0))
        .body("width", equalTo(0))
        .body("headerWidth", equalTo(2));

    // Assert metaData.
    String expectedMetaData =
        "{\"pager\":{\"isLastPage\":true,\"pageSize\":100,\"page\":1},\"items\":{\"ImspTQPwCqd\":{\"uid\":\"ImspTQPwCqd\",\"code\":\"OU_525\",\"valueType\":\"TEXT\",\"name\":\"Sierra Leone\",\"dimensionItemType\":\"ORGANISATION_UNIT\",\"totalAggregationType\":\"SUM\"},\"USER_ORGUNIT\":{\"organisationUnits\":[\"ImspTQPwCqd\"]},\"ou\":{\"uid\":\"ou\",\"dimensionType\":\"ORGANISATION_UNIT\",\"name\":\"Organisation unit\"},\"Ge7Eo3FNnbl\":{\"uid\":\"Ge7Eo3FNnbl\",\"name\":\"XX MAL RDT - Case Registration\"},\"Ge7Eo3FNnbl.rypjN8CV02V\":{\"uid\":\"rypjN8CV02V\",\"aggregationType\":\"SUM\",\"valueType\":\"TEXT\",\"name\":\"XX MAL RDT TRK - Village of Residence\",\"style\":{\"icon\":\"nullapi\\/icons\\/star_medium_positive\\/icon.svg\"},\"dimensionItemType\":\"DATA_ELEMENT\",\"totalAggregationType\":\"SUM\"},\"MoUd5BTQ3lY\":{\"uid\":\"MoUd5BTQ3lY\",\"name\":\"XX MAL RDT - Case Registration\"},\"USER_ORGUNIT_CHILDREN\":{\"organisationUnits\":[\"at6UHUQatSo\",\"TEQlaapDQoK\",\"PMa2VCrupOd\",\"qhqAxPSTUXp\",\"kJq2mPyFEHo\",\"jmIPBj66vD6\",\"Vth0fbpFcsO\",\"jUb8gELQApl\",\"fdc6uOvgoji\",\"eIQbndfxQMb\",\"O6uvpzGd5pu\",\"lc3eMKXaEfw\",\"bL4ooGhyHRQ\"]},\"rypjN8CV02V\":{\"uid\":\"rypjN8CV02V\",\"aggregationType\":\"SUM\",\"valueType\":\"TEXT\",\"name\":\"XX MAL RDT TRK - Village of Residence\",\"style\":{\"icon\":\"nullapi\\/icons\\/star_medium_positive\\/icon.svg\"},\"dimensionItemType\":\"DATA_ELEMENT\",\"totalAggregationType\":\"SUM\"},\"LAST_12_MONTHS\":{\"name\":\"Last 12 months\"}},\"dimensions\":{\"pe\":[],\"ou\":[\"ImspTQPwCqd\"],\"Ge7Eo3FNnbl.rypjN8CV02V\":[]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(
        response, 0, "ouname", "Organisation unit name", "TEXT", "java.lang.String", false, true);
    validateHeader(
        response,
        1,
        "Ge7Eo3FNnbl.rypjN8CV02V",
        "XX MAL RDT TRK - Village of Residence",
        "ORGANISATION_UNIT",
        "org.hisp.dhis.organisationunit.OrganisationUnit",
        false,
        true);

    // no rows to assert
  }
}
