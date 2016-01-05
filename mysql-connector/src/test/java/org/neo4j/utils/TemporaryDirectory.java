package org.neo4j.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class TemporaryDirectory
{
    public static Resource<Path> temporaryDirectory()
    {
        return temporaryDirectory( "tmp" );
    }

    public static Resource<Path> temporaryDirectory( String prefix )
    {
        return new LazyResource<>( new LazyResource.Lifecycle<Path>()
        {
            @Override
            public Path create() throws IOException
            {
                return Files.createTempDirectory( prefix );
            }

            @Override
            public void destroy( Path directory ) throws IOException
            {
                try
                {
                    FileUtils.deleteDirectory( directory.toFile() );
                }
                catch ( IOException e )
                {
                    // Retry
                    try
                    {
                        FileUtils.deleteDirectory( directory.toFile() );
                    }
                    catch ( Exception ex )
                    {
                        ex.printStackTrace();
                    }
                }
            }
        } );
    }
}
