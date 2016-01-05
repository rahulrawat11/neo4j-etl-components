package org.neo4j.command_line;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.neo4j.io.InMemoryStreamRecorder;
import org.neo4j.io.StreamEventHandler;

import static java.util.Arrays.asList;

import static org.neo4j.command_line.Result.Evaluator;

class CommandsBuilder
        implements Commands.Builder.WorkingDirectory,
        Commands.Builder.ResultEvaluator,
        Commands.Builder.TimeoutMillis,
        Commands.Builder.Environment,
        Commands.Builder.Redirection,
        Commands.Builder
{
    final List<String> commands;
    Optional<Path> workingDirectory = Optional.empty();
    Evaluator resultEvaluator = Evaluator.FAIL_ON_NON_ZERO_EXIT_VALUE;
    long timeoutMillis = -1;
    Map<String, String> extraEnvironment = Collections.emptyMap();
    StreamEventHandler stdOutEventHandler = new InMemoryStreamRecorder();
    StreamEventHandler stdErrEventHandler = new InMemoryStreamRecorder();
    ProcessBuilder.Redirect stdInRedirect = ProcessBuilder.Redirect.PIPE;

    public CommandsBuilder( String... commands )
    {
        this.commands = asList( commands );
    }

    @Override
    public ResultEvaluator workingDirectory( Path workingDirectory )
    {
        this.workingDirectory = Optional.ofNullable( workingDirectory );
        return this;
    }

    @Override
    public ResultEvaluator inheritWorkingDirectory()
    {
        return workingDirectory( null );
    }

    @Override
    public TimeoutMillis commandResultEvaluator( Evaluator resultEvaluator )
    {
        this.resultEvaluator = resultEvaluator;
        return this;
    }

    @Override
    public TimeoutMillis failOnNonZeroExitValue()
    {
        this.resultEvaluator = Evaluator.FAIL_ON_NON_ZERO_EXIT_VALUE;
        return this;
    }

    @Override
    public TimeoutMillis ignoreFailures()
    {
        this.resultEvaluator = Evaluator.IGNORE_FAILURES;
        return this;
    }

    @Override
    public Environment timeout( long timeout, TimeUnit unit )
    {
        timeoutMillis = unit.toMillis( timeout );
        return this;
    }

    @Override
    public Environment noTimeout()
    {
        this.timeoutMillis = -1;
        return this;
    }

    @Override
    public Redirection inheritEnvironment()
    {
        return this;
    }

    @Override
    public Redirection augmentEnvironment( Map<String, String> extra )
    {
        this.extraEnvironment = extra;
        return this;
    }

    @Override
    public Redirection redirectStdInFrom( ProcessBuilder.Redirect redirection )
    {
        this.stdInRedirect = redirection;
        return this;
    }

    @Override
    public Redirection redirectStdOutTo( StreamEventHandler streamEventHandler )
    {
        this.stdOutEventHandler = streamEventHandler;
        return this;
    }

    @Override
    public Redirection redirectStdErrTo( StreamEventHandler streamEventHandler )
    {
        this.stdErrEventHandler = streamEventHandler;
        return this;
    }

    @Override
    public Commands build()
    {
        return new Commands( this );
    }
}
