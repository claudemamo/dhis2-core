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
package org.hisp.dhis.dxf2.events.importer.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Luciano Fiandesio
 */
@MockitoSettings( strictness = Strictness.LENIENT )
class OrganisationUnitSupplierTest extends AbstractSupplierTest<OrganisationUnit, Set<OrganisationUnit>>

{

    private OrganisationUnitSupplier subject;

    private static final String uid = CodeGenerator.generateUid();

    @Mock
    private Environment environment;

    @BeforeEach
    void setUp()
        throws SQLException
    {
        this.subject = new OrganisationUnitSupplier( jdbcTemplate, environment );
        reset( mockResultSet );
        when( mockResultSet.next() ).thenReturn( true ).thenReturn( false );
    }

    @Test
    void handleNullEvents()
    {
        assertNotNull( subject.get( ImportOptions.getDefaultImportOptions(), null, null ) );
    }

    public void verifySupplier()
        throws SQLException
    {
    }

    @Test
    void verifyOuWith4LevelHierarchyIsHandledCorrectly()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "organisationunitid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "ALFA" );
        when( mockResultSet.getString( "path" ) ).thenReturn( "/aaaa/bbbb/cccc/abcded" );
        when( mockResultSet.getInt( "hierarchylevel" ) ).thenReturn( 4 );
        // create event to import
        Event event = new Event();
        event.setUid( uid );
        event.setOrgUnit( "abcded" );
        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );

        Multimap<String, String> orgUnitToEntity = HashMultimap.create();
        orgUnitToEntity.put( "abcded", uid );

        Map<String, OrganisationUnit> map = subject.get( ImportOptions.getDefaultImportOptions(),
            new HashSet<>( List.of( event.getUid() ) ), orgUnitToEntity );

        OrganisationUnit organisationUnit = map.get( event.getUid() );
        assertThat( organisationUnit, is( notNullValue() ) );
        assertThat( organisationUnit.getId(), is( 100L ) );
        assertThat( organisationUnit.getUid(), is( "abcded" ) );
        assertThat( organisationUnit.getCode(), is( "ALFA" ) );
        assertThat( organisationUnit.getPath(), is( "/aaaa/bbbb/cccc/abcded" ) );
        assertThat( organisationUnit.getHierarchyLevel(), is( 4 ) );
    }

    @Test
    void verifyOuWith1LevelHierarchyIsHandledCorrectly()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "organisationunitid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "ALFA" );
        when( mockResultSet.getString( "path" ) ).thenReturn( "/abcded" );
        when( mockResultSet.getInt( "hierarchylevel" ) ).thenReturn( 1 );
        // create event to import
        Event event = new Event();
        event.setUid( uid );
        event.setOrgUnit( "abcded" );
        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );

        Multimap<String, String> orgUnitToEntity = HashMultimap.create();
        orgUnitToEntity.put( "abcded", uid );

        Map<String, OrganisationUnit> map = subject.get( ImportOptions.getDefaultImportOptions(),
            new HashSet<>( List.of( event.getUid() ) ), orgUnitToEntity );
        OrganisationUnit organisationUnit = map.get( event.getUid() );

        assertThat( organisationUnit, is( notNullValue() ) );
        assertThat( organisationUnit.getId(), is( 100L ) );
        assertThat( organisationUnit.getUid(), is( "abcded" ) );
        assertThat( organisationUnit.getCode(), is( "ALFA" ) );
        assertThat( organisationUnit.getPath(), is( "/abcded" ) );
        assertThat( organisationUnit.getHierarchyLevel(), is( 1 ) );
    }

    @Test
    void verifyCodeSchemaForOu()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "organisationunitid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "CODE1" );
        when( mockResultSet.getString( "path" ) ).thenReturn( "/abcded" );
        when( mockResultSet.getInt( "hierarchylevel" ) ).thenReturn( 1 );
        // create event to import
        Event event = new Event();
        event.setUid( uid );
        event.setOrgUnit( "CODE1" );
        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );
        ImportOptions importOptions = ImportOptions.getDefaultImportOptions();
        importOptions.setOrgUnitIdScheme( IdScheme.CODE.name() );

        Multimap<String, String> orgUnitToEntity = HashMultimap.create();
        orgUnitToEntity.put( "abcded", uid );

        Map<String, OrganisationUnit> map = subject.get( ImportOptions.getDefaultImportOptions(),
            new HashSet<>( List.of( event.getUid() ) ), orgUnitToEntity );

        OrganisationUnit organisationUnit = map.get( event.getUid() );
        assertThat( organisationUnit, is( notNullValue() ) );
        assertThat( organisationUnit.getId(), is( 100L ) );
        assertThat( organisationUnit.getUid(), is( "abcded" ) );
        assertThat( organisationUnit.getCode(), is( "CODE1" ) );
        assertThat( organisationUnit.getPath(), is( "/abcded" ) );
        assertThat( organisationUnit.getHierarchyLevel(), is( 1 ) );
    }

    @Test
    void verifyAttributeSchemaForOu()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "organisationunitid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "CODE1" );
        when( mockResultSet.getString( "path" ) ).thenReturn( "/abcded" );
        when( mockResultSet.getInt( "hierarchylevel" ) ).thenReturn( 1 );
        when( mockResultSet.getString( "attributevalues" ) ).thenReturn( "someattributevalue" );
        // create event to import
        Event event = new Event();
        event.setUid( uid );
        event.setOrgUnit( "someattributevalue" );
        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );
        final String attributeId = CodeGenerator.generateUid();
        ImportOptions importOptions = ImportOptions.getDefaultImportOptions();
        importOptions.setOrgUnitIdScheme( IdScheme.ATTR_ID_SCHEME_PREFIX + attributeId );

        Multimap<String, String> orgUnitToEntity = HashMultimap.create();
        orgUnitToEntity.put( "someattributevalue", uid );

        Map<String, OrganisationUnit> map = subject.get( importOptions,
            new HashSet<>( List.of( event.getUid() ) ), orgUnitToEntity );

        final String executedSql = sql.getValue();
        OrganisationUnit organisationUnit = map.get( event.getUid() );
        assertThat( organisationUnit, is( notNullValue() ) );
        assertThat( organisationUnit.getId(), is( 100L ) );
        assertThat( organisationUnit.getUid(), is( "abcded" ) );
        assertThat( organisationUnit.getCode(), is( "CODE1" ) );
        assertThat( organisationUnit.getPath(), is( "/abcded" ) );
        assertThat( organisationUnit.getHierarchyLevel(), is( 1 ) );
        assertThat( executedSql,
            is( "select ou.organisationunitid, ou.uid, ou.code, ou.name, ou.path, ou.hierarchylevel ,attributevalues->'"
                + attributeId + "'->>'value' as attributevalues from organisationunit ou where ou.attributevalues#>>'{"
                + attributeId + ",value}' in (:ids)" ) );
    }
}
