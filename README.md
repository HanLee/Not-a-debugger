# Not-a-debugger
An experiment into dynamic hooking and modification of Android application functions and return values.

Summary
--------
Not a debugger (NAD) was created to perform run time analysis, tracing and tampering of Android application function input parameters and return values.

NAD provides an user interface written in Java, which allows the user to connect to the accompanying Android application using their testing machine. This allows convenient manipulation of Android parameters and hooks.

This application is built upon the Xposed Framework and it is necessary for this to work. This is just a POC so expect it to break, turn on logcat to view any error messages.

Features
--------

### Websocket Connection
  Turn on the switch on the accompanying Android application, enter the device's Ip address into the IP field and click "Connect" and you're done!
  
### Apps Tab
  The "Apps" tab lists all the applications that are installed on the device. The freetext search field allows easy searching of applications installed on the Android device to be hooked.
  ![gui explaination](/screenshots/gui-1.png?raw=true)

  Selecting the application to analyze is as easy as double clicking on the package name on the list displayed and confirming your selection.
  ![gui explaination](/screenshots/gui-2-hook.png?raw=true)
  
  Note, ensure that the application you have selected to hook is not running in the background after selection. NAD only analyzes the application classes and methods of the application when it is starting up. If it is running, kill it with the task manager or something similar. 
  
### Functions Tab
  After hooking using the "Apps" tab, launch the application and browse to the "Functions" tab, it will list out the functions within the application to be analyzed. 
  ![gui explaination](/screenshots/gui-3-functions.png?raw=true)
  
  To hook a function, double click on the function and select what you would like to hook.
  ![gui explaination](/screenshots/gui-4-hook function.png?raw=true)
  
  ***Hook Input Parameters Only***
  Choose this if you want to tamper only the input parameters of the function when the function is called. Note: Not all parameters are able to be edited. When such a parameter is encountered, that parameter will be skipped.
  
  ***Hook Return Value Only***
  Choose this only if you want to tamper with the return values of a function. Note that not all values are able to be edited, nor will it work on a function that returns a void (probably will crash).
  
  ***Hook Input and Return Values***
  Choose this if you want to hook and tamper with both the input parameters and the return values of that function.
  
  ***Remove Hook***
  Obviously, remove the hook.
  
  Example, we want to analyze the parameters and the result of the return value of the function "boolean a(android.content.Context, java.lang.String)". We select the "Hook Input and Return Values" option.
  
  We entered the string "Secret Password" on our target application and clicked on the "VERIFY PASSWORD" button that triggered the function. When the function is called, we get a prompt that shows us the String paramter as shown below, looks like our user input is passed into this function. Here we are free to modify the string in the prompt if we want to, perhaps bypass user input restrictions?
  ![gui explaination](/screenshots/gui-5-intercept param.png?raw=true)
  
  After we click on "OK", the function returns the boolean "false" (obviously as we did not enter the right password). Once again, as we selected the option to hook the return value as well, we are presented with the next prompt as shown below:
  ![gui explaination](/screenshots/gui-6-intercept return.png?raw=true)
  
  Here as this is a boolean, we get the option to make it return true or false. In this case the function returned "false", let's turn that into a true by editing it to "true" and we click "OK" again. Note that putting anything else than what the function expects will cause it to crash.
  ![gui explaination](/screenshots/gui-7-success.png?raw=true)
  
  The application also supports custom objects that are passed around to functions:
  ![gui explaination](/screenshots/gui-8-intercept custom object param.png?raw=true)
  
  Arrays are supported too!
  ![gui explaination](/screenshots/gui-9-intercept return value.png?raw=true)
  
### Options Tab
  The "Options" tab offers the user 2 functions. Mainly "Trace mode", and also "Canary mode". 
  
  The "Trace mode" toggle allows the user to turn on / off the trace mode. Turn this mode on, launch the application and mess around with it. Logcat will show a trace of all the functions of the application that were called.
  Options tab GUI:
  ![gui explaination](/screenshots/gui-13-canary-mode.png?raw=true)
  
  Logcat output of the teamsik application:
  ![gui explaination](/screenshots/gui-11-tracemode-output.png?raw=true)
  
  The hooking function, trace mode and the canary mode (more details below) functions are exclusive, and can only work when the other is off. Remember to turn off the trace mode when you want to start hooking functions!
  
  The "Canary mode" toggle allows the user to define a unique canary token, in the screenshot above, the value of "deadbeef". In the demo application screenshot below, we enter the string "deadbeef" into the input text and we click the "VERIFY PASSWORD" button.
  ![gui explaination](/screenshots/gui-15-canary-input.png?raw=true)
  
  Any functions that receives a parameter with the value of the canary token will be printed out to logcat as shown in the screenshot below:
  ![gui explaination](/screenshots/gui-16-canary-result.png?raw=true)
  
  The "Canary mode" just like trace mode, works exclusively by itself. When in this mode, you will not be able to modify/hook on the fly.
  
  Upon connecting to the Android application with the client, if the canary mode is turned on, a prompt will alert you that canary mode is turned on.
  ![gui explaination](/screenshots/gui-14-canary-mode-alert.png?raw=true)
  
### Accompanying Android Application
  There is nothing fancy about the accompanying android application. Just an on/off switch.
  ![gui explaination](/screenshots/gui-10-android.png?raw=true)
