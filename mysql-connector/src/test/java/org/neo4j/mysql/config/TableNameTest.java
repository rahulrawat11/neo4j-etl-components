package org.neo4j.mysql.config;

import org.junit.Test;

import org.neo4j.ingest.config.Field;

import static org.junit.Assert.*;

public class TableNameTest
{
    @Test
    public void shouldReturnSimpleNameFromQualifiedName()
    {
        // given
        TableName tableName = new TableName( "example.Person" );

        // when
        String simpleName = tableName.simpleName();

        // then
        assertEquals( "Person", simpleName );
    }

    @Test
    public void shouldReturnSimpleNameFromSimpleName()
    {
        // given
        TableName tableName = new TableName( "Person" );

        // when
        String simpleName = tableName.simpleName();

        // then
        assertEquals( "Person", simpleName );
    }
}