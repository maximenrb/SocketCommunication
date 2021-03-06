# TP-1_POA
Commands communication between a client and a server with Sockets 

# Commands
## Compilation

Schema:
```
compilation#relative_source_file1,relative_source_file2,...#relative_output_directory
```

Example:
```
compilation#server/classes/Course.java,server/classes/Student.java#server/out
```

## Loading class

Schema:
```
load#qualified_class_name#relative_output_directory
```

Example:
```
load#classes.Course#server/out
```

## Create class

Schema:
```
create#qualified_class_name#object_id
```

Example:
```
create#classes.Course#8inf853
```

## Write attribute

Schema:
```
write#object_id#attribute_name#value
```

Example:
```
write#8inf853#title#Architecture des applications
```

## Read attribute

Schema:
```
read#object_id#attribute_name
```

Example:
```
read#8inf853#title
```

## Call a function

Schema:
```
function#object_id#function_name#type1:value1,type2:value2...
```

Example:
```
function#8inf853#attributeNote#classes.Student:ID(mathilde),float:3.7
```


# Launching Server

```
ApplicationServer <port> <relative source directory> <relative classes directory> <relative log file path>
```


# Launching Client
```
ApplicationClient <hostname> <port> <relative command file path> <relative output file path>
```