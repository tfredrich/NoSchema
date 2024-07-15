# NoSchema

NoSchema is a Java library offering a document-oriented repository pattern for Cassandra storage. It eliminates the need for a predefined schema, thereby providing significant flexibility in domain modeling without necessitating table data migrations.

In NoSchema, only the keys that form an object's identifier are stored as individual columns. These keys are extracted from the entity at storage time. The object itself is serialized into a storage format through a pluggable serialization process and stored as a binary blob. Currently, NoSchema supports BSON (like MongoDB) and GSON (JSON) serialization formats.

Given that usage of materialized views and indexes in Cassandra are discouraged (at least in high-throughput scenarios), NoSchema introduces the concepts of a `PrimaryTable`, `View`, and `Index`. These are fully managed and encapsulated within a Repository pattern that implements UnitOfWork. This ensures that multiple, denormalized tables are written; one for each key format, eliminating complex coding, reducing time-to-market, increasing developer efficiency and software accuracy.

NoSchema simplifies the management of resource-oriented Plain Old Java Objects (PoJos) with multiple, denormalized views, and indexes. This is achieved through a straightforward Repository pattern, making it an ideal choice for developers building RESTful APIs to simplify their data storage in Cassandra while increasing functionality.

## Features
* **Primary Table**: A `PrimaryTable`  is typically identified by a single-unique identifier (like a UUID) is easily defined using the PrimaryTable DSL.

* **Views**: A `View` of a primary table with a completely different key structure is easily created and maintained automatically along with the `PrimaryTable` CRUD operations via a Unit of Work.

