# DO assignment

To run this project you need a Java runtime installed, if you want to build the project from
scratch you'll need [SBT](http://www.scala-sbt.org/) installed.

To run the project you can just go with:

    ./target/universal/stage/bin/digital-ocean

This will run the project using Netty, if you want to run the threaded version you use:

    ./target/universal/stage/bin/digital-ocean -- threaded

If you want to build it from scratch and run the simplest solution is to run:

    sbt stage && ./target/universal/stage/bin/digital-ocean

This will produce the executable, to run the test suite you can just run:

    sbt test

And this runs the test suite for the project.

## Design rationale

### Index structure

The index implementation itself follows the idea of a read-mostly data structure with a read-write
lock, readers do not block when trying to read data from it but if a writer comes in other operators
block until the write is finished. This allows the implementation to provide good throughput
(assuming it will be read-mostly) and only blocking for small periods when writing.

Using concurrent collections here wouldn't make much difference as most operations are not atomic,
as we need to both include a package to the index and introduce it to all of it's dependencies.
Using concurrent collections would still require locks as, for clients, every operation has to be
assumed as atomic and that would not be the case if we didn't fully prevent writes from overlapping
with each other.

While having the `Package` class fully mutable due to it's dependables might not be the cleanest
design (it could have been immutable) it allows for fast include and remove operations as there is
no need to navigate the whole index to find all dependents.

### Threaded server

Following the requirements from the assignment (no external libraries), there is a fully functional
server that uses blocking IO and threads to handle the clients. The implementation is a simple
thread-per-client-socket solution where each client that connects gets it's own thread handling
the socket. Since the index itself is thread-safe there is no need to have locks or thread-safety
protections at this part of the system.

Threads read data from clients (the `ClientWorker` class is the one responsable here) until they
have a full message (once `\n` happens) and then forward it to the `OperationExecutor`
(this class exists only because we want to reuse the message parser for the Netty version as well),
once they get a response they write it to the socket and wait for the next message.

This solution is fully functional (and could even possibly be deployed to an actual production
environment), but it's not really the most scalable solution. A thread-per-client implementation
requires much more resources to be allocated for every connected client and these resources will
tax both memory and the CPU unnecessarily, something that can be simply solved by using a
non-blocking IO library, like Netty below.

### Netty server

Doing non-blocking IO in Java/Scala using the standard library is fully possible but it's, most of
the time, undesirable. The API is really low level and offers very little other than a notification
that bytes have been written/read from a specific channel and this leads to most non-blocking IO
apps in the environment to be built using libraries like Netty. So while this jumps over the _no
external libraries_ requirement I believe it's worth the case here as this is how I would actually
design this solution if I wanted to build it for production and for a team to work on.

The main difference here between this and the threaded solution is that there is no
thread-per-client, threads here aren't even visible for the code written, it's all managed by
Netty itself. The concurrency model lives inside the library event loop and can be selected as
needed by the application instead of being forced to be one or the other. By default it will go for a non-blocking IO solution that only uses threads when there is actual data to be processed, making it much more scalable and using much less resources than the threaded version.

The library also offers a bit more structure, so this implementation has a separate response encoder
and also uses a framework provided frame decoder that knows how to break the input messages on
every `\n`. It could also have a decoder that knows how to read the messages and forwards only a
simple object instead of `ByteBuf` objects but it made more sense here to just reuse the
`OperationExecutor` for this purpose.

### General considerations

Both implemetations would also benefit from a timeout from clients that are taking too long to
produce any messages as they're mostly taking up resources and not making any useful operations.
