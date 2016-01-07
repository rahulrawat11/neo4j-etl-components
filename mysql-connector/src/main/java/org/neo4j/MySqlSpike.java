package org.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.neo4j.command_line.Commands;
import org.neo4j.ingest.config.Data;
import org.neo4j.ingest.config.DataType;
import org.neo4j.ingest.config.Field;
import org.neo4j.ingest.config.Formatting;
import org.neo4j.ingest.config.Id;
import org.neo4j.io.Pipe;
import org.neo4j.mysql.Export;
import org.neo4j.mysql.SqlRunner;
import org.neo4j.mysql.config.Column;
import org.neo4j.mysql.config.ExportConfig;
import org.neo4j.mysql.config.MySqlConnectionConfig;
import org.neo4j.mysql.config.Table;

import static java.lang.String.format;

public class MySqlSpike
{
    private static final String EXPORT_SQL = "LOAD DATA INFILE '%s' INTO TABLE javabase.test FIELDS TERMINATED " +
            "BY " +
            "'\\t' OPTIONALLY ENCLOSED BY '' ESCAPED BY '\\\\' LINES TERMINATED BY '\\n' STARTING BY ''";

    private static final String IMPORT_SQL = "SELECT id, data INTO OUTFILE '%s' FIELDS TERMINATED " +
            "BY " +
            "'\\t' OPTIONALLY ENCLOSED BY '' ESCAPED BY '\\\\' LINES TERMINATED BY '\\n' STARTING BY '' FROM javabase" +
            ".test";

    public static void main( String[] args ) throws Exception
    {
        doExport();
    }

    private static void doExport() throws Exception
    {
        MySqlConnectionConfig connectionConfig = new MySqlConnectionConfig(
                "jdbc:mysql://localhost:3306/javabase",
                "java",
                "password" );

        ExportConfig exportConfig = ExportConfig.builder()
                .destination( Paths.get( "/Users/iansrobinson/Desktop" ) )
                .mySqlConnectionConfig( connectionConfig )
                .formatting( Formatting.DEFAULT )
                .table( Table.builder()
                        .name( "javabase.test" )
                        .addColumn( Column.withName( "id" )
                                .mapsTo( new Field( "personId", Id.ID ) ) )
                        .addColumn( Column.withName( "data")
                                .mapsTo( new Field( "data", Data.ofType( DataType.String ) ) ) )
                        .build() )
                .build();

        Export export = new Export( exportConfig );
        Collection<Path> files = export.execute();

        for ( Path file : files )
        {
            System.out.println(file.toString());
        }
    }

    private static void originalTest() throws IOException
    {
        String exportId = UUID.randomUUID().toString();
        String importId = UUID.randomUUID().toString();

        MySqlConnectionConfig connectionConfig = new MySqlConnectionConfig(
                "jdbc:mysql://localhost:3306/javabase",
                "java",
                "password" );

        try ( Pipe pipe = new Pipe( exportId ) )
        {
            SqlRunner sqlRunner = new SqlRunner( connectionConfig, format( EXPORT_SQL, pipe.name() ) );
            CompletableFuture<OutputStream> out = pipe.out( sqlRunner.execute().toFuture() );

            try ( Writer writer = new OutputStreamWriter( out.get() ) )
            {
                writer.write( "50\tsometext\n" );
                Thread.sleep( 1000 );
                writer.write( "51\tsometext\n" );
                Thread.sleep( 1000 );
                writer.write( "52\tsometext\n" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        File importFile = new File( importId );

        try
        {
            SqlRunner sqlRunner = new SqlRunner( connectionConfig, format( IMPORT_SQL, importFile.getAbsolutePath() ) );
            Commands.commands( "chmod", "0777", importFile.getAbsoluteFile().getParent() ).execute().await();

            sqlRunner.execute().await();

            try ( BufferedReader reader =
                          new BufferedReader( new InputStreamReader( new FileInputStream( importFile ) ) ) )
            {
                String line;

                while ( (line = reader.readLine()) != null && !line.equals( "" ) )
                {
                    System.out.println( line );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            Files.deleteIfExists( importFile.toPath() );
        }
    }
}