* **Indexes**: An `Index` of the primary tale with a completely different key structure and only a reference to the primary table identifier (`Index`es don't replicate the primary table data) is maintained alongside the `PrimaryTable` via a Unit of Work.

* **UnitOfWork**: NoSchema includes a built-in UnitOfWork class that provides pseudo-transactions across `PrimaryTable`s, `View`s, and `Index`es. There are implementations that honor various consistency levels.

* **Repository Pattern**: The project provides a default repository implementation, `CassandraNoSchemaRepository` to enable quick and easy CRUD operations for storing and retrieving PoJos on Cassandra.

* **Repository Observer**: When you need to "get in the game" of the repository, the `DocumentObserver` class can be implemented to inject your own code into the processing chain. The default, do-nothing implementation is `AbstractDocumentObserver` which can be extended when only a method or two need overriding. There is also, `EntityObserver` for when the entity contained in the document needs tweaking before or after serialization.

* **Metadata Built-In**: Every document stored in the database contains an underlying `metadata` map column that can be used for things like encryption key names, etc. that apply to the stored, embedded entity.

* **Pluggable Serialization**: The project includes `BsonObjectCodec` and `GsonObjectCodec` classes that provide methods for serializing and deserializing objects to and from BSON and Gson formats respectively. If Jackson is already used in your project, simply implement the `ObjectCodec` interface and pass an instance in to the repository.

## Modules
The project is divided into four main modules:

1. **core**: This module provides the base classes and interfaces such as `Identifiable` and `Identifier`.

1. **cassandra**: This module provides the base Respository class for Cassandra database operations and also includes the UnitOfWork implementation, which is used by the Repository.

1. **bson-provider**: This module provides the BsonObjectCodec class for BSON serialization and deserialization. The class implements the ObjectCodec interface and provides methods for serializing and deserializing objects to and from BSON format.

1. **gson-provider**: This module provides the GsonObjectCodec class for Gson serialization and deserialization. The class implements the ObjectCodec interface and provides methods for serializing and deserializing objects to and from Gson format. The class is located in the com.strategicgains.noschema.gson package.

## Getting Started
1. One requirement for using NoSchema is that entities **MUST** implement the `Identifiable` interface which needs a `getId()` method returning an `Identifier` instance containing the primary identifier components for the entity.

1. Choose a serialization provider: BSON (recommended) and GSON are built-in. But if you want to use something that's already in your project, implement the `ObjectCodec` interface.

1. Override the CassandraNoSchemaRepository for each PoJo, defining the `PrimaryTable`s and `View`s for the resource (See: *Defining Keys*, below).

1. Override any methods needed in `AbstractDocumentObserver` to process entities before or after encoding or before or after storage. For example, encryption/decryption of the entity, event streaming for CUD operations, etc. Add your observer to the repository constructor using the `.withObserver()` DSL setter.

1. Determine the UnitOfWork consistency level. The default is to use the capabilities of the Cassandra client driver to distribute and manage the multiple statements across the views as completely asynchronous operations. It is also possible to use LOGGED and UNLOGGED batch operations, but know that these are *anti-patterns in Cassandra due to them likely being cross-partition writes and will impact performance.*

### Maven Configuration
For the recommended configuration using BSON as the serialization provider, there are two dependencies needed in the Maven `pom.xml`.

```xml
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-cassandra</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>com.strategicgains.noschema</groupId>
	<artifactId>noschema-bson-provider</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```
If GSON is desired instead:

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

## Usage
Here is a basic example of how to use NoSchema.

Note that this example doesn't override the default `CassandraNoSchemaRepository` but uses it raw for illustration purposes.
You will likely want to override it to scope it to your own resources.:

```java
public static void main(String[] args)
throws KeyDefinitionException, InvalidIdentifierException, DuplicateItemException, ItemNotFoundException
{
	CqlSession session = ... // create the CqlSession as usual.

	// Define a table and its views.
	PrimaryTable albumsTable = new PrimaryTable("sample_keyspace", "albums", "id:UUID unique")
		.withView("by_name", "(account.id as account_id:UUID), name:text unique");

	try
	{
		// Create a Repository instance.
		// This one uses the asyncronous UnitOfWork and BSON serialization.
		CassandraNoSchemaRepository<Album> albums = new CassandraNoSchemaRepository<>(session, albumsTable, UnitOfWorkType.ASYNC, new BsonObjectCodec());

		// this creates any missing underlying Cassandra tables (for both PrimaryTable and any Views)
		albums.ensureTables();

		// Create a new in-memory Album instance.
		UUID id = UUID.randomUUID();
		UUID accountId = UUID.randomUUID();
		Album album = new Album(accountId, id, "AC/DC", "Back In Black", ...);

		// Write the Album, writing both PrimaryTable and View at once, ensuring uniqueness of both the primary table and `by_name` view.
		Album written = albums.create(album);
		System.out.println(written.toString());

		// Read the album by its ID.
		Album read = albums.read(new Identifier(id));
		System.out.println(read.toString());

		// Read the album by account number and name.
		read = albums.read(ALBUMS_BY_NAME, new Identifier(accountId, "Back In Black"));
		System.out.println(read.toString());
	}
	finally
	{
		session.close();
	}
}
```

### Defining Keys

NoSchema has its own key definition language. While it is very similar to Cassandra's Partition Key and Clustering Key concepts, it needs a type for each of those components. It also allows the mapping of PoJo property names to a column name that doesn't match. Additionally, NoSchema supports uniqueness enforcement while Cassandra does not.

Key definition strings are expected to follow a specific format, which is defined as follows:

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

In the above BNF-style diagram:
- The `partition-key` is a comma-separated list of `column-definitions` enclosed in parentheses.
- The `clustering-key` is a comma-separated list of `clustering-key-components`.
- The `modifier` is an optional "unique" keyword which causes the `UnitOfWork` to enforce uniqueness on create/update and requires presence on delete. Note this causes read-before-write on create/update/delete.
- The `clustering-key-component` is a `name-value-pair`, optionally prefixed with a "+" or "-" to indicate ascending or descending order, respectively.
- The `column-definition` is a `name-type-pair`, optionally prefixed with a `property-name` and "as" to indicate a different name in the entity.
- The `name-type-pair` is a property name and a type separated by a colon.
- The `property-name` is a string of alphanumeric characters and underscores; a PoJo property name.

Examples:

```bnf
(id:uuid)	// partition key only
((id:uuid) name:text unique)	// partition key of id, clustering key of name, with unique modifier.
((id:uuid, name:text), -created:timestamp, +age:int)	// partition key of id and name + clustering key of created and age, with sort order on each.
```

The class throws a `KeyDefinitionException` if the string is invalid. This can occur if the string is null or empty, if it contains too many parentheses, if a parenthesis is misplaced, or if the parentheses are unmatched.
The class throws an `InvalidIdentifierException` if the entity is missing any property values required by the key definition while the identifier is being extracted from a PoJo at storage time.


## Contributing
Contributions are welcome! Submit a pull request from your own clone of this GitHub repo.

## License
This project is licensed under the terms of the Apache 2.0 license.