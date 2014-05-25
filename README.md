# RAML-CODEGEN

> A basic generator which eats a RAML file and put, either a REST-ASSURED test project, or a mock plug-in for Robohydra. 

# Installation

RAML-CODEGEN is a Java project which uses MAVEN.

_Warning_  : this project uses the Java RAML parser which is not yet available on the maven repository. You must install this [project](https://github.com/mulesoft/raml-jaxrs-codegen) before building RAML-CODEGEN.

_Nota_ : thanks to norberto.herz@mulesoft.com which has added the MuleSoft maven repository to the `pom.xml`.

`git clone` this repository (in a location that we call `$raml-codegen` in this document) then `cd $raml-codegen` then `mvn package`.

# Usage

The Maven project builds an executable `jar` file in `$raml-codegen/target` and all the needed dependencies in `$raml-codegen/target/lib`.

```
> cd target
> java -jar raml-codegen-1.0-SNAPSHOT.jar -h
usage: raml-codegen
 -d,--destination <pathName>   the path name of the project which have to
                               be generated
 -h,--help                     print this help
 -ht,--help-type               print the specific help types configuration
                               and options
 -o,--options <options>        the specific options of a type project
 -s,--source <ramlFile url>    the raml file url
 -t,--type <generationType>    the type of the project which have to be
                               generated
 -v,--verbose                  print full stacktraces when an error
                               occurred
```

`-t` specifies what kind of project you want to generate from the RAML file.

For this moment, `-t mocks` generates a Robohydra plug-in for mocking purpose and `-t tests` generates unit tests which validates an implementation against a specification.

# Mocks

## Plug-in installation

If you use the `-t mocks` option, then in the `$raml-codegen/target/$generated-sources` directory, you can find a `node.js` project which can be used as a plug-in for [Robohydra](https://github.com/robohydra/robohydra).

`cd $your-generated-api-mock` then `npm install`.

You can globally install Robohydra with the `npm install -g robohydra` command.

Then `robohydra $your-generated-api-mock` launch your mocking server.

## Usage

### Default behavior

You can play with your API : `http://localhost:3000/$plugin/path/to/your/raml/resources`

The plug-in returns the examples as they are described in the RAML specification.

But the RAML specification is very simple and doesn't allow to return different results for different parameters.

But the generated plug-in can be easily customized.

### Customization

In the Robohydra source plugin, you can find a `custom.json` file, which is a JSON file where you can use reg exp patterns to change the default behavior. 

Example:

```json
[
{
        "name":"/books",
        "DELETE": [
                {  
                  "comment": "Available params are: [log] [query]  ",
                  "pattern": null,
                  "statusCode": null,
                  "headers": null,
                  "content": null 
                }
        ],

        "GET": [
                {  
                  "comment": "Available params are: [author] [token] [edition] [page] [log] [query]  ",
                  "pattern": null,
                  "statusCode": null,
                  "headers": null,
                  "content": null 
                }
        ]},
{
        "name":"/books/:bookTitle",
        "GET": [
                {  
                  "comment": "Available params are: NONE ",
                  "pattern": null,
                  "statusCode": null,
                  "headers": null,
                  "content": null 
                }
        ],
        "POST": [
                {  
                  "comment": "Available params are: NONE ",
                  "pattern": null,
                  "statusCode": null,
                  "headers": null,
                  "content": null 
                }
        ],

        "PUT": [
                {  
                  "comment": "Available params are: NONE ",
                  "pattern": null,
                  "statusCode": null,
                  "headers": null,
                  "content": null 
                }
        ]}    
]
```

You can overload this file.
For example, for the `/books` rule, add the following pattern.

```json
"GET": [
                {  
                  "comment": "Available params are: [author] [token] [edition] [page] [log] [query]  ",
                  "pattern": "log=yes",
                  "statusCode": null,
                  "headers": null,
                  "content": "log.json" 
                }
```

Then if you use the `/books?log=yes` url in your Robohydra server, then, the default example in the RAML file is replaced by the content of the `log.json` file.

You can use the power of the `search/replace` reg exp to customize your plug-in.

```json
"GET": [
                {  
                  "comment": "Available params are: [author] [token] [edition] [page] [log] [query]  ",
                  "pattern": "log=(yes|no)",
                  "statusCode": null,
                  "headers": null,
                  "content": "log_$1.json" 
                }
```

Now, if you use `/books?log=yes`, then the content of the `log_yes.json` is returned. If you use `/books?log=no`, then the content of the `log_no.json` is returned.

In the custom file, you can change the status code for a request, or add your own headers (a json map).

In your content JSON files, you can use `$x` too. 

Moreover, the content JSON files use `Mustache` template engine. So, you can use the classical `Mustache` tag (`{{}}`) in your JSON content.

In your content file, you can use the following model objects :

- `q`: the request query parameters.
- `p`: the path paramaters.
- `url`: the request url.

Example (in a `log.json` file)

```json
{
    "log":{{q.log}}
}
```

If you want to understand a content return, you can use the html view of the plugin which displays logs. For such a view, you can use the `_verbose` query parameter in your URL.

Example : `/books?log=yes&_verbose`.

### Plug-in configuration

The generator produces a basic Robohydra configuration.

For exemple : 

```json
{"plugins": ["Test_API_v1"]}
```

You can change the directory where the plugin find your `custom.js` file and your JSON content files.

Change your `$plugin.conf` :

```json
{"plugins": [
    {"name": "Test_API_v1",
     "config": {"customerPath":"your/pathname/here"} 
    }
    ]
}
```

_Warning_ : each generation replaces the default custom file and erases your modification if you've changed this file. But the generation does not erase a `custom.js` file in another location that the default one (in the source directory of the plug-in).

# Tests

## Usage

If you use the `-t tests` option, RAML-CODEGEN generates a unit test projects in your destination path.

This project is a maven project.

To build it : `cd target/generated-sources/$yourApi`, then `mvn package`.

Then, you can run your test campaign with `mvn exec:exec`.

## Configuration

The generated project uses the `BaseUri` in your RAML file to know were is the implementation that you want to test.

You can generate tests against onother location with the `-o` option of the generator.

Example :

```
java -jar raml-codegen-1.0-SNAPSHOT.jar -s http://.../myApi.raml -t tests -o baseUri:http://xxx.yyy/
```

