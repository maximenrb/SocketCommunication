# TP-1_POA
Commands communication between a client and a server with Sockets 

# Commands
1. Compilation

Schema:
```
compilation#relative_source_file1,relative_source_file2,...#relative_output_directory
```

Example:
```
compilation#server/classes/Course.java,server/classes/Student.java#server/out
```

2. Loading class

Schema:
```
load#qualified_class_name#relative_output_directory
```

Example:
```
load#classes.Course#server/out
```

3. Create class

Schema:
```
create#qualified_class_name#object_id
```

Example:
```
create#classes.Course#8inf853
```

4. Write attribute

Schema:
```
write#object_id#attribute_name#value
```

Example:
```
write#8inf853#title#Architecture des applications
```

5. Read attribute

Schema:
```
read#object_id#attribute_name
```

Example:
```
read#8inf853#title
```


6. Call a function

Schema:
```
function#object_id#function_name#type1:value1,type2:value2...
```

Example:
```
function#8inf853#attributeNote#classes.Student:ID(mathilde),float:3.7
```
