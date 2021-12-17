/*
 * Copyright (c) 2004-2021, University of Oslo
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
package org.hisp.dhis.tracker.validation.hooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1083;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1100;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1103;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.DhisConvenienceTest;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.program.ProgramType;
import org.hisp.dhis.security.Authorities;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.ValidationErrorReporter;
import org.hisp.dhis.tracker.validation.TrackerImportValidationContext;
import org.hisp.dhis.tracker.validation.service.TrackerImportAccessManager;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Enrico Colasante
 */
@MockitoSettings( strictness = Strictness.LENIENT )
@ExtendWith( MockitoExtension.class )
public class PreCheckSecurityOwnershipValidationHookTest extends DhisConvenienceTest
{

    private static final String ORG_UNIT_ID = "ORG_UNIT_ID";

    private static final String TEI_ID = "TEI_ID";

    private static final String TEI_TYPE_ID = "TEI_TYPE_ID";

    private static final String PROGRAM_ID = "PROGRAM_ID";

    private static final String PS_ID = "PS_ID";

    private PreCheckSecurityOwnershipValidationHook validatorToTest;

    @Mock
    private TrackerImportAccessManager trackerImportAccessManager;

    @Mock
    private TrackerImportValidationContext ctx;

    private TrackerBundle bundle;

    @Mock
    private TrackerPreheat preheat;

    private ValidationErrorReporter reporter;

    private OrganisationUnit organisationUnit;

    private TrackedEntityType trackedEntityType;

    private Program program;

    private ProgramStage programStage;

    @BeforeEach
    public void setUp()
    {
        validatorToTest = new PreCheckSecurityOwnershipValidationHook( trackerImportAccessManager );
        User user = createUser( 'A' );
        bundle = TrackerBundle.builder().user( user ).preheat( preheat ).build();

        when( ctx.getBundle() ).thenReturn( bundle );

        organisationUnit = createOrganisationUnit( 'A' );
        organisationUnit.setUid( ORG_UNIT_ID );
        when( ctx.getOrganisationUnit( ORG_UNIT_ID ) ).thenReturn( organisationUnit );

        trackedEntityType = createTrackedEntityType( 'A' );
        trackedEntityType.setUid( TEI_TYPE_ID );
        when( ctx.getTrackedEntityType( TEI_TYPE_ID ) ).thenReturn( trackedEntityType );

        program = createProgram( 'A' );
        program.setUid( PROGRAM_ID );
        program.setProgramType( ProgramType.WITH_REGISTRATION );
        when( ctx.getProgram( PROGRAM_ID ) ).thenReturn( program );

        programStage = createProgramStage( 'A', program );
        programStage.setUid( PS_ID );
        when( ctx.getProgramStage( PS_ID ) ).thenReturn( programStage );

        when( ctx.getOwnerOrganisationUnit( TEI_ID, PROGRAM_ID ) ).thenReturn( organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForTrackedEntity()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
    }

    @Test
    public void verifyValidationSuccessForTrackedEntityWithNoProgramInstancesUsingDeleteStrategy()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithNoProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
    }

