package org.neo4j.integration.sql.exportcsv.io;

import java.util.HashMap;

import org.junit.Test;

import org.neo4j.integration.sql.RowAccessor;
import org.neo4j.integration.sql.exportcsv.ColumnUtil;
import org.neo4j.integration.sql.metadata.Column;
import org.neo4j.integration.sql.metadata.ColumnRole;
import org.neo4j.integration.sql.metadata.TableName;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RowStrategyTest
{
    private ColumnUtil columnUtil = new ColumnUtil();

    @Test
    public void shouldReturnTrueIfAnyOfTheNonKeyColumnsAreNull() throws Exception
    {
        // given
        HashMap<String, String> rowOne = new HashMap<>();
        rowOne.put( "id", "1" );
        rowOne.put( "username", "user-1" );
        rowOne.put( "first_name", "Boaty" );
        rowOne.put( "last_name", "Mc.Boatface" );
        rowOne.put( "age", null );

        TableName table = new TableName( "test.users" );
        Column[] columns = new Column[]{
                columnUtil.column( table, "id", ColumnRole.PrimaryKey ),
                columnUtil.column( table, "username", ColumnRole.ForeignKey ),
                columnUtil.column( table, "age", ColumnRole.Data ),
                columnUtil.compositeKeyColumn( table, asList( "first_name", "last_name" ), ColumnRole.PrimaryKey )};

        // when
        RowStrategy strategy = RowStrategy.IgnoreRowWithNullKey;

        // then
        RowAccessor stubRowAccessor = columnLabel -> singletonList( rowOne ).get( 0 ).get( columnLabel );
        assertTrue( strategy.test( stubRowAccessor, columns ) );
    }

    @Test
    public void shouldReturnFalseIfAnyOfTheKeyColumnsAreNull() throws Exception
    {
        // given
        HashMap<String, String> rowOne = new HashMap<>();
        rowOne.put( "id", "1" );
        rowOne.put( "username", null );
        rowOne.put( "age", "42" );
        TableName table = new TableName( "test.Users" );

        Column[] columns = new Column[]{
                columnUtil.column( table, "id", ColumnRole.PrimaryKey ),
                columnUtil.column( table, "username", ColumnRole.ForeignKey ),
                columnUtil.column( table, "age", ColumnRole.Data )};

        // when
        RowStrategy strategy = RowStrategy.IgnoreRowWithNullKey;

        // then
        RowAccessor stubRowAccessor = columnLabel -> singletonList( rowOne ).get( 0 ).get( columnLabel );
        assertFalse( strategy.test( stubRowAccessor, columns ) );
    }

    @Test
    public void shouldReturnFalseIfAnyOfTheCompositeKeyColumnsAreNull() throws Exception
    {
        // given
        HashMap<String, String> rowOne = new HashMap<>();
        rowOne.put( "age", "42" );

        TableName table = new TableName( "test.Users" );
        Column compositeColumn = columnUtil.compositeKeyColumn( table, asList( "first_name", "last_name" ),
                ColumnRole.PrimaryKey );

        Column[] columns = new Column[]{
                compositeColumn,
                columnUtil.column( table, "age", ColumnRole.Data )};

        // when
        RowStrategy strategy = RowStrategy.IgnoreRowWithNullKey;

        // then
        RowAccessor stubRowAccessor = columnLabel -> singletonList( rowOne ).get( 0 ).get( columnLabel );
        assertFalse( strategy.test( stubRowAccessor, columns ) );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleAccessorException() throws Exception
    {
        // given
        TableName table = new TableName( "users" );
        Column[] columns = new Column[]{
                columnUtil.column( table, "id", ColumnRole.PrimaryKey ),
                columnUtil.column( table, "username", ColumnRole.ForeignKey ),
                columnUtil.column( table, "age", ColumnRole.Data )};

        // when
        RowStrategy strategy = RowStrategy.IgnoreRowWithNullKey;

        // then
        RowAccessor stubRowAccessor = columnLabel -> {
            throw new IllegalStateException();
        };
        strategy.test( stubRowAccessor, columns );
        fail( "Should have bubbled up the exception" );
    }
}
