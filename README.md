# NoSchema

NoSchema is a Java library for storing domain objects in Cassandra using a document-oriented repository pattern. It writes entity payloads as serialized blobs while extracting identifier components into Cassandra key columns, allowing denormalized access patterns without manual materialized-view maintenance.

## Highlights
- `PrimaryTable`, `View`, and `Index` definitions via DSLs.
- Automatic fan-out writes across primary/view/index tables through `UnitOfWork`.
- Pluggable serialization with `DocumentCodec<T>`.
- Built-in codecs: `JacksonObjectCodec` and `GsonObjectCodec`.
- Lifecycle hooks at both document and entity layers:
  - `DocumentObserver` (`AbstractDocumentObserver`, `GzipDocumentObserver`)
  - `RepositoryObserver<T>`

## Modules
1. `core`: shared interfaces and contracts (`Repository`, `Identifiable`, `Identifier`, `DocumentCodec`, exceptions, unit-of-work abstractions).
2. `cassandra`: Cassandra repository implementation (`CassandraRepository`), table/key DSLs, schema providers, and Cassandra-specific unit-of-work strategies.
3. `jackson-provider`: Jackson implementation of `DocumentCodec`.
4. `gson-provider`: Gson implementation of `DocumentCodec`.

## Requirements
- Java 21
- Maven 3.8+
- Cassandra cluster reachable by your application

## Maven Dependencies
Jackson setup:

```xml
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-cassandra</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-jackson-provider</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

Gson setup:

```xml
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-cassandra</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-gson-provider</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start
Entities must implement `Identifiable` by returning an `Identifier` from `getIdentifier()`.

Define a repository by extending `CassandraRepository<T>`:

```java
public class AlbumRepository extends CassandraRepository<Album>
{
	private static final String BY_NAME = "by_name";

	public AlbumRepository(CqlSession session, String keyspace, DocumentCodec<Album> codec)
	throws KeyDefinitionException
	{
		super(session,
			new PrimaryTable(keyspace, "albums", "id:uuid unique")
				.withView(BY_NAME, "(account.id as account_id:uuid), name:text unique"),
			UnitOfWorkType.ASYNC,
			codec
		);
	}

	public Album readById(UUID id)
	{
		return read(new Identifier(id));
	}

	public Album readByName(UUID accountId, String name)
	{
		return read(BY_NAME, new Identifier(accountId, name));
	}
}
```

Use the repository:

```java
DocumentCodec<Album> codec = new JacksonObjectCodec<>();
AlbumRepository albums = new AlbumRepository(session, "sample_keyspace", codec);
albums.withDocumentObserver(new GzipDocumentObserver());

albums.ensureTables();
Album created = albums.create(album);
Album byId = albums.read(new Identifier(created.getId()));
```

## Key Definition DSL
Key strings support partition keys, clustering keys, property-to-column mapping, and uniqueness:

```text
id:uuid unique
(account.id as account_id:uuid), name:text unique
((account.id as account_id:uuid), -created_at:timestamp)
```

General grammar:

```bnf
key-definition ::= "(" partition-key ")" clustering-key modifier
partition-key ::= column-definition | column-definition "," column-definition
clustering-key ::= clustering-key-component | clustering-key "," clustering-key-component
modifier ::= "unique" | ε
clustering-key-component ::= "+" column-definition | "-" column-definition | column-definition
column-definition ::= name-type-pair | property-name " as " name-type-pair
name-type-pair ::= name ":" type
property-name ::= [a-zA-Z0-9_]+
```

## Build and Test
- `mvn clean verify`: full build with tests and source/javadoc artifacts.
- `mvn test`: run all tests.
- `mvn -pl cassandra -Dtest=KeyDefinitionParserTest test`: run one test class.

## Contributing
Submit pull requests with focused, module-scoped changes, a clear rationale, and test commands used.

## License
NoSchema is licensed under Apache License 2.0.
