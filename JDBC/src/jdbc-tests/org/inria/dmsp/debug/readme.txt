1. How are the traces produced?
The traces of the JDBC commands are printed in the method call() of class DBMS using the following format(end with " "):
TRACE_CALL;cmd;nb_params;static_plan_modif;plan_params_size;plan_params[0]-plan_params[1]-...;res; 

Note: 1) there are some APIs defined by DBMS as abstract methods. 
The traces for the APIs should be printed using the following format(end with " "):
TRACE_API;API_name;args[0];args[1];...;args[n]; 
In our platform, these APIs are implemented by DBMSTcp

2) The traces of open() and exit() methods of class DBMS are also needed. 
They are printed using the same format as TRACE_API(end with " "):
TRACE_API;API_name;args[0];args[1];...;args[n]; 

2. How are the traces replayed?
The traces are replayed by calling the method replayTraces() of class TracesReplayer:
for TRACE_CALL, parse the traces to get the parameters of the method call() of class DBMS;
for TRACE_API, parse the traces to get the name and parameters of the API. 
