TITAN-ANDROID
=============

![Feature Image](https://github.com/wuman/titan-android/raw/master/doc/images/titan-logo.png)

Titan-Android is a port/fork of [Titan](https://github.com/thinkaurelius/titan)
for the Android platform. It is meant to be a light-weight implementation of a
graph database on mobile devices. The port removes HBase and Cassandra support
as their usage make little sense on a mobile device (convince me otherwise!).
Gremlin is only supported via the Java interface as I have not been able to 
port groovy successfully. Nevertheless, Titan-Android supports local storage 
backend via BerkeleyDB and supports the [Tinkerpop](http://tinkerpop.com/) 
stack natively.


Including in Your Project
-------------------------

There are two ways to include the library in your projects:

1. You can download the released jar file in the [Downloads section](https://github.com/wuman/titan-android/downloads).
2. If you use Maven to build your project you can simply add a dependency to 
   the desired component of the library.

        <dependency>
            <groupId>com.wu-man</groupId>
            <artifactId>titan-android</artifactId>
            <version>0.1.0.0</version>
        </dependency>


What is Titan
-------------

Titan is a highly scalable [graph database](http://en.wikipedia.org/wiki/Graph_database) 
optimized for storing and querying large graphs with billions of vertices and 
edges distributed across a multi-machine cluster. Titan is a transactional 
database that can support 
[thousands of concurrent users](http://thinkaurelius.com/2012/08/06/titan-provides-real-time-big-graph-data/).

Titan has the following features:

* Elastic and linear scalability for a growing data and user base.
* Data distribution and replication for performance and fault tolerance.
* Support for [ACID](http://en.wikipedia.org/wiki/ACID) and 
  [eventual consistency](http://en.wikipedia.org/wiki/Eventual_consistency).
* Support for various [storage backend](https://github.com/thinkaurelius/titan/wiki/Storage-Backend-Overview):
    * [Apache Cassandra](http://cassandra.apache.org)
    * [Apache HBase](http://hbase.apache.org)
    * [Oracle BerkeleyDB](http://www.oracle.com/technetwork/database/berkeleydb/overview/index-093405.html)
* Native integration with the [TinkerPop](http://www.tinkerpop.com) graph stack:
    * [Gremlin](http://gremlin.tinkerpop.com) graph query language
    * [Frames](http://frames.tinkerpop.com) object-to-graph mapper
    * [Rexster](http://rexster.tinkerpop.com) REST graph server
    * [Blueprints](http://blueprints.tinkerpop.com) standard graph API


Contribute
----------

If you would like to contribute code you can do so through GitHub by forking 
the repository and sending a pull request.


Developed By
------------

* Android porting contributor
    * David Wu - <david@wu-man.com> - [http://blog.wu-man.com](http://blog.wu-man.com)
* Original contributors to Titan
    * Marko A. Rodriguez - <marko@markorodriguez.com> - http://markorodriguez.com
    * Matthias Broecheler - <me@matthiasb.com> - http://matthiasb.com
    * Daniel LaRocque - <dalaro@hopcount.org> 


License
-------

    Copyright 2012, David Wu
    Copyright 2012 Aurelius LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

