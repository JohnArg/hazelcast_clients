# Hazelcast IMDG Clients

This project was created for some experiments/benchmarks
conducted for an MSc Thesis with title
"Efficient State Machine Replication With RDMA RPCs in Java".
It creates [Hazelcast IMDG](https://github.com/JohnArg/hazelcast)
clients that call CP operations on a Hazelcast IMDG cluster.
This allows running benchmarks on the Hazelcast IMDG's CP
subsystem performance. The project was used in the evaluation
process of the MSc Thesis to evaluate the performance of
Hazelcast IMDG's CP subsystem when using either RDMA or TCP 
socket communications.


## Types Of Hazelcast Clients

### Timed

A timed client will call a specific CP operation continuously until
a timeout passes.
Timed clients for calling various CP operations can be found in the
<i>src/main/java/client/hazelcast/timed</i> package.

### Iterated

Used for throughput benchmarks with a single client and a specified
number of benchmark iterations. 
Iterated client bechmarks run a warm up phase first and a benchmark phase
afterwards. Users can determine the number of iterations in each phase.
These types of benchmarks can be found in the 
<i>src/main/java/client/hazelcast/iterated</i> package.

### Multi-threaded

Like the iterated clients, they are used for throughput benchmarks but
with more than one client threads. Warmup and benchmark iterations are
divided equally to the number of client threads and each client threads
runs it own iterations.
Multi-threaded client benchmarks can be found in the
<i>src/main/java/client/hazelcast/multithreaded</i> package.
