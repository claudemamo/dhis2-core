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
package org.hisp.dhis.trackedentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Ameen Mohamed <ameen@dhis2.org>
 */
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
class TrackedEntityProgramOwnerServiceTest extends PostgresIntegrationTestBase {

  private static final String PA = "PA";

  private static final String TE_B1 = "TE-B1";

  private static final String TE_A1 = "TE-A1";

  @Autowired private TrackedEntityService trackedEntityService;

  @Autowired private TrackedEntityProgramOwnerService programOwnerService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private ProgramService programService;

  private OrganisationUnit organisationUnitA;

  private OrganisationUnit organisationUnitB;

  @BeforeAll
  void setUp() {
    organisationUnitA = createOrganisationUnit('A');
    organisationUnitService.addOrganisationUnit(organisationUnitA);
    organisationUnitB = createOrganisationUnit('B');
    organisationUnitService.addOrganisationUnit(organisationUnitB);
    TrackedEntity trackedEntityA1 = createTrackedEntity(organisationUnitA);
    trackedEntityA1.setUid(TE_A1);
    TrackedEntity trackedEntityB1 = createTrackedEntity(organisationUnitA);
    trackedEntityB1.setUid(TE_B1);
    trackedEntityService.addTrackedEntity(trackedEntityA1);
    trackedEntityService.addTrackedEntity(trackedEntityB1);
    Program programA = createProgram('A');
    programA.setUid(PA);
    programService.addProgram(programA);
  }

  @Test
  void testCreateTrackedEntityProgramOwner() {
    programOwnerService.createTrackedEntityProgramOwner(TE_A1, PA, organisationUnitA.getUid());
    assertNotNull(programOwnerService.getTrackedEntityProgramOwner(TE_A1, PA));
    assertNull(programOwnerService.getTrackedEntityProgramOwner(TE_B1, PA));
  }

  @Test
  void testCreateOrUpdateTrackedEntityProgramOwner() {
    programOwnerService.createOrUpdateTrackedEntityProgramOwner(
        TE_A1, PA, organisationUnitA.getUid());
    TrackedEntityProgramOwner programOwner =
        programOwnerService.getTrackedEntityProgramOwner(TE_A1, PA);
    assertNotNull(programOwner);
    assertEquals(organisationUnitA.getUid(), programOwner.getOrganisationUnit().getUid());
    programOwnerService.createOrUpdateTrackedEntityProgramOwner(
        TE_A1, PA, organisationUnitB.getUid());
    programOwner = programOwnerService.getTrackedEntityProgramOwner(TE_A1, PA);
    assertNotNull(programOwner);
    assertEquals(organisationUnitB.getUid(), programOwner.getOrganisationUnit().getUid());
  }
}