    @Test
    public void verifyCaptureScopeIsCheckedForTrackedEntityCreation()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.CREATE );
        when( ctx.getOrganisationUnit( ORG_UNIT_ID ) ).thenReturn( organisationUnit );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifySearchScopeIsCheckedForTrackedEntityUpdation()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithNoProgramInstances() );
        when( ctx.getOrganisationUnit( ORG_UNIT_ID ) ).thenReturn( organisationUnit );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
        verify( trackerImportAccessManager ).checkOrgUnitInSearchScope( reporter, organisationUnit );
    }

    @Test
    public void verifySearchScopeIsCheckedForTrackedEntityDeletion()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithNoProgramInstances() );
        when( ctx.getOrganisationUnit( ORG_UNIT_ID ) ).thenReturn( organisationUnit );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForTrackedEntityWithDeletedProgramInstancesUsingDeleteStrategy()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithDeleteProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
    }

    @Test
    public void verifyValidationSuccessForTrackedEntityUsingDeleteStrategyAndUserWithCascadeAuthority()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );
        bundle.setUser( deleteTeiAuthorisedUser() );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
    }

    @Test
    public void verifyValidationFailsForTrackedEntityUsingDeleteStrategyAndUserWithoutCascadeAuthority()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( TEI_ID )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntityType( TEI_TYPE_ID )
            .build();

        when( ctx.getStrategy( trackedEntity ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, trackedEntity );

        validatorToTest.validateTrackedEntity( reporter, trackedEntity );

        assertTrue( reporter.hasErrors() );
        assertThat( reporter.getReportList().get( 0 ).getErrorCode(), is( E1100 ) );
        verify( trackerImportAccessManager ).checkTeiTypeWriteAccess( reporter, trackedEntityType );
    }

    @Test
    public void verifyValidationSuccessForEnrollment()
    {
        Enrollment enrollment = Enrollment.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
    }

    @Test
    public void verifyCaptureScopeIsCheckedForEnrollmentCreation()
    {
        Enrollment enrollment = Enrollment.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.CREATE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifyCaptureScopeIsCheckedForEnrollmentDeletion()
    {
        String enrollmentUid = CodeGenerator.generateUid();
        Enrollment enrollment = Enrollment.builder()
            .enrollment( enrollmentUid )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithNoProgramInstances() );
        when( ctx.getProgramInstance( enrollment.getEnrollment() ) ).thenReturn( getEnrollment( enrollmentUid ) );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifyCaptureScopeIsCheckedForEnrollmentProgramWithoutRegistration()
    {
        program.setProgramType( ProgramType.WITHOUT_REGISTRATION );

        String enrollmentUid = CodeGenerator.generateUid();
        Enrollment enrollment = Enrollment.builder()
            .enrollment( enrollmentUid )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithNoProgramInstances() );
        when( ctx.getProgramInstance( enrollment.getEnrollment() ) ).thenReturn( getEnrollment( enrollmentUid ) );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForEnrollmentWithoutEventsUsingDeleteStrategy()
    {
        Enrollment enrollment = Enrollment.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.programInstanceHasEvents( enrollment.getEnrollment() ) ).thenReturn( false );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForEnrollmentUsingDeleteStrategyAndUserWithCascadeAuthority()
    {
        Enrollment enrollment = Enrollment.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.programInstanceHasEvents( enrollment.getEnrollment() ) ).thenReturn( true );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );
        bundle.setUser( deleteEnrollmentAuthorisedUser() );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
    }

    @Test
    public void verifyValidationFailsForEnrollmentUsingDeleteStrategyAndUserWithoutCascadeAuthority()
    {
        Enrollment enrollment = Enrollment.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .trackedEntity( TEI_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( enrollment ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.programInstanceHasEvents( enrollment.getEnrollment() ) ).thenReturn( true );
        when( ctx.getTrackedEntityInstance( TEI_ID ) ).thenReturn( getTEIWithProgramInstances() );

        reporter = new ValidationErrorReporter( ctx, enrollment );

        validatorToTest.validateEnrollment( reporter, enrollment );

        assertTrue( reporter.hasErrors() );
        assertThat( reporter.getReportList().get( 0 ).getErrorCode(), is( E1103 ) );
        verify( trackerImportAccessManager ).checkWriteEnrollmentAccess( reporter, program,
            TEI_ID, organisationUnit, organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForEventUsingDeleteStrategy()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.DELETE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            null, false );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );
    }

    @Test
    public void verifyValidationSuccessForNonTrackerEventUsingCreateStrategy()
    {
        program.setProgramType( ProgramType.WITHOUT_REGISTRATION );
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.CREATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            null, null,
            null, false );
    }

    @Test
    public void verifyValidationSuccessForTrackerEventCreation()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.CREATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( null );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            TEI_ID, false );
        verify( trackerImportAccessManager ).checkOrgUnitInCaptureScope( reporter, organisationUnit );

    }

    @Test
    public void verifyValidationSuccessForTrackerEventUpdation()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            TEI_ID, false );
    }

    @Test
    public void verifyValidationSuccessForEventUsingUpdateStrategy()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .status( EventStatus.COMPLETED )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.UPDATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            null, false );
    }

    @Test
    public void verifyValidationSuccessForEventUsingUpdateStrategyAndUserWithAuthority()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.UPDATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        bundle.setUser( changeCompletedEventAuthorisedUser() );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertFalse( reporter.hasErrors() );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            null, false );
    }

    @Test
    public void verifyValidationFailsForEventUsingUpdateStrategyAndUserWithoutAuthority()
    {
        Event event = Event.builder()
            .enrollment( CodeGenerator.generateUid() )
            .orgUnit( ORG_UNIT_ID )
            .programStage( PS_ID )
            .program( PROGRAM_ID )
            .build();

        when( ctx.getStrategy( event ) ).thenReturn( TrackerImportStrategy.UPDATE );
        when( ctx.getProgramStageInstance( event.getEvent() ) ).thenReturn( getEvent() );
        when( ctx.getProgramInstance( event.getEnrollment() ) ).thenReturn( getEnrollment( null ) );

        reporter = new ValidationErrorReporter( ctx, event );

        validatorToTest.validateEvent( reporter, event );

        assertTrue( reporter.hasErrors() );
        assertThat( reporter.getReportList().get( 0 ).getErrorCode(), is( E1083 ) );
        verify( trackerImportAccessManager ).checkEventWriteAccess( reporter, programStage, organisationUnit,
            organisationUnit, null,
            null, false );
    }

    private TrackedEntityInstance getTEIWithNoProgramInstances()
    {
        TrackedEntityInstance trackedEntityInstance = createTrackedEntityInstance( organisationUnit );
        trackedEntityInstance.setUid( TEI_ID );
        trackedEntityInstance.setProgramInstances( Sets.newHashSet() );
        return trackedEntityInstance;
    }

    private TrackedEntityInstance getTEIWithDeleteProgramInstances()
    {
        ProgramInstance programInstance = new ProgramInstance();
        programInstance.setDeleted( true );

        TrackedEntityInstance trackedEntityInstance = createTrackedEntityInstance( organisationUnit );
        trackedEntityInstance.setUid( TEI_ID );
        trackedEntityInstance.setProgramInstances( Sets.newHashSet( programInstance ) );

        return trackedEntityInstance;
    }

    private TrackedEntityInstance getTEIWithProgramInstances()
    {
        TrackedEntityInstance trackedEntityInstance = createTrackedEntityInstance( organisationUnit );
        trackedEntityInstance.setUid( TEI_ID );
        trackedEntityInstance.setProgramInstances( Sets.newHashSet( new ProgramInstance() ) );

        return trackedEntityInstance;
    }

    private ProgramInstance getEnrollment( String enrollmentUid )
    {
        if ( StringUtils.isEmpty( enrollmentUid ) )
        {
            enrollmentUid = CodeGenerator.generateUid();
        }
        ProgramInstance programInstance = new ProgramInstance();
        programInstance.setUid( enrollmentUid );
        programInstance.setOrganisationUnit( organisationUnit );
        programInstance.setEntityInstance( getTEIWithNoProgramInstances() );
        programInstance.setProgram( program );
        programInstance.setStatus( ProgramStatus.ACTIVE );
        return programInstance;
    }

    private ProgramStageInstance getEvent()
    {
        ProgramStageInstance programStageInstance = new ProgramStageInstance();
        programStageInstance.setProgramStage( programStage );
        programStageInstance.setOrganisationUnit( organisationUnit );
        programStageInstance.setProgramInstance( new ProgramInstance() );
        programStageInstance.setStatus( EventStatus.COMPLETED );
        return programStageInstance;
    }

    private User deleteTeiAuthorisedUser()
    {
        return createUser( 'A', Lists.newArrayList( Authorities.F_TEI_CASCADE_DELETE.getAuthority() ) );
    }

    private User deleteEnrollmentAuthorisedUser()
    {
        return createUser( 'A', Lists.newArrayList( Authorities.F_ENROLLMENT_CASCADE_DELETE.getAuthority() ) );
    }

    private User changeCompletedEventAuthorisedUser()
    {
        return createUser( 'A', Lists.newArrayList( "F_UNCOMPLETE_EVENT" ) );
    }
}